package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityMechanic;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.damage.AdvancedDamageScaling;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.InternalMindActorSpawnRequest;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Owns every transient physical class servant and corpse opportunity. The runtime uses native
 * Mind programs for locomotion and combat while this class owns Bukkit lifecycle, balance and
 * event isolation. No state in this module is persistent.
 */
public final class ClassMinionManager implements Listener, AutoCloseable {
    private static final int CORPSE_UPTIME_TICKS = 30 * 20;
    private static final double CORPSE_ELIGIBILITY_RANGE_SQUARED = 32D * 32D;
    private static final double CORPSE_TARGET_RANGE = 24D;
    private static final double OWNER_MAX_DISTANCE_SQUARED = 40D * 40D;
    private static final double AUTONOMOUS_TARGET_RANGE_SQUARED = 16D * 16D;
    private static final int AUTONOMOUS_TARGET_TICKS = 30;
    private static final int RETALIATION_TICKS = 8 * 20;
    private static final int TICK_PERIOD = 2;
    private static final int CORPSE_PARTICLE_PERIOD = 10;
    private static final int MAX_CORPSE_PARTICLE_MARKERS_PER_PULSE = 32;

    private final Plugin plugin;
    private final OwnerProfileResolver ownerProfiles;
    private final MinionDamageEvaluator damageEvaluator;
    private final MinionImpactEffectApplier impactEffects;
    private final MinionRuntimeObserver runtimeObserver;
    private final ClassMinionIdentity minionIdentity;
    private final AbyssalGateManager gates;
    private final CorpseOpportunityLedger corpseLedger = new CorpseOpportunityLedger();
    private final Map<UUID, CorpseMarker> corpses = new LinkedHashMap<>();
    private final Map<UUID, MinionInstance> minionsByEntity = new HashMap<>();
    private final Map<UUID, MinionInstance> minionsByElite = new HashMap<>();
    private final Map<UUID, ClassMinionRoster<MinionInstance>> rosters = new HashMap<>();
    private final BukkitTask task;
    private long nextCorpseParticleTick;
    private int sequence;
    private boolean closed;

    public ClassMinionManager(
            Plugin plugin,
            OwnerProfileResolver ownerProfiles,
            MinionDamageEvaluator damageEvaluator,
            MinionImpactEffectApplier impactEffects,
            MinionRuntimeObserver runtimeObserver) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.ownerProfiles = Objects.requireNonNull(ownerProfiles, "ownerProfiles");
        this.damageEvaluator = Objects.requireNonNull(damageEvaluator, "damageEvaluator");
        this.impactEffects = Objects.requireNonNull(impactEffects, "impactEffects");
        this.runtimeObserver = Objects.requireNonNull(runtimeObserver, "runtimeObserver");
        this.minionIdentity = new ClassMinionIdentity(plugin);
        this.gates = new AbyssalGateManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, TICK_PERIOD);
    }

    /** Preflights every world mutation and returns failure without consuming the caller's resource. */
    public SummonResult summon(
            Player owner,
            FixedAbilitySpec spec,
            int effectiveLevel,
            Location aimedLocation) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(aimedLocation, "aimedLocation");
        if (closed || !Bukkit.isPrimaryThread() || !owner.isOnline() || owner.isDead()) {
            return SummonResult.failure(Status.RUNTIME_UNAVAILABLE);
        }

        ClassMinionTheme theme = ClassMinionTheme.forAbility(spec.id()).orElse(null);
        if (theme == null) return SummonResult.failure(Status.RUNTIME_UNAVAILABLE);
        ClassMinionBalanceContract balance = ClassMinionBalanceContract.from(
                spec, theme, effectiveLevel);
        long now = MonotonicTickClock.currentTick();

        CorpseMarker selectedCorpse = null;
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.REQUIRES_CORPSE)) {
            selectedCorpse = selectCorpse(owner, now).orElse(null);
            if (selectedCorpse == null) return SummonResult.failure(Status.NO_CORPSE);
            aimedLocation = selectedCorpse.location();
        }

        AbyssalGateManager.GateHandle gate = null;
        if (theme == ClassMinionTheme.NETHER) {
            gate = gates.place(owner, aimedLocation, balance.uptimeTicks()).orElse(null);
            if (gate == null) return SummonResult.failure(Status.UNSAFE_DESTINATION);
        }

        List<SpawnPlan> plans = new ArrayList<>(balance.summonCount());
        for (int index = 0; index < balance.summonCount(); index++) {
            ClassMinionTheme.Carrier carrier = theme.carrierFor(sequence + index);
            Location spawn = findSpawn(aimedLocation, carrier, index).orElse(null);
            if (spawn == null) {
                gates.remove(gate);
                return SummonResult.failure(Status.UNSAFE_DESTINATION);
            }
            plans.add(new SpawnPlan(carrier, spawn));
        }

        UUID ownerId = owner.getUniqueId();
        List<MinionInstance> spawned = new ArrayList<>(plans.size());
        try {
            for (SpawnPlan plan : plans) {
                ClassMinionControlState control = new ClassMinionControlState(
                        ownerId, balance.attackPeriodTicks());
                EliteMindBodyProfile bodyProfile = EliteMindBodyProfile.forCarrier(
                        plan.carrier().entityType().getKey(), plan.carrier().locomotion());
                EliteEntity actor = EliteMindServiceModule.spawnInternal(
                        new InternalMindActorSpawnRequest(
                                plugin,
                                ownerId,
                                plan.location(),
                                Math.max(1, effectiveLevel),
                                bodyProfile,
                                ClassMinionMindProgramFactory.create(plugin, theme, control),
                                elite -> initializeActor(elite, balance)))
                        .orElseThrow(() -> new IllegalStateException("Native Mind runtime is unavailable"));
                LivingEntity body = actor.getLivingEntity();
                if (body == null || !body.isValid()) {
                    EliteMindServiceModule.clearInternal(actor);
                    throw new IllegalStateException("Native Mind body was not committed");
                }
                minionIdentity.mark(body);
                MinionInstance instance = new MinionInstance(
                        actor,
                        body.getUniqueId(),
                        ownerId,
                        spec,
                        effectiveLevel,
                        theme,
                        balance,
                        control,
                        expiresAt(now, balance.uptimeTicks()));
                spawned.add(instance);
            }
        } catch (RuntimeException exception) {
            for (MinionInstance instance : spawned) removeMinion(instance, RemovalReason.OTHER);
            gates.remove(gate);
            Logger.warn("Could not create class servant for " + owner.getName() + ": "
                    + exception.getMessage());
            return SummonResult.failure(Status.RUNTIME_UNAVAILABLE);
        }

        if (selectedCorpse != null
                && !corpseLedger.consume(selectedCorpse.id(), ownerId, now)) {
            for (MinionInstance instance : spawned) removeMinion(instance, RemovalReason.OTHER);
            gates.remove(gate);
            return SummonResult.failure(Status.NO_CORPSE);
        }
        if (selectedCorpse != null && !corpseLedger.contains(selectedCorpse.id())) {
            removeCorpse(selectedCorpse.id());
        }

        sequence += spawned.size();
        for (MinionInstance instance : spawned) register(instance);
        ClassMinionRoster<MinionInstance> roster = rosters.computeIfAbsent(
                ownerId, ignored -> new ClassMinionRoster<>(balance.ownerCap()));
        for (MinionInstance evicted : roster.admitAll(spawned)) {
            removeMinion(evicted, RemovalReason.OTHER);
        }

        Location presentationOrigin = spawned.getFirst().body().getLocation();
        presentationOrigin.getWorld().spawnParticle(
                summonParticle(theme), presentationOrigin.clone().add(0D, .8D, 0D),
                24, .65D, .55D, .65D, .04D);
        presentationOrigin.getWorld().playSound(
                presentationOrigin, summonSound(theme), 1F, summonPitch(theme));
        return SummonResult.success(spawned.stream().map(MinionInstance::body).toList(), presentationOrigin);
    }

    public void deactivate(Player owner) {
        if (owner != null) deactivate(owner.getUniqueId());
    }

    public void deactivate(UUID ownerId) {
        if (ownerId == null) return;
        ClassMinionRoster<MinionInstance> roster = rosters.remove(ownerId);
        if (roster != null)
            for (MinionInstance minion : roster.drain()) removeMinion(minion, RemovalReason.OTHER);
        gates.removeOwner(ownerId);
        for (UUID corpseId : corpseLedger.removePlayer(ownerId)) removeCorpse(corpseId);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEliteDeath(EliteMobDeathEvent event) {
        EliteEntity dead = event.getEliteEntity();
        if (dead == null || minionsByElite.containsKey(dead.getEliteUUID())) return;
        Location deathLocation = event.getEntity() == null ? null : event.getEntity().getLocation();
        if (deathLocation == null || deathLocation.getWorld() == null) return;

        Set<UUID> eligible = new LinkedHashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(deathLocation.getWorld())
                    || player.getLocation().distanceSquared(deathLocation) > CORPSE_ELIGIBILITY_RANGE_SQUARED)
                continue;
            OwnerProfile profile = ownerProfiles.resolve(player).orElse(null);
            if (profile != null && isNecromancerForm(profile.activeFormId())) {
                eligible.add(player.getUniqueId());
            }
        }
        if (eligible.isEmpty()) return;

        UUID corpseId = UUID.randomUUID();
        long now = MonotonicTickClock.currentTick();
        long expiresAt = expiresAt(now, CORPSE_UPTIME_TICKS);
        Location markerLocation = deathLocation.clone().add(0D, .35D, 0D);
        TextDisplay display = markerLocation.getWorld().spawn(
                markerLocation, TextDisplay.class, text -> {
                    text.setText(ChatColorConverter.convert("&5&lCorpse"));
                    text.setBillboard(Display.Billboard.CENTER);
                    text.setShadowed(true);
                    text.setPersistent(false);
                    text.setSeeThrough(false);
                    text.setViewRange(24F);
                });
        EntityTracker.registerVisualEffects(display);
        corpseLedger.offer(corpseId, eligible, expiresAt);
        corpses.put(corpseId, new CorpseMarker(corpseId, deathLocation, display, expiresAt));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerAttacksMinion(EntityDamageByEntityEvent event) {
        if (minionsByEntity.containsKey(event.getEntity().getUniqueId())
                && source(event.getDamager()) instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMinionDamage(EntityDamageByEntityEvent event) {
        LivingEntity source = source(event.getDamager());
        if (source == null) return;
        MinionInstance minion = minionsByEntity.get(source.getUniqueId());
        if (minion == null) return;

        EliteEntity target = EntityTracker.getEliteMobEntity(event.getEntity());
        double damage = target == null
                ? 4D * minion.balance().scaledDamageMultiplierPerHit()
                : AdvancedDamageScaling.classAbility(
                        target, minion.effectiveLevel(), minion.balance().damageMultiplierPerHit());
        Player owner = Bukkit.getPlayer(minion.ownerId());
        if (target != null && owner != null && owner.isOnline() && !owner.isDead()) {
            damage = damageEvaluator.evaluate(
                    owner,
                    target,
                    damage,
                    CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_SUMMON);
        }
        if (!Double.isFinite(damage) || damage <= 0D) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(damage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatCommand(EntityDamageByEntityEvent event) {
        LivingEntity attacker = source(event.getDamager());
        if (attacker == null || attacker instanceof Player || isMinion(attacker)) return;

        MinionInstance struckMinion = minionsByEntity.get(event.getEntity().getUniqueId());
        if (struckMinion != null) {
            struckMinion.control().commandTarget(attacker, RETALIATION_TICKS);
            return;
        }
        if (event.getEntity() instanceof Player owner) {
            ClassMinionRoster<MinionInstance> roster = rosters.get(owner.getUniqueId());
            if (roster == null) return;
            for (MinionInstance minion : roster.entries()) {
                minion.control().commandTarget(attacker, RETALIATION_TICKS);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMinionImpact(EntityDamageByEntityEvent event) {
        LivingEntity attacker = source(event.getDamager());
        MinionInstance minion = attacker == null
                ? null : minionsByEntity.get(attacker.getUniqueId());
        if (minion == null || !(event.getEntity() instanceof LivingEntity target)
                || event.getFinalDamage() <= 0D) return;

        Player owner = Bukkit.getPlayer(minion.ownerId());
        if (owner == null || !owner.isOnline() || owner.isDead()) return;
        EliteEntity targetElite = EntityTracker.getEliteMobEntity(target);
        if (targetElite != null) {
            // Credit the explicit player owner. The servant never becomes a reward or threat owner.
            targetElite.addDamager(owner, event.getFinalDamage());
        }
        FixedAbilitySpec spec = minion.spec();
        runtimeObserver.applied(
                owner.getUniqueId(), target.getUniqueId(), spec.id(),
                AbilityEffect.DAMAGE, event.getFinalDamage());
        ClassMinionImpactPlan impactPlan = ClassMinionImpactPlan.from(spec);
        if (impactPlan.lifestealFraction() > 0D) {
            double healed = healOwner(owner, event.getFinalDamage() * impactPlan.lifestealFraction()
                    * damageEvaluator.healingDoneMultiplier(owner));
            if (healed > 0D)
                runtimeObserver.applied(
                        owner.getUniqueId(), owner.getUniqueId(), spec.id(),
                        AbilityEffect.LIFESTEAL, healed);
        }
        impactEffects.apply(owner, spec, target, minion.effectiveLevel());
        Location impact = target.getLocation().add(0D, target.getHeight() * .5D, 0D);
        impact.getWorld().spawnParticle(
                summonParticle(minion.theme()), impact, 6, .22D, .22D, .22D, .025D);
        impact.getWorld().playSound(impact, Sound.ENTITY_PLAYER_ATTACK_STRONG, .55F, .8F);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMinionDeath(EntityDeathEvent event) {
        if (!isMinion(event.getEntity())) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (!taggedMinion(entity)) continue;
            EliteEntity actor = EntityTracker.getEliteMobEntity(entity);
            if (actor != null) {
                try {
                    if (EliteMindServiceModule.clearInternal(actor)) continue;
                } catch (RuntimeException ignored) {
                }
            }
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteRemove(EliteMobRemoveEvent event) {
        MinionInstance minion = minionsByElite.get(event.getEliteMobEntity().getEliteUUID());
        if (minion != null) forget(minion);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (isMinion(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (isMinion(event.getMother()) || isMinion(event.getFather())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLoveMode(EntityEnterLoveModeEvent event) {
        if (isMinion(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent event) {
        if (isMinion(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        if (!isMinion(event.getEntity())) return;
        event.blockList().clear();
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(EntityPortalEvent event) {
        if (isMinion(event.getEntity())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            MinionInstance minion = minionsByEntity.get(entity.getUniqueId());
            if (minion != null) removeMinion(minion, RemovalReason.OTHER);
        }
        List<UUID> markerIds = corpses.values().stream()
                .filter(marker -> marker.location().getWorld().equals(event.getWorld()))
                .filter(marker -> marker.location().getBlockX() >> 4 == event.getChunk().getX())
                .filter(marker -> marker.location().getBlockZ() >> 4 == event.getChunk().getZ())
                .map(CorpseMarker::id)
                .toList();
        for (UUID markerId : markerIds) removeCorpse(markerId);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldUnloadEvent event) {
        for (MinionInstance minion : new ArrayList<>(minionsByEntity.values())) {
            LivingEntity body = minion.body();
            if (body != null && body.getWorld().equals(event.getWorld())) {
                removeMinion(minion, RemovalReason.OTHER);
            }
        }
        List<UUID> markerIds = corpses.values().stream()
                .filter(marker -> marker.location().getWorld().equals(event.getWorld()))
                .map(CorpseMarker::id)
                .toList();
        for (UUID markerId : markerIds) removeCorpse(markerId);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        deactivate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        deactivate(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        deactivate(event.getPlayer());
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        task.cancel();
        for (MinionInstance minion : new ArrayList<>(minionsByEntity.values())) {
            removeMinion(minion, RemovalReason.SHUTDOWN);
        }
        for (UUID corpseId : new ArrayList<>(corpses.keySet())) removeCorpse(corpseId);
        rosters.clear();
        gates.close();
        HandlerList.unregisterAll(this);
    }

    private void tick() {
        if (closed) return;
        long now = MonotonicTickClock.currentTick();
        gates.tick(now);
        for (UUID corpseId : corpseLedger.expire(now)) removeCorpse(corpseId);
        if (now >= nextCorpseParticleTick) {
            nextCorpseParticleTick = now + CORPSE_PARTICLE_PERIOD;
            corpses.values().stream()
                    .limit(MAX_CORPSE_PARTICLE_MARKERS_PER_PULSE)
                    .forEach(this::renderCorpse);
        }

        for (MinionInstance minion : new ArrayList<>(minionsByEntity.values())) {
            Player owner = Bukkit.getPlayer(minion.ownerId());
            LivingEntity body = minion.body();
            OwnerProfile profile = owner == null ? null : ownerProfiles.resolve(owner).orElse(null);
            if (now >= minion.expiresAtTick()
                    || owner == null || !owner.isOnline() || owner.isDead()
                    || profile == null
                    || !minion.spec().id().equals(profile.activeFormId() + ".signature")
                    || body == null || !body.isValid() || body.isDead()
                    || !body.getWorld().equals(owner.getWorld())
                    || body.getLocation().distanceSquared(owner.getLocation()) > OWNER_MAX_DISTANCE_SQUARED) {
                removeMinion(minion, RemovalReason.OTHER);
                continue;
            }
            if (now % 20L == 0L && minion.control().target(body).isEmpty()) {
                nearestEliteTarget(owner, body).ifPresent(
                        target -> minion.control().commandTarget(target, AUTONOMOUS_TARGET_TICKS));
            }
        }
    }

    private Optional<CorpseMarker> selectCorpse(Player owner, long currentTick) {
        Location eye = owner.getEyeLocation();
        Vector direction = eye.getDirection();
        List<CorpseTargetSelector.Candidate> candidates = corpses.values().stream()
                .filter(marker -> marker.location().getWorld().equals(owner.getWorld()))
                .filter(marker -> corpseLedger.availableTo(marker.id(), owner.getUniqueId(), currentTick))
                .map(marker -> new CorpseTargetSelector.Candidate(
                        marker.id(), point(marker.location())))
                .toList();
        UUID selected = CorpseTargetSelector.select(
                point(eye), new CorpseTargetSelector.Point(
                        direction.getX(), direction.getY(), direction.getZ()),
                CORPSE_TARGET_RANGE, candidates).orElse(null);
        return Optional.ofNullable(selected == null ? null : corpses.get(selected));
    }

    private Optional<LivingEntity> nearestEliteTarget(Player owner, LivingEntity body) {
        return body.getNearbyEntities(16D, 12D, 16D).stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(candidate -> !(candidate instanceof Player))
                .filter(candidate -> !isMinion(candidate))
                .filter(candidate -> EntityTracker.getEliteMobEntity(candidate) != null)
                .filter(candidate -> candidate.getWorld().equals(owner.getWorld()))
                .filter(candidate -> candidate.getLocation().distanceSquared(owner.getLocation())
                        <= AUTONOMOUS_TARGET_RANGE_SQUARED)
                .min(Comparator.comparingDouble(candidate ->
                        candidate.getLocation().distanceSquared(body.getLocation())));
    }

    private void register(MinionInstance minion) {
        minionsByEntity.put(minion.entityId(), minion);
        minionsByElite.put(minion.actor().getEliteUUID(), minion);
    }

    private void removeMinion(MinionInstance minion, RemovalReason reason) {
        if (minion == null) return;
        if (minionsByElite.get(minion.actor().getEliteUUID()) != minion
                && !minionsByEntity.containsKey(minion.entityId())) {
            // A newly spawned actor may not have been registered yet.
            try {
                EliteMindServiceModule.clearInternal(minion.actor());
            } catch (RuntimeException ignored) {
            }
            return;
        }
        forget(minion);
        try {
            if (!EliteMindServiceModule.clearInternal(minion.actor())) {
                minion.actor().remove(reason);
            }
        } catch (RuntimeException exception) {
            LivingEntity body = minion.body();
            if (body != null && body.isValid()) body.remove();
            Logger.warn("Could not cleanly remove class servant: " + exception.getMessage());
        }
    }

    private void forget(MinionInstance minion) {
        boolean tracked = minionsByEntity.remove(minion.entityId(), minion);
        tracked |= minionsByElite.remove(minion.actor().getEliteUUID(), minion);
        ClassMinionRoster<MinionInstance> roster = rosters.get(minion.ownerId());
        if (roster != null) {
            roster.remove(minion);
            if (roster.entries().isEmpty()) rosters.remove(minion.ownerId());
        }
        if (tracked) runtimeObserver.removed(
                minion.ownerId(), minion.entityId(), minion.spec().id());
    }

    private void removeCorpse(UUID corpseId) {
        corpseLedger.discard(corpseId);
        CorpseMarker marker = corpses.remove(corpseId);
        if (marker != null && marker.display().isValid()) marker.display().remove();
    }

    private void renderCorpse(CorpseMarker marker) {
        World world = marker.location().getWorld();
        if (world == null || !world.isChunkLoaded(
                marker.location().getBlockX() >> 4, marker.location().getBlockZ() >> 4)) return;
        world.spawnParticle(Particle.SOUL, marker.location().clone().add(0D, .5D, 0D),
                4, .25D, .35D, .25D, .01D);
    }

    private static void initializeActor(EliteEntity actor, ClassMinionBalanceContract balance) {
        actor.setPersistent(false);
        actor.setEliteLoot(false);
        actor.setVanillaLoot(false);
        actor.setRandomLoot(false);
        double existing = Math.max(1D, actor.getMaxHealth());
        actor.setHealthMultiplier(balance.maxHealth() / existing);
        actor.setMaxHealth();
        actor.resetMaxHealth();
        actor.clearDamagers();

        LivingEntity body = actor.getLivingEntity();
        if (body == null) return;
        body.setPersistent(false);
        body.setRemoveWhenFarAway(false);
        body.setCanPickupItems(false);
        body.setPortalCooldown(Integer.MAX_VALUE);
        body.setCustomNameVisible(false);
        AttributeManager.setAttribute(body, "generic_attack_damage", 1D);
        EntityEquipment equipment = body.getEquipment();
        if (equipment != null) {
            equipment.clear();
            equipment.setItemInMainHandDropChance(0F);
            equipment.setItemInOffHandDropChance(0F);
            equipment.setHelmetDropChance(0F);
            equipment.setChestplateDropChance(0F);
            equipment.setLeggingsDropChance(0F);
            equipment.setBootsDropChance(0F);
        }
        if (body instanceof Ageable ageable) {
            ageable.setAdult();
            ageable.setAgeLock(true);
        }
        if (body instanceof Animals animals) animals.setBreed(false);
    }

    private static double healOwner(Player owner, double requested) {
        if (!Double.isFinite(requested) || requested <= 0D) return 0D;
        double maximum = owner.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) == null
                ? Math.max(1D, owner.getHealth())
                : owner.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double amount = Math.min(requested, Math.max(0D, maximum - owner.getHealth()));
        if (amount <= 0D) return 0D;
        double before = owner.getHealth();
        EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                owner, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled() && event.getAmount() > 0D) {
            owner.setHealth(Math.min(maximum, owner.getHealth() + event.getAmount()));
        }
        return Math.max(0D, owner.getHealth() - before);
    }

    private Optional<Location> findSpawn(
            Location aimed,
            ClassMinionTheme.Carrier carrier,
            int ordinal) {
        World world = aimed.getWorld();
        if (world == null) return Optional.empty();
        int[][] offsets = {
                {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {2, 0}, {-2, 0}
        };
        for (int step = 0; step < offsets.length; step++) {
            int[] offset = offsets[Math.floorMod(step + ordinal * 2, offsets.length)];
            int x = aimed.getBlockX() + offset[0];
            int z = aimed.getBlockZ() + offset[1];
            if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
            for (int yOffset = 1; yOffset >= -2; yOffset--) {
                int y = aimed.getBlockY() + yOffset;
                Location candidate = new Location(world, x + .5D, y, z + .5D,
                        aimed.getYaw(), 0F);
                if (validSpawn(candidate, carrier)) return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static boolean validSpawn(Location location, ClassMinionTheme.Carrier carrier) {
        if (!location.getBlock().isPassable()
                || !location.clone().add(0D, 1D, 0D).getBlock().isPassable()) return false;
        if (carrier.locomotion() == com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion.FLYING)
            return true;
        return location.clone().add(0D, -1D, 0D).getBlock().getType().isSolid();
    }

    private boolean isMinion(Entity entity) {
        return entity != null && (minionsByEntity.containsKey(entity.getUniqueId()) || taggedMinion(entity));
    }

    private boolean taggedMinion(Entity entity) {
        return minionIdentity.matches(entity);
    }

    private static LivingEntity source(Entity damager) {
        if (damager instanceof LivingEntity living) return living;
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            return shooter instanceof LivingEntity living ? living : null;
        }
        return null;
    }

    private static boolean isNecromancerForm(String formId) {
        return "necromancer".equals(formId)
                || "lich".equals(formId)
                || "plaguebringer".equals(formId);
    }

    private static long expiresAt(long now, int durationTicks) {
        return Long.MAX_VALUE - now < durationTicks ? Long.MAX_VALUE : now + durationTicks;
    }

    private static CorpseTargetSelector.Point point(Location location) {
        return new CorpseTargetSelector.Point(location.getX(), location.getY(), location.getZ());
    }

    private static Particle summonParticle(ClassMinionTheme theme) {
        return switch (theme) {
            case UNDEAD -> Particle.SOUL;
            case ANIMAL -> Particle.HAPPY_VILLAGER;
            case NETHER -> Particle.SOUL_FIRE_FLAME;
            case SPIRIT -> Particle.END_ROD;
        };
    }

    private static Sound summonSound(ClassMinionTheme theme) {
        return switch (theme) {
            case UNDEAD -> Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED;
            case ANIMAL -> Sound.ENTITY_WOLF_AMBIENT;
            case NETHER -> Sound.BLOCK_PORTAL_TRIGGER;
            case SPIRIT -> Sound.ENTITY_VEX_AMBIENT;
        };
    }

    private static float summonPitch(ClassMinionTheme theme) {
        return theme == ClassMinionTheme.NETHER ? .65F : 1.15F;
    }

    @FunctionalInterface
    public interface OwnerProfileResolver {
        Optional<OwnerProfile> resolve(Player player);
    }

    @FunctionalInterface
    public interface MinionDamageEvaluator {
        MinionDamageEvaluator NEUTRAL = (owner, target, rawDamage, domain) -> rawDamage;

        double evaluate(
                Player owner,
                EliteEntity target,
                double rawDamage,
                CombatDamageContext.ClassAbilityDamageDomain domain);

        /** Applies owner-side outgoing-healing bonuses to servant drain without reclassifying it
         * as healing received by the owner. */
        default double healingDoneMultiplier(Player owner) {
            return 1D;
        }
    }

    @FunctionalInterface
    public interface MinionImpactEffectApplier {
        MinionImpactEffectApplier NONE = (owner, spec, target, effectiveLevel) -> { };

        void apply(Player owner, FixedAbilitySpec spec, LivingEntity target, int effectiveLevel);
    }

    @FunctionalInterface
    public interface MinionRuntimeObserver {
        MinionRuntimeObserver NONE = (casterId, targetId, abilityId, effect, amount) -> { };

        void applied(
                UUID casterId,
                UUID targetId,
                String abilityId,
                AbilityEffect effect,
                double amount);

        default void removed(UUID casterId, UUID minionId, String abilityId) {
        }
    }

    public record OwnerProfile(String activeFormId, int effectiveLevel) {
        public OwnerProfile {
            Objects.requireNonNull(activeFormId, "activeFormId");
            if (effectiveLevel < 1) throw new IllegalArgumentException("effectiveLevel must be positive");
        }
    }

    public enum Status {
        SUCCESS,
        NO_CORPSE,
        UNSAFE_DESTINATION,
        RUNTIME_UNAVAILABLE
    }

    public record SummonResult(Status status, List<LivingEntity> minions, Location origin) {
        public SummonResult {
            Objects.requireNonNull(status, "status");
            minions = List.copyOf(minions);
            origin = origin == null ? null : origin.clone();
            if ((status == Status.SUCCESS) != (!minions.isEmpty() && origin != null)) {
                throw new IllegalArgumentException("Successful summons need physical minions and an origin");
            }
        }

        static SummonResult success(List<LivingEntity> minions, Location origin) {
            return new SummonResult(Status.SUCCESS, minions, origin);
        }

        static SummonResult failure(Status status) {
            if (status == Status.SUCCESS) throw new IllegalArgumentException("Failure cannot be successful");
            return new SummonResult(status, List.of(), null);
        }

        @Override
        public Location origin() {
            return origin == null ? null : origin.clone();
        }
    }

    private record SpawnPlan(ClassMinionTheme.Carrier carrier, Location location) {
        private SpawnPlan {
            location = location.clone();
        }

        @Override
        public Location location() {
            return location.clone();
        }
    }

    private record CorpseMarker(UUID id, Location location, TextDisplay display, long expiresAtTick) {
        private CorpseMarker {
            location = location.clone();
        }

        @Override
        public Location location() {
            return location.clone();
        }
    }

    private record MinionInstance(
            EliteEntity actor,
            UUID entityId,
            UUID ownerId,
            FixedAbilitySpec spec,
            int effectiveLevel,
            ClassMinionTheme theme,
            ClassMinionBalanceContract balance,
            ClassMinionControlState control,
            long expiresAtTick) {
        LivingEntity body() {
            return actor.getLivingEntity();
        }
    }
}
