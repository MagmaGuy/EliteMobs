package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.mind.EliteMindService;
import com.magmaguy.elitemobs.api.power.ElitePowerActionPosition;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.easyminecraftgoals.NMSAdapter;
import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.magmacore.ai.MindFailure;
import com.magmaguy.magmacore.ai.MindHost;
import com.magmaguy.magmacore.ai.MindActionContext;
import com.magmaguy.magmacore.ai.MindActionRequest;
import com.magmaguy.magmacore.ai.MindActionResult;
import com.magmaguy.magmacore.ai.MindPosition;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/** Internal lifecycle owner for EliteMobs' native Mind interface. */
public final class EliteMindServiceModule {
    private static EliteMindServiceImpl implementation;
    private static NMSAdapter adapter;
    private static BukkitTask mindTickTask;

    private EliteMindServiceModule() {
    }

    /** Ordinary YAML chooses a behavior; it does not introduce a second boss spawn path. */
    public static Function<Consumer<LivingEntity>, LivingEntity> behaviorBodyFactory(
            CustomBossEntity actor, Location location) {
        var fields = actor.getCustomBossesConfigFields();
        if (!fields.isAi() || fields.isNeutral()) return null;
        String filename = fields.getBehavior();
        if (filename == null) {
            var properties = EliteMobProperties.getPluginData(fields.getEntityType());
            filename = properties == null ? null : properties.getBehavior();
        }
        if (filename == null || filename.isBlank() || filename.equalsIgnoreCase("native")) return null;
        EliteMindServiceImpl current = requireImplementation();
        String reference = filename.trim();
        current.validateBehavior(reference);
        return configure -> current.spawnBehaviorBody(actor, location, reference, configure);
    }

    public static EliteEntity spawnBehaviorElite(Location location, EntityType type, int level,
            CreatureSpawnEvent.SpawnReason reason, Set<PowersConfigFields> powers) {
        return requireImplementation().spawnBehaviorElite(location, type, level, reason, powers);
    }

    public static void detachBehavior(EliteEntity actor) {
        EliteMindServiceImpl.detachBehavior(actor);
    }

    private static EliteMindServiceImpl requireImplementation() {
        if (implementation == null) throw new IllegalStateException("EliteMobs behavior service is unavailable");
        return implementation;
    }

    public static void initialize() {
        if (implementation != null) return;
        NMSAdapter selectedAdapter = NMSManager.getAdapter();
        if (selectedAdapter == null) return;
        EliteLuaPowerServiceImpl luaPowerService = EliteLuaPowerServiceModule.implementation();
        if (luaPowerService == null) {
            throw new IllegalStateException("Lua power service must initialize before the Mind service");
        }

        MindHost mindHost;
        try {
            mindHost = selectedAdapter.createMindHost(
                    new NamespacedKey(MetadataHandler.PLUGIN, "native_mind"),
                    EliteMindServiceModule::reportFailure,
                    EliteMindServiceModule::dispatchAction);
        } catch (UnsupportedOperationException exception) {
            Logger.info("Native Mind interface is unavailable on this Minecraft version.");
            return;
        }
        EliteMindServiceImpl candidate = null;
        try {
            candidate = new EliteMindServiceImpl(mindHost, luaPowerService);
            adapter = selectedAdapter;
            implementation = candidate;
            Bukkit.getPluginManager().registerEvents(candidate, MetadataHandler.PLUGIN);
            Bukkit.getServicesManager().register(
                    EliteMindService.class,
                    candidate,
                    MetadataHandler.PLUGIN,
                    ServicePriority.Normal);
            candidate.reconcileLoadedBodies();
            mindTickTask = Bukkit.getScheduler().runTaskTimer(
                    MetadataHandler.PLUGIN,
                    mindHost::tick,
                    1L,
                    1L);
        } catch (RuntimeException | Error failure) {
            BukkitTask failedTickTask = mindTickTask;
            mindTickTask = null;
            if (failedTickTask != null) failedTickTask.cancel();
            if (candidate != null) {
                try {
                    Bukkit.getServicesManager().unregister(EliteMindService.class, candidate);
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
                try {
                    HandlerList.unregisterAll(candidate);
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
                try {
                    candidate.shutdown();
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            implementation = null;
            adapter = null;
            try {
                selectedAdapter.shutdownMindHost();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    public static void shutdown() {
        EliteMindServiceImpl current = implementation;
        NMSAdapter selectedAdapter = adapter;
        BukkitTask currentTickTask = mindTickTask;
        implementation = null;
        adapter = null;
        mindTickTask = null;
        if (currentTickTask != null) currentTickTask.cancel();
        if (current != null) {
            try {
                Bukkit.getServicesManager().unregister(EliteMindService.class, current);
            } catch (RuntimeException exception) {
                Logger.warn("Failed to unregister the EliteMobs Mind service: "
                        + exception.getMessage());
            }
            try {
                HandlerList.unregisterAll(current);
            } catch (RuntimeException exception) {
                Logger.warn("Failed to unregister EliteMobs Mind listeners: "
                        + exception.getMessage());
            }
            try {
                current.shutdown();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to finish EliteMobs Mind service shutdown: "
                        + exception.getMessage());
            }
        }
        if (selectedAdapter != null) {
            try {
                selectedAdapter.shutdownMindHost();
            } catch (RuntimeException exception) {
                Logger.warn("Failed to shut down the native EliteMobs Mind host: "
                        + exception.getMessage());
            }
        }
    }

    /** Internal EntityTracker hook that keeps native sessions alive across chunk serialization. */
    public static boolean suspendForChunkUnload(EliteEntity eliteEntity, LivingEntity removedBody) {
        EliteMindServiceImpl current = implementation;
        return current != null && current.suspendForChunkUnload(eliteEntity, removedBody);
    }

    /** Internal natural-spawn hook used before historical probabilistic conversion. */
    public static boolean replaceNaturalSpawn(
            CreatureSpawnEvent event,
            int suggestedLevel) {
        EliteMindServiceImpl current = implementation;
        return current != null && current.replaceNaturalSpawn(event, suggestedLevel);
    }

    /** Internal Java-program actor seam. Public plugin consumers continue to use EliteMindService. */
    public static Optional<EliteEntity> spawnInternal(InternalMindActorSpawnRequest request) {
        EliteMindServiceImpl current = implementation;
        return current == null ? Optional.empty() : Optional.of(current.spawnInternal(request));
    }

    /** Idempotent terminal cleanup for an actor created through {@link #spawnInternal}. */
    public static boolean clearInternal(EliteEntity actor) {
        EliteMindServiceImpl current = implementation;
        return current != null && current.clearProgram(actor);
    }

    private static void reportFailure(MindFailure failure) {
        String cause = failure.cause() == null
                ? ""
                : " (" + failure.cause().getClass().getSimpleName() + ": "
                + failure.cause().getMessage() + ")";
        Logger.warn("Mind " + failure.programIdentifier() + " callback "
                + failure.callbackIdentifier() + " failed with " + failure.kind() + cause);
    }

    private static MindActionResult dispatchAction(
            MindActionContext context,
            MindActionRequest request) {
        EliteLuaPowerServiceImpl luaPowerService = EliteLuaPowerServiceModule.implementation();
        if (luaPowerService == null) return MindActionResult.REJECTED;
        NamespacedKey actionKey = NamespacedKey.fromString(request.identifier());
        if (actionKey == null) return MindActionResult.REJECTED;
        ElitePowerActionResult result = luaPowerService.dispatchMindAction(
                context.logicalOwner(),
                context.body().entity().getUniqueId(),
                actionKey,
                toPublicPayload(request.payload()),
                context.gameTick(),
                context.generation());
        return MindActionResult.valueOf(result.name());
    }

    private static Map<String, Object> toPublicPayload(Map<String, ?> nativePayload) {
        LinkedHashMap<String, Object> converted = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : nativePayload.entrySet()) {
            Object value = entry.getValue();
            converted.put(entry.getKey(), value instanceof MindPosition position
                    ? new ElitePowerActionPosition(
                            position.world(), position.x(), position.y(), position.z())
                    : value);
        }
        return converted;
    }
}
