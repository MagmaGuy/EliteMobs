package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerActivity;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerService;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerType;
import com.magmaguy.elitemobs.api.power.ElitePowerActionHandler;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** EliteMobs implementation behind the classloader-safe Lua power interface. */
final class EliteLuaPowerServiceImpl implements EliteLuaPowerService, Listener {
    private final EliteLuaPowerCatalog catalog = new EliteLuaPowerCatalog();
    private final Map<NamespacedKey, LinkedHashMap<NamespacedKey, ActionRegistration>> actionHandlers =
            new LinkedHashMap<>();
    private boolean closed;

    @Override
    public EliteLuaPowerProgram registerLuaPower(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source,
            EliteLuaPowerType type,
            String effect) {
        requireAvailable();
        requireServerThread();
        requireEnabledOwner(owner);
        return catalog.register(owner, key, revision, source, type, effect);
    }

    @Override
    public List<EliteLuaPowerProgram> setPowers(
            Plugin owner,
            EliteEntity eliteEntity,
            List<NamespacedKey> orderedPowerKeys) {
        requireAvailable();
        requireServerThread();
        requireEnabledOwner(owner);
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        if (EntityTracker.getEliteMobEntities().get(eliteEntity.getEliteUUID()) != eliteEntity) {
            throw new IllegalStateException("Elite actor is not registered with EliteMobs");
        }
        return setPowersInternal(owner, eliteEntity, orderedPowerKeys, false);
    }

    /** Attaches an exact power set while a native Mind actor is still private to its transaction. */
    List<EliteLuaPowerProgram> setPreparedPowers(
            Plugin owner,
            EliteEntity eliteEntity,
            List<NamespacedKey> orderedPowerKeys) {
        requireAvailable();
        requireServerThread();
        requireEnabledOwner(owner);
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        return setPowersInternal(owner, eliteEntity, orderedPowerKeys, true);
    }

    @Override
    public List<EliteLuaPowerProgram> inspectPowers(EliteEntity eliteEntity) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding == null) return List.of();
        if (!binding.matchesAttachmentOrder(eliteEntity.copyElitePowersInAttachmentOrder())) {
            clearBindingQuietly(eliteEntity, "managed power collection drift", true);
            return List.of();
        }
        return binding.descriptors();
    }

    @Override
    public List<EliteLuaPowerActivity> inspectPowerActivity(EliteEntity eliteEntity) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding == null) return List.of();
        if (!binding.matchesAttachmentOrder(eliteEntity.copyElitePowersInAttachmentOrder())) {
            clearBindingQuietly(eliteEntity, "managed power collection drift", true);
            return List.of();
        }
        return binding.activity();
    }

    @Override
    public void registerActionHandler(
            Plugin owner,
            NamespacedKey powerKey,
            NamespacedKey actionKey,
            ElitePowerActionHandler handler) {
        requireAvailable();
        requireServerThread();
        requireEnabledOwner(owner);
        Objects.requireNonNull(powerKey, "powerKey");
        Objects.requireNonNull(actionKey, "actionKey");
        Objects.requireNonNull(handler, "handler");
        catalog.resolveOwned(owner, List.of(powerKey));
        requireOwnerNamespace(owner, actionKey, "Power action");

        LinkedHashMap<NamespacedKey, ActionRegistration> handlers =
                actionHandlers.computeIfAbsent(actionKey, ignored -> new LinkedHashMap<>());
        for (ActionRegistration registration : handlers.values()) {
            if (registration.owner() != owner) {
                throw new IllegalArgumentException("Power action " + actionKey
                        + " is already owned by another plugin");
            }
        }
        ActionRegistration previous = handlers.get(powerKey);
        if (previous != null) {
            if (previous.owner() == owner
                    && previous.powerKey().equals(powerKey)
                    && previous.handler() == handler) return;
            throw new IllegalArgumentException("Power action " + actionKey
                    + " is already registered for " + powerKey);
        }
        handlers.put(powerKey, new ActionRegistration(owner, powerKey, handler));
    }

    @Override
    public void unregisterOwner(Plugin owner) {
        requireAvailable();
        requireServerThread();
        unregisterOwnerInternal(Objects.requireNonNull(owner, "owner"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobRemove(EliteMobRemoveEvent event) {
        clearBindingQuietly(event.getEliteMobEntity(), "removed elite", false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (closed || event.getPlugin() == MetadataHandler.PLUGIN) return;
        unregisterOwnerInternal(event.getPlugin());
    }

    void shutdown() {
        if (closed) return;
        requireServerThread();
        closed = true;
        for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            clearBindingQuietly(eliteEntity, "EliteMobs shutdown", true);
        }
        actionHandlers.clear();
        catalog.clear();
    }

    ElitePowerActionResult dispatchMindAction(
            UUID logicalOwner,
            UUID physicalEntity,
            NamespacedKey actionKey,
            Map<String, Object> payload,
            long gameTick,
            long generation) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(logicalOwner, "logicalOwner");
        Objects.requireNonNull(physicalEntity, "physicalEntity");
        Objects.requireNonNull(actionKey, "actionKey");
        Objects.requireNonNull(payload, "payload");

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntities().get(logicalOwner);
        if (eliteEntity == null
                || eliteEntity.getLivingEntity() == null
                || !eliteEntity.getLivingEntity().getUniqueId().equals(physicalEntity)) {
            return ElitePowerActionResult.REJECTED;
        }

        return dispatchIfPowerActive(eliteEntity, () -> dispatchActiveMindAction(
                eliteEntity, actionKey, payload, gameTick, generation));
    }

    static ElitePowerActionResult dispatchIfPowerActive(
            EliteEntity eliteEntity,
            Supplier<ElitePowerActionResult> activeDispatch) {
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        Objects.requireNonNull(activeDispatch, "activeDispatch");
        if (eliteEntity.getPowerSuppression().isSuppressed()) {
            return ElitePowerActionResult.REJECTED;
        }
        return Objects.requireNonNull(activeDispatch.get(), "Mind action dispatch result");
    }

    private ElitePowerActionResult dispatchActiveMindAction(
            EliteEntity eliteEntity,
            NamespacedKey actionKey,
            Map<String, Object> payload,
            long gameTick,
            long generation) {
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding == null) return ElitePowerActionResult.REJECTED;
        if (!binding.matchesAttachmentOrder(eliteEntity.copyElitePowersInAttachmentOrder())) {
            clearBindingQuietly(eliteEntity, "managed power collection drift", true);
            return ElitePowerActionResult.REJECTED;
        }
        EliteMindBinding mindBinding = eliteEntity.getEliteMindBinding();
        if (mindBinding == null) {
            return ElitePowerActionResult.REJECTED;
        }
        Plugin owner = mindBinding.ownerForGeneration(generation);
        if (owner == null || !owner.isEnabled() || !binding.isOwnedBy(owner)) {
            return ElitePowerActionResult.REJECTED;
        }

        ElitePowerActionRequest request = new ElitePowerActionRequest(
                eliteEntity,
                actionKey,
                payload,
                gameTick,
                generation);
        ElitePowerActionResult luaResult = binding.dispatchMindAction(request);
        if (luaResult == ElitePowerActionResult.ACCEPTED) {
            return luaResult;
        }
        boolean deferred = luaResult == ElitePowerActionResult.DEFERRED;

        Map<NamespacedKey, ActionRegistration> handlers = actionHandlers.get(actionKey);
        if (handlers == null) {
            return deferred ? ElitePowerActionResult.DEFERRED : ElitePowerActionResult.REJECTED;
        }
        for (NamespacedKey attachedPower : binding.powerKeys()) {
            ActionRegistration registration = handlers.get(attachedPower);
            if (registration == null || registration.owner() != owner) continue;
            try {
                ElitePowerActionResult result = Objects.requireNonNull(
                        registration.handler().handle(request),
                        "Power action handler result");
                if (result == ElitePowerActionResult.ACCEPTED) return result;
                deferred |= result == ElitePowerActionResult.DEFERRED;
            } catch (RuntimeException exception) {
                Logger.warn("Power action handler for " + actionKey + " on " + attachedPower
                        + " failed: " + exception.getMessage());
            }
        }
        return deferred ? ElitePowerActionResult.DEFERRED : ElitePowerActionResult.REJECTED;
    }

    void suspendMindActorPowers(EliteEntity eliteEntity) {
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding != null) {
            try {
                binding.stopRuntimes();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to stop Lua powers during native body unload: "
                        + exception.getMessage());
            }
        } else eliteEntity.suspendUnmanagedPowerRuntimes();
        eliteEntity.suspendPowerStances();
    }

    void resumeMindActorPowers(EliteEntity eliteEntity) {
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding != null) binding.restartRuntimes();
        else eliteEntity.resumeUnmanagedPowerRuntimes();
        eliteEntity.refreshPowerStances();
    }

    void setMindActorPowersPaused(EliteEntity eliteEntity, boolean paused) {
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding != null) binding.setPaused(paused);
        else if (paused) eliteEntity.suspendUnmanagedPowerRuntimes();
        else eliteEntity.resumeUnmanagedPowerRuntimes();
        if (paused) eliteEntity.suspendPowerStances();
        else eliteEntity.refreshPowerStances();
    }

    private List<EliteLuaPowerProgram> setPowersInternal(
            Plugin owner,
            EliteEntity eliteEntity,
            List<NamespacedKey> orderedPowerKeys,
            boolean preparedActor) {
        Objects.requireNonNull(orderedPowerKeys, "orderedPowerKeys");
        if (eliteEntity.getLivingEntity() == null || !eliteEntity.getLivingEntity().isValid()) {
            throw new IllegalStateException("Elite actor has no loaded living entity");
        }
        if (preparedActor && !eliteEntity.isPreparedMindSpawn()) {
            throw new IllegalStateException("Elite actor is not in native Mind preparation");
        }

        List<EliteLuaPowerCatalog.Entry> entries = catalog.resolveOwned(owner, orderedPowerKeys);
        List<ElitePower> previousPowers = eliteEntity.copyElitePowersInAttachmentOrder();
        EliteLuaPowerBinding previousBinding = eliteEntity.getEliteLuaPowerBinding();
        requireReplaceable(owner, eliteEntity.getEliteMindBinding(), previousBinding, previousPowers);

        ArrayList<EliteLuaPowerBinding.AttachedPower> attached = new ArrayList<>(entries.size());
        try {
            for (EliteLuaPowerCatalog.Entry entry : entries) {
                LuaElitePower power = entry.registration().newPower();
                power.setOwnerEntity(eliteEntity);
                power.startRuntimeOrThrow();
                attached.add(new EliteLuaPowerBinding.AttachedPower(entry, power));
            }
        } catch (RuntimeException exception) {
            closeNewRuntimes(attached, exception);
            throw exception;
        }

        EliteLuaPowerBinding replacement = attached.isEmpty()
                ? null
                : new EliteLuaPowerBinding(owner, attached);
        try {
            eliteEntity.replaceElitePowersInOrder(
                    replacement == null ? List.of() : replacement.powers());
            eliteEntity.setEliteLuaPowerBinding(replacement);
            eliteEntity.refreshPowerStances();
        } catch (RuntimeException exception) {
            if (replacement != null) {
                try {
                    replacement.close();
                } catch (RuntimeException cleanupFailure) {
                    exception.addSuppressed(cleanupFailure);
                }
            }
            restorePreviousState(eliteEntity, previousPowers, previousBinding, exception);
            throw exception;
        }

        if (preparedActor && replacement != null) {
            eliteEntity.addPreparedMindSpawnCleanup(
                    () -> detachIfCurrent(eliteEntity, replacement, false));
        }
        if (previousBinding != null && previousBinding != replacement) {
            closeBindingQuietly(previousBinding, "replaced Lua power loadout");
        } else if (previousBinding == null) {
            closePowerRuntimesQuietly(previousPowers, "replaced unmanaged power loadout");
        }
        return replacement == null ? List.of() : replacement.descriptors();
    }

    private static void restorePreviousState(
            EliteEntity eliteEntity,
            List<ElitePower> previousPowers,
            EliteLuaPowerBinding previousBinding,
            RuntimeException originalFailure) {
        try {
            eliteEntity.replaceElitePowersInOrder(previousPowers);
            eliteEntity.setEliteLuaPowerBinding(previousBinding);
            eliteEntity.refreshPowerStances();
        } catch (RuntimeException rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
        }
    }

    private static void requireReplaceable(
            Plugin owner,
            EliteMindBinding mindBinding,
            EliteLuaPowerBinding binding,
            List<ElitePower> currentPowers) {
        if (mindBinding != null && !mindBinding.isOwnedBy(owner)) {
            throw new IllegalArgumentException(
                    "A plugin cannot replace another plugin's Mind actor power loadout");
        }
        if (binding == null) return;
        if (!binding.isOwnedBy(owner)) {
            throw new IllegalArgumentException(
                    "A plugin cannot replace another plugin's managed power loadout");
        }
        if (!binding.matchesAttachmentOrder(currentPowers)) {
            throw new IllegalStateException(
                    "Elite actor power collection changed outside the Lua power service");
        }
    }

    private void unregisterOwnerInternal(Plugin owner) {
        actionHandlers.values().forEach(handlers ->
                handlers.values().removeIf(registration -> registration.owner() == owner));
        actionHandlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        catalog.unregisterOwner(owner);
        for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
            if (binding != null && binding.isOwnedBy(owner)) {
                clearBindingQuietly(eliteEntity, "owner plugin disable", true);
            }
        }
    }

    private static void clearBindingQuietly(
            EliteEntity eliteEntity,
            String context,
            boolean refreshStances) {
        EliteLuaPowerBinding binding = eliteEntity.getEliteLuaPowerBinding();
        if (binding == null) return;
        try {
            detachIfCurrent(eliteEntity, binding, refreshStances);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to clear Lua power binding during " + context + ": "
                    + exception.getMessage());
        }
    }

    private static void detachIfCurrent(
            EliteEntity eliteEntity,
            EliteLuaPowerBinding expected,
            boolean refreshStances) {
        if (eliteEntity.getEliteLuaPowerBinding() != expected) return;
        eliteEntity.setEliteLuaPowerBinding(null);
        RuntimeException failure = null;
        try {
            expected.close();
        } catch (RuntimeException exception) {
            failure = exception;
        } finally {
            try {
                eliteEntity.replaceElitePowersInOrder(List.of());
                if (refreshStances && eliteEntity.getLivingEntity() != null) {
                    eliteEntity.refreshPowerStances();
                }
            } catch (RuntimeException cleanupFailure) {
                if (failure == null) failure = cleanupFailure;
                else failure.addSuppressed(cleanupFailure);
            }
        }
        if (failure != null) throw failure;
    }

    private static void closeNewRuntimes(
            List<EliteLuaPowerBinding.AttachedPower> attachedPowers,
            RuntimeException primaryFailure) {
        for (int index = attachedPowers.size() - 1; index >= 0; index--) {
            try {
                attachedPowers.get(index).power().closeRuntime();
            } catch (RuntimeException cleanupFailure) {
                primaryFailure.addSuppressed(cleanupFailure);
            }
        }
    }

    private static void closeBindingQuietly(EliteLuaPowerBinding binding, String context) {
        try {
            binding.close();
        } catch (RuntimeException exception) {
            Logger.warn("Failed to close " + context + ": " + exception.getMessage());
        }
    }

    private static void closePowerRuntimesQuietly(List<ElitePower> powers, String context) {
        for (ElitePower power : powers) {
            try {
                power.closeRuntime();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to close " + context + " for " + power.getFileName()
                        + ": " + exception.getMessage());
            }
        }
    }

    private void requireAvailable() {
        if (closed) throw new IllegalStateException("EliteMobs Lua power service is shut down");
    }

    private static void requireEnabledOwner(Plugin owner) {
        Objects.requireNonNull(owner, "owner");
        if (!owner.isEnabled()) throw new IllegalStateException("Lua power owner plugin is not enabled");
    }

    private static void requireOwnerNamespace(Plugin owner, NamespacedKey key, String subject) {
        String ownerNamespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
        if (!ownerNamespace.equals(key.getNamespace())) {
            throw new IllegalArgumentException(subject + " key " + key
                    + " is outside plugin namespace " + ownerNamespace);
        }
    }

    private static void requireServerThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("EliteMobs Lua power operations must run on the server thread");
        }
    }

    private record ActionRegistration(
            Plugin owner,
            NamespacedKey powerKey,
            ElitePowerActionHandler handler) {
    }
}
