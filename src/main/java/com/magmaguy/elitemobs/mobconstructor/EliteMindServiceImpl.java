package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyCapabilities;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.api.mind.EliteMindModule;
import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.elitemobs.api.mind.EliteMindService;
import com.magmaguy.elitemobs.api.mind.EliteMindSnapshot;
import com.magmaguy.elitemobs.api.mind.EliteMindSpawnRequest;
import com.magmaguy.elitemobs.api.mind.EliteMindTransferPolicy;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnContext;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnProvider;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnReplacement;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnSuppression;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.magmacore.ai.MindBodyRehydration;
import com.magmaguy.magmacore.ai.MindBodyCapabilities;
import com.magmaguy.magmacore.ai.MindBodyLocomotion;
import com.magmaguy.magmacore.ai.MindBodyProfile;
import com.magmaguy.magmacore.ai.MindHandle;
import com.magmaguy.magmacore.ai.MindHost;
import com.magmaguy.magmacore.ai.MindProgram;
import com.magmaguy.magmacore.ai.MobBody;
import com.magmaguy.magmacore.ai.StateTransfer;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** EliteMobs implementation behind the classloader-safe Mind interface. */
final class EliteMindServiceImpl implements EliteMindService, Listener {
    private final EliteMindCatalog catalog = new EliteMindCatalog();
    private final EliteNaturalSpawnProviderRegistry naturalSpawnProviders =
            new EliteNaturalSpawnProviderRegistry();
    private final MindHost mindHost;
    private final EliteLuaPowerServiceImpl luaPowerService;
    private final EliteMindBodyCapabilities bodyCapabilities;
    private boolean closed;

    EliteMindServiceImpl(MindHost mindHost, EliteLuaPowerServiceImpl luaPowerService) {
        this.mindHost = Objects.requireNonNull(mindHost, "mindHost");
        this.luaPowerService = Objects.requireNonNull(luaPowerService, "luaPowerService");
        this.bodyCapabilities = toPublicCapabilities(mindHost.bodyCapabilities());
    }

    @Override
    public EliteMindModule registerLuaModule(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(owner, "owner");
        if (!owner.isEnabled()) throw new IllegalStateException("Mind owner plugin is not enabled");
        return catalog.registerModule(owner, key, revision, source);
    }

    @Override
    public EliteMindProgram registerLuaProgram(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(owner, "owner");
        if (!owner.isEnabled()) throw new IllegalStateException("Mind owner plugin is not enabled");
        return catalog.register(owner, key, revision, source);
    }

    @Override
    public void unregisterOwner(Plugin owner) {
        requireAvailable();
        requireServerThread();
        unregisterOwnerInternal(Objects.requireNonNull(owner, "owner"));
    }

    @Override
    public EliteMindBodyCapabilities bodyCapabilities() {
        requireAvailable();
        return bodyCapabilities;
    }

    @Override
    public EliteEntity spawn(EliteMindSpawnRequest request) {
        return spawn(request, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    private EliteEntity spawn(
            EliteMindSpawnRequest request,
            CreatureSpawnEvent.SpawnReason spawnReason) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(spawnReason, "spawnReason");
        if (!request.owner().isEnabled()) throw new IllegalStateException("Mind owner plugin is not enabled");

        EliteMindCatalog.Entry entry = catalog.require(request.programKey());
        if (entry.owner() != request.owner()) {
            throw new IllegalArgumentException("Plugin " + request.owner().getName()
                    + " does not own mind program " + request.programKey());
        }

        return spawnResolved(
                request.owner(),
                entry,
                request.location(),
                request.level(),
                request.persistent(),
                request.bodyProfile(),
                request.powerLoadout(),
                spawnReason,
                ignored -> {
                });
    }

    EliteEntity spawnInternal(InternalMindActorSpawnRequest request) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(request, "request");
        if (!request.systemOwner().isEnabled()) {
            throw new IllegalStateException("Mind owner plugin is not enabled");
        }
        NamespacedKey key = NamespacedKey.fromString(request.program().identifier());
        if (key == null) throw new IllegalArgumentException("Internal Mind program key is invalid");
        EliteMindProgramEntry entry = new InternalProgramEntry(
                request.systemOwner(),
                new EliteMindProgram(key, request.program().revision()),
                request.program());
        return spawnResolved(
                request.systemOwner(),
                entry,
                request.location(),
                request.level(),
                false,
                request.bodyProfile(),
                com.magmaguy.elitemobs.api.mind.EliteMindPowerLoadout.exact(java.util.List.of()),
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                eliteEntity -> {
                    eliteEntity.addCustomData(
                            new NamespacedKey(request.systemOwner(), "native_mind_player_owner"),
                            request.playerOwnerId());
                    request.initializer().accept(eliteEntity);
                });
    }

    private EliteEntity spawnResolved(
            Plugin owner,
            EliteMindProgramEntry entry,
            org.bukkit.Location location,
            int level,
            boolean persistent,
            EliteMindBodyProfile bodyProfile,
            com.magmaguy.elitemobs.api.mind.EliteMindPowerLoadout powerLoadout,
            CreatureSpawnEvent.SpawnReason spawnReason,
            Consumer<EliteEntity> initializer) {
        MobBody body = null;
        EliteEntity eliteEntity = null;
        MindHandle handle = null;
        EliteMindBinding binding = null;
        boolean committed = false;
        try {
            bodyCapabilities.validate(bodyProfile);
            body = mindHost.spawnBody(location, toNativeProfile(bodyProfile));
            CrashFix.persistentTracker(body.entity());
            eliteEntity = new EliteEntity();
            eliteEntity.prepareMindActor(
                    body.entity(),
                    level,
                    spawnReason,
                    powerLoadout.randomized());
            if (!powerLoadout.randomized()) {
                luaPowerService.setPreparedPowers(
                        owner,
                        eliteEntity,
                        powerLoadout.powerKeys());
            }
            eliteEntity.setPersistent(persistent);
            initializer.accept(eliteEntity);

            handle = mindHost.open(eliteEntity.getEliteUUID(), entry.instantiate());
            binding = new EliteMindBinding(
                    entry,
                    handle,
                    body);
            eliteEntity.setEliteMindBinding(binding);
            if (!eliteEntity.commitPreparedMindSpawn()) {
                throw new IllegalStateException("EliteMobSpawnEvent rejected the native mind actor");
            }
            committed = true;
            return eliteEntity;
        } finally {
            if (!committed) {
                rollbackSpawn(eliteEntity, body, handle, binding);
            }
        }
    }

    @Override
    public void registerNaturalSpawnProvider(
            Plugin owner,
            EliteNaturalSpawnProvider provider) {
        requireAvailable();
        requireServerThread();
        naturalSpawnProviders.register(owner, provider);
    }

    /** Returns true when an owner claimed and terminally handled this vanilla carrier. */
    boolean replaceNaturalSpawn(CreatureSpawnEvent event, int suggestedLevel) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(event, "event");
        EliteNaturalSpawnContext context = new EliteNaturalSpawnContext(
                event.getEntity(),
                event.getSpawnReason(),
                suggestedLevel);
        EliteNaturalSpawnProviderRegistry.Selection selection = naturalSpawnProviders
                .select(context)
                .orElse(null);
        if (selection == null) return false;
        if (selection.claim() instanceof EliteNaturalSpawnSuppression) return true;
        EliteNaturalSpawnReplacement replacement =
                (EliteNaturalSpawnReplacement) selection.claim();
        EliteMindSpawnRequest requested = replacement.request();
        if (requested.owner() != selection.owner()) {
            throw new IllegalArgumentException(
                    "Natural-spawn claim owner does not own its Mind request");
        }
        if (!requested.bodyProfile().carrierType().equals(
                event.getEntityType().getKeyOrThrow())) {
            throw new IllegalArgumentException(
                    "Natural-spawn replacement carrier "
                            + requested.bodyProfile().carrierType()
                            + " does not match source " + event.getEntityType().getKeyOrThrow());
        }
        EliteMindSpawnRequest atSource = new EliteMindSpawnRequest(
                requested.owner(),
                requested.programKey(),
                event.getLocation(),
                requested.level(),
                requested.persistent(),
                requested.bodyProfile(),
                requested.powerLoadout());
        EliteEntity actor = spawn(atSource, event.getSpawnReason());
        try {
            replacement.afterSpawn().accept(actor);
        } catch (RuntimeException | Error failure) {
            try {
                clearProgram(actor);
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
        return true;
    }

    @Override
    public EliteMindSnapshot setProgram(
            EliteEntity eliteEntity,
            NamespacedKey key,
            EliteMindTransferPolicy transferPolicy) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        EliteMindCatalog.Entry entry = catalog.require(key);
        EliteMindBinding binding = eliteEntity.getEliteMindBinding();
        if (binding == null) {
            throw new IllegalStateException("Elite actor is not backed by a MagmaCore native mind body");
        }
        binding.replace(entry, toNativeTransfer(transferPolicy));
        return binding.snapshot(eliteEntity);
    }

    @Override
    public EliteMindSnapshot setPaused(EliteEntity eliteEntity, boolean paused) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        EliteMindBinding binding = eliteEntity.getEliteMindBinding();
        if (binding == null) {
            throw new IllegalStateException("Elite actor is not backed by a MagmaCore native mind body");
        }
        EliteMindSnapshot before = binding.snapshot(eliteEntity);
        if (before.paused() == paused) return before;

        binding.setPaused(paused);
        try {
            luaPowerService.setMindActorPowersPaused(eliteEntity, paused);
        } catch (RuntimeException failure) {
            try {
                binding.setPaused(before.paused());
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
        return binding.snapshot(eliteEntity);
    }

    @Override
    public boolean clearProgram(EliteEntity eliteEntity) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        if (eliteEntity.getEliteMindBinding() == null) return false;
        try {
            eliteEntity.terminateMindActor(RemovalReason.OTHER);
        } finally {
            clearProgramQuietly(eliteEntity, "explicit program clear");
        }
        return true;
    }

    @Override
    public Optional<EliteMindSnapshot> inspect(EliteEntity eliteEntity) {
        requireAvailable();
        requireServerThread();
        Objects.requireNonNull(eliteEntity, "eliteEntity");
        EliteMindBinding binding = eliteEntity.getEliteMindBinding();
        return binding == null ? Optional.empty() : Optional.of(binding.snapshot(eliteEntity));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobRemove(EliteMobRemoveEvent event) {
        clearProgramQuietly(event.getEliteMobEntity(), "removed elite");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if (closed || event.getPlugin() == MetadataHandler.PLUGIN) return;
        unregisterOwnerInternal(event.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (closed) return;
        for (Entity entity : event.getEntities()) {
            if (entity instanceof LivingEntity livingEntity) rehydrate(livingEntity);
        }
    }

    void shutdown() {
        if (closed) return;
        requireServerThread();
        closed = true;
        for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            clearProgramQuietly(eliteEntity, "EliteMobs shutdown");
        }
        catalog.clear();
        naturalSpawnProviders.clear();
    }

    /** Reconciles carriers whose chunk-load event occurred before EliteMobs finished initializing. */
    void reconcileLoadedBodies() {
        requireAvailable();
        requireServerThread();
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity livingEntity : world.getLivingEntities()) {
                rehydrate(livingEntity);
            }
        }
    }

    boolean suspendForChunkUnload(EliteEntity eliteEntity, LivingEntity removedBody) {
        EliteMindBinding binding = eliteEntity.getEliteMindBinding();
        if (closed || binding == null) return false;
        if (removedBody != null && removedBody.isDead()) return false;
        boolean suspended = eliteEntity.suspendMindBodyForChunkUnload(removedBody);
        if (suspended) {
            binding.suspendBody();
            luaPowerService.suspendMindActorPowers(eliteEntity);
        }
        return suspended;
    }

    private void rehydrate(LivingEntity loadedCarrier) {
        Optional<MindBodyRehydration> recovered;
        try {
            recovered = mindHost.rehydrateBody(loadedCarrier);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to recover a native mind carrier: " + exception.getMessage());
            return;
        }
        if (recovered.isEmpty()) return;

        MindBodyRehydration rehydration = recovered.get();
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntities().get(rehydration.logicalOwner());
        if (eliteEntity == null || eliteEntity.getEliteMindBinding() == null) {
            rehydration.body().entity().remove();
            return;
        }

        try {
            eliteEntity.getEliteMindBinding().attachBody(rehydration.body());
            eliteEntity.reattachMindBody(rehydration.body().entity());
            luaPowerService.resumeMindActorPowers(eliteEntity);
        } catch (RuntimeException exception) {
            luaPowerService.suspendMindActorPowers(eliteEntity);
            try {
                eliteEntity.terminateMindActor(RemovalReason.ENTITY_REPLACEMENT);
            } catch (RuntimeException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            } finally {
                clearProgramQuietly(eliteEntity, "failed native mind reattachment");
            }
            rehydration.body().entity().remove();
            Logger.warn("Failed to reattach native mind body for elite " + eliteEntity.getEliteUUID()
                    + ": " + exception.getMessage());
        }
    }

    private void unregisterOwnerInternal(Plugin owner) {
        catalog.unregisterOwner(owner);
        naturalSpawnProviders.unregister(owner);
        for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            EliteMindBinding binding = eliteEntity.getEliteMindBinding();
            if (binding != null && binding.isOwnedBy(owner)) {
                removeOwnedActor(eliteEntity);
            }
        }
    }

    private static void removeOwnedActor(EliteEntity eliteEntity) {
        try {
            eliteEntity.terminateMindActor(RemovalReason.OTHER);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to remove native mind actor during owner plugin disable: "
                    + exception.getMessage());
        } finally {
            clearProgramQuietly(eliteEntity, "owner plugin disable");
        }
    }

    private static boolean clearProgramInternal(EliteEntity eliteEntity) {
        EliteMindBinding binding = eliteEntity.getEliteMindBinding();
        if (binding == null) return false;
        eliteEntity.setEliteMindBinding(null);
        binding.close();
        return true;
    }

    private static void clearProgramQuietly(EliteEntity eliteEntity, String context) {
        try {
            clearProgramInternal(eliteEntity);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to clear native mind binding during " + context + ": "
                    + exception.getMessage());
        }
    }

    private static void rollbackSpawn(
            EliteEntity eliteEntity,
            MobBody body,
            MindHandle handle,
            EliteMindBinding binding) {
        boolean published = eliteEntity != null
                && EntityTracker.getEliteMobEntities().get(eliteEntity.getEliteUUID()) == eliteEntity;
        if (published) {
            try {
                eliteEntity.terminateMindActor(RemovalReason.ENTITY_REPLACEMENT);
            } catch (RuntimeException cleanupFailure) {
                Logger.warn("Failed to remove a rejected native mind actor: "
                        + cleanupFailure.getMessage());
            } finally {
                clearProgramQuietly(eliteEntity, "native mind spawn rollback");
            }
        } else if (eliteEntity != null && eliteEntity.getEliteMindBinding() != null) {
            clearProgramQuietly(eliteEntity, "native mind spawn rollback");
        } else if (binding != null) {
            try {
                binding.close();
            } catch (RuntimeException cleanupFailure) {
                Logger.warn("Failed to close a rejected native mind binding: "
                        + cleanupFailure.getMessage());
            }
        } else if (handle != null) {
            try {
                handle.close();
            } catch (RuntimeException cleanupFailure) {
                Logger.warn("Failed to close a rejected native mind session: "
                        + cleanupFailure.getMessage());
            }
        }

        if (!published && eliteEntity != null) {
            try {
                eliteEntity.rollbackPreparedMindSpawn();
            } catch (RuntimeException cleanupFailure) {
                Logger.warn("Failed to roll back a prepared native mind actor: "
                        + cleanupFailure.getMessage());
            }
        }

        if (body != null && body.entity().isValid()) {
            try {
                body.entity().remove();
            } catch (RuntimeException cleanupFailure) {
                Logger.warn("Failed to remove a rejected native mind body: "
                        + cleanupFailure.getMessage());
            }
        }
    }

    private static StateTransfer toNativeTransfer(EliteMindTransferPolicy policy) {
        Objects.requireNonNull(policy, "transferPolicy");
        return switch (policy) {
            case NONE -> StateTransfer.NONE;
            case COMPATIBLE -> StateTransfer.COMPATIBLE;
            case PERSISTENT_ONLY -> StateTransfer.PERSISTENT_ONLY;
        };
    }

    private record InternalProgramEntry(
            Plugin owner,
            EliteMindProgram descriptor,
            MindProgram program) implements EliteMindProgramEntry {
        private InternalProgramEntry {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(descriptor, "descriptor");
            Objects.requireNonNull(program, "program");
        }

        @Override
        public MindProgram instantiate() {
            return program;
        }
    }

    private static MindBodyProfile toNativeProfile(EliteMindBodyProfile profile) {
        Objects.requireNonNull(profile, "profile");
        return new MindBodyProfile(
                switch (profile.locomotion()) {
                    case GROUNDED -> MindBodyLocomotion.GROUNDED;
                    case FLYING -> MindBodyLocomotion.FLYING;
                    case AQUATIC -> MindBodyLocomotion.AQUATIC;
                    case AMPHIBIOUS -> MindBodyLocomotion.AMPHIBIOUS;
                    case STATIONARY -> MindBodyLocomotion.STATIONARY;
                },
                profile.uniformScale(),
                profile.entityCollidable(),
                profile.carrierType().toString());
    }

    private static EliteMindBodyCapabilities toPublicCapabilities(MindBodyCapabilities capabilities) {
        Objects.requireNonNull(capabilities, "capabilities");
        return new EliteMindBodyCapabilities(
                toPublicLocomotion(capabilities.supportedLocomotions()),
                toPublicLocomotion(capabilities.pathfindingLocomotions()),
                capabilities.uniformScale(),
                capabilities.minimumUniformScale(),
                capabilities.maximumUniformScale(),
                capabilities.independentDimensions(),
                capabilities.entityCollisionToggle(),
                capabilities.blockCollisionToggle());
    }

    private static Set<EliteMindBodyLocomotion> toPublicLocomotion(
            Set<MindBodyLocomotion> locomotion) {
        EnumSet<EliteMindBodyLocomotion> mapped = EnumSet.noneOf(EliteMindBodyLocomotion.class);
        for (MindBodyLocomotion value : locomotion) {
            mapped.add(switch (value) {
                case GROUNDED -> EliteMindBodyLocomotion.GROUNDED;
                case FLYING -> EliteMindBodyLocomotion.FLYING;
                case AQUATIC -> EliteMindBodyLocomotion.AQUATIC;
                case AMPHIBIOUS -> EliteMindBodyLocomotion.AMPHIBIOUS;
                case STATIONARY -> EliteMindBodyLocomotion.STATIONARY;
            });
        }
        return mapped;
    }

    private void requireAvailable() {
        if (closed) throw new IllegalStateException("EliteMobs mind service is shut down");
    }

    private static void requireServerThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("EliteMobs mind operations must run on the server thread");
        }
    }
}
