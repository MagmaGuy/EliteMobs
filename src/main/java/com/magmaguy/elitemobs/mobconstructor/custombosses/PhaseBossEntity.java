package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.ElitePhaseSwitchEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PhaseBossEntity {

    private static final int DESTINATION_WAIT_TICKS = 100;
    // Phase scripts often teleport players a few ticks after the switch. Without a nearby player
    // the destination chunk would unload in that gap, removing the boss and cancelling the teleport.
    private static final int DESTINATION_HOLD_TICKS = 200;
    // A player this close is standing in the destination's own loaded area. A wider radius such as
    // the view distance could count players still at the previous arena and release too early.
    private static final int PLAYER_ARRIVAL_CHUNKS = 2;
    // Bukkit tickets are per plugin/chunk, so concurrent phases share only tickets we acquired.
    private static final Map<DestinationTicket, Integer> destinationTickets = new HashMap<>();
    private final CustomBossEntity customBossEntity;
    @Getter
    private final List<BossPhase> bossPhases;
    private BossPhase currentPhase = null;
    private Location originalSpawnLocation;
    private Double pendingHealthFraction;
    private PendingTransition pendingTransition;
    private DestinationHold destinationHold;
    private final java.util.Set<BossPhase> reportedInvalidLocations = new java.util.HashSet<>();

    public PhaseBossEntity(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        ArrayList<BossPhase> parsed = new ArrayList<>();
        parsed.add(new BossPhase(customBossEntity.getCustomBossesConfigFields(), 1));
        for (String entry : customBossEntity.getCustomBossesConfigFields().getPhases()) {
            try {
                String[] parts = entry.split(":", -1);
                if (parts.length != 2) throw new IllegalArgumentException("Expected filename:healthFraction");
                CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(parts[0].trim());
                if (fields == null) throw new IllegalArgumentException("Missing phase boss " + parts[0]);
                double fraction = Double.parseDouble(parts[1].trim());
                if (!Double.isFinite(fraction) || fraction < 0 || fraction >= 1)
                    throw new IllegalArgumentException("Phase health fraction must be between 0 inclusive and 1 exclusive");
                parsed.add(new BossPhase(fields, fraction));
            } catch (RuntimeException failure) {
                throw new IllegalArgumentException("Invalid phase '" + entry + "' in "
                        + customBossEntity.getCustomBossesConfigFields().getFilename() + ": " + failure.getMessage(), failure);
            }
        }
        parsed.sort((first, second) -> Double.compare(second.healthPercentage, first.healthPercentage));
        bossPhases = List.copyOf(parsed);
        currentPhase = bossPhases.get(0);
    }

    public boolean isInFirstPhase() {
        return bossPhases.get(0).equals(currentPhase);
    }

    private void switchPhase(BossPhase bossPhase, RemovalReason removalReason, double healthPercentage) {
        if (bossPhase.equals(currentPhase)) {
            Logger.warn("Attempted to change the boss phase to what it already was.", true);
            return;
        }
        Location origin = customBossEntity.getLocation();
        Location destination;
        boolean reset = removalReason == RemovalReason.PHASE_BOSS_RESET;
        String authored = bossPhase.customBossesConfigFields.getPhaseSpawnLocation();
        if (reset) destination = originalSpawnLocation == null ? customBossEntity.getSpawnLocation() : originalSpawnLocation;
        else if (authored == null) destination = origin;
        else {
            destination = ConfigurationLocation.serialize(authored, true);
            if (destination != null && origin != null && authored.split(",", 2)[0].equalsIgnoreCase("same_as_boss"))
                destination.setWorld(origin.getWorld());
        }
        PendingTransition preparation = null;
        try {
            if (destination == null || destination.getWorld() == null)
                throw new IllegalArgumentException("Phase destination has no loaded world");
            destination.checkFinite();
            destination = destination.clone();
            if (NMSManager.getAdapter() == null)
                throw new IllegalStateException("Native chunk readiness is unavailable");
            if (!NMSManager.getAdapter().isPositionEntityTicking(destination)) {
                // A loaded chunk may still reject entity tracking. Keep the old encounter
                // until the destination can accept its replacement and run the phase script.
                preparation = new PendingTransition(bossPhase, removalReason, healthPercentage, destination);
                pendingTransition = preparation;
                preparation.acquireTicket();
                // Loading can synchronously invoke listeners which retire or reset this boss.
                if (pendingTransition != preparation || !preparation.stillCurrent()) {
                    if (pendingTransition == preparation) pendingTransition = null;
                    preparation.close();
                    return;
                }
                preparation.task = Bukkit.getScheduler().runTaskTimer(
                        preparation.plugin, preparation, 1L, 1L);
                return;
            }
        } catch (RuntimeException failure) {
            if (preparation != null) {
                if (pendingTransition == preparation) pendingTransition = null;
                preparation.close();
            }
            reportDestinationFailure(bossPhase, failure.getMessage());
            return;
        }
        completeTransition(bossPhase, removalReason, healthPercentage, destination);
    }

    private void completeTransition(BossPhase bossPhase, RemovalReason removalReason,
                                    double healthPercentage, Location destination) {
        boolean reset = removalReason == RemovalReason.PHASE_BOSS_RESET;
        String authored = bossPhase.customBossesConfigFields.getPhaseSpawnLocation();
        if (isInFirstPhase()) originalSpawnLocation = customBossEntity.getSpawnLocation().clone();
        if (removalReason == RemovalReason.PHASE_BOSS_PHASE_END
                && customBossEntity instanceof RegionalBossEntity regional)
            com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBossBlock.clearPhaseBlocks(regional);
        customBossEntity.remove(removalReason);
        if (customBossEntity.getCustomModel() != null) customBossEntity.getCustomModel().switchPhase();
        customBossEntity.setCustomBossesConfigFields(bossPhase.customBossesConfigFields);
        if (reset) {
            if (bossPhase.customBossesConfigFields.getSong() != null)
                customBossEntity.setBossMusic(new CustomMusic(bossPhase.customBossesConfigFields.getSong(), customBossEntity));
            customBossEntity.setSpawnLocation(destination.clone());
        } else {
            if (authored != null) customBossEntity.setSpawnLocation(destination.clone());
            //Handle music, soundtrack shouldn't change if the new one is the same
            if (bossPhase.customBossesConfigFields.getSong() != null) {
                if (currentPhase.customBossesConfigFields.getSong() == null) {
                    customBossEntity.setBossMusic(new CustomMusic(bossPhase.customBossesConfigFields.getSong(), customBossEntity));
                    customBossEntity.getBossMusic().start(customBossEntity);
                }
                if (!bossPhase.customBossesConfigFields.getSong().equals(currentPhase.customBossesConfigFields.getSong())) {
                    if (customBossEntity.getBossMusic() != null) customBossEntity.getBossMusic().stop();
                    customBossEntity.setBossMusic(new CustomMusic(bossPhase.customBossesConfigFields.getSong(), customBossEntity));
                }
            }
        }
        customBossEntity.setRespawnOverrideLocation(destination.clone());
        customBossEntity.setPersistentLocation(destination);
        currentPhase = bossPhase;
        pendingHealthFraction = healthPercentage;
        // Take the hold before materializing, so the phase scripts fired during the spawn and
        // their delayed actions find the destination still loaded. The new lease is acquired
        // before the previous hold releases, so a shared chunk never loses its ticket in between.
        DestinationHold previousHold = destinationHold;
        DestinationHold hold = new DestinationHold(bossPhase, destination);
        destinationHold = hold;
        try {
            hold.start();
        } catch (RuntimeException failure) {
            if (destinationHold == hold) destinationHold = null;
            hold.close();
            Logger.warn("Could not hold the phase destination of "
                    + bossPhase.customBossesConfigFields.getFilename() + ": " + failure.getMessage());
        } finally {
            if (previousHold != null) previousHold.close();
        }
        // Logical transition owns the intent even if materialization waits for a loaded chunk.
        customBossEntity.spawn(true);
    }

    private void releaseDestinationHold() {
        DestinationHold hold = destinationHold;
        destinationHold = null;
        if (hold != null) hold.close();
    }

    private void reportDestinationFailure(BossPhase phase, String problem) {
        if (reportedInvalidLocations.add(phase))
            Logger.warn("Refused phase transition from " + currentPhase.customBossesConfigFields.getFilename()
                    + " to " + phase.customBossesConfigFields.getFilename() + ": " + problem
                    + ". The current phase was retained.");
    }

    private void cancelPendingTransition() {
        PendingTransition pending = pendingTransition;
        pendingTransition = null;
        if (pending != null) pending.close();
    }

    private record DestinationTicket(UUID worldId, int x, int z, Plugin plugin) {}

    /**
     * One holder's share of the plugin chunk ticket at a phase destination. Bukkit keeps a single
     * ticket per plugin and chunk, so shares are counted and the ticket goes when the last one does.
     */
    private static final class TicketLease {
        private final World world;
        private final DestinationTicket ticket;
        private boolean owned;

        private TicketLease(Location destination, Plugin plugin) {
            world = destination.getWorld();
            ticket = new DestinationTicket(world.getUID(),
                    destination.getBlockX() >> 4, destination.getBlockZ() >> 4, plugin);
        }

        private void acquire() {
            if (owned) return;
            Integer owners = destinationTickets.get(ticket);
            if (owners != null) {
                destinationTickets.put(ticket, owners + 1);
                owned = true;
                // A count left over from an interrupted lifecycle must not stand in for a ticket.
                if (!world.getPluginChunkTickets(ticket.x(), ticket.z()).contains(ticket.plugin())
                        && !world.addPluginChunkTicket(ticket.x(), ticket.z(), ticket.plugin()))
                    throw new IllegalStateException("Phase destination chunk ticket could not be acquired");
            } else if (!world.getPluginChunkTickets(ticket.x(), ticket.z()).contains(ticket.plugin())) {
                // Bukkit inserts the ticket before loading. Own cleanup before the load
                // can throw or synchronously invoke a listener which cancels this phase.
                destinationTickets.put(ticket, 1);
                owned = true;
                if (!world.addPluginChunkTicket(ticket.x(), ticket.z(), ticket.plugin()))
                    throw new IllegalStateException("Phase destination chunk ticket could not be acquired");
            }
        }

        private boolean worldLoaded() {
            return Bukkit.getWorld(ticket.worldId()) == world;
        }

        private void release() {
            if (!owned) return;
            owned = false;
            Integer owners = destinationTickets.get(ticket);
            if (owners == null) return;
            if (owners > 1) {
                destinationTickets.put(ticket, owners - 1);
                return;
            }
            destinationTickets.remove(ticket);
            // An unloaded world already dropped its tickets; a disabled plugin had them removed by Bukkit.
            if (worldLoaded() && ticket.plugin().isEnabled())
                world.removePluginChunkTicket(ticket.x(), ticket.z(), ticket.plugin());
        }
    }

    private final class PendingTransition implements Runnable {
        private final BossPhase expectedPhase = currentPhase;
        private final LivingEntity expectedBody = customBossEntity.getLivingEntity();
        private final BossPhase nextPhase;
        private final RemovalReason removalReason;
        private final double healthPercentage;
        private final Location destination;
        private final Plugin plugin = MetadataHandler.PLUGIN;
        private final TicketLease lease;
        private BukkitTask task;
        private int elapsedTicks;

        private PendingTransition(BossPhase nextPhase, RemovalReason removalReason,
                                  double healthPercentage, Location destination) {
            this.nextPhase = nextPhase;
            this.removalReason = removalReason;
            this.healthPercentage = healthPercentage;
            this.destination = destination;
            lease = new TicketLease(destination, plugin);
        }

        private void acquireTicket() {
            lease.acquire();
        }

        private boolean stillCurrent() {
            return customBossEntity.isValid() && customBossEntity.getLivingEntity() == expectedBody
                    && currentPhase == expectedPhase && plugin.isEnabled() && lease.worldLoaded();
        }

        @Override
        public void run() {
            if (pendingTransition != this) return;
            if (!stillCurrent()) {
                cancelPendingTransition();
                return;
            }
            boolean ready;
            try {
                if (NMSManager.getAdapter() == null)
                    throw new IllegalStateException("Native chunk readiness is unavailable");
                ready = NMSManager.getAdapter().isPositionEntityTicking(destination);
            } catch (RuntimeException failure) {
                cancelPendingTransition();
                reportDestinationFailure(nextPhase, failure.getMessage());
                return;
            }
            if (ready) {
                pendingTransition = null;
                if (task != null) task.cancel();
                try {
                    completeTransition(nextPhase, removalReason, healthPercentage, destination);
                } finally {
                    // completeTransition took a DestinationHold share of this chunk before
                    // materializing, so releasing the wait's share keeps the ticket in place
                    // until a player arrives or the hold times out.
                    close();
                }
            } else if (++elapsedTicks >= DESTINATION_WAIT_TICKS) {
                cancelPendingTransition();
                reportDestinationFailure(nextPhase, "Destination did not become entity-ticking within "
                        + DESTINATION_WAIT_TICKS + " ticks");
            }
        }

        private void close() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            lease.release();
        }
    }

    /**
     * Keeps a phase destination loaded after the boss materializes there. Phase scripts commonly
     * teleport players a few ticks later; with nobody nearby the chunk would unload first, the boss
     * would be removed and its scheduled teleport cancelled. Released once a player is within
     * {@link #PLAYER_ARRIVAL_CHUNKS} chunks of the destination, after {@link #DESTINATION_HOLD_TICKS}, or when the encounter ends.
     */
    private final class DestinationHold implements Runnable {
        private final BossPhase heldPhase;
        private final Location destination;
        private final Plugin plugin = MetadataHandler.PLUGIN;
        private final TicketLease lease;
        private BukkitTask task;
        private int elapsedTicks;
        private boolean closed;

        private DestinationHold(BossPhase heldPhase, Location destination) {
            this.heldPhase = heldPhase;
            this.destination = destination.clone();
            lease = new TicketLease(this.destination, plugin);
        }

        private void start() {
            lease.acquire();
            if (closed) return;
            task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 1L);
        }

        @Override
        public void run() {
            if (closed) return;
            if (destinationHold != this || currentPhase != heldPhase || !plugin.isEnabled()
                    || !lease.worldLoaded() || ++elapsedTicks >= DESTINATION_HOLD_TICKS
                    || playerArrived()) {
                if (destinationHold == this) destinationHold = null;
                close();
            }
        }

        private boolean playerArrived() {
            int chunkX = destination.getBlockX() >> 4;
            int chunkZ = destination.getBlockZ() >> 4;
            for (Player player : destination.getWorld().getPlayers()) {
                Location location = player.getLocation();
                if (Math.abs((location.getBlockX() >> 4) - chunkX) <= PLAYER_ARRIVAL_CHUNKS
                        && Math.abs((location.getBlockZ() >> 4) - chunkZ) <= PLAYER_ARRIVAL_CHUNKS)
                    return true;
            }
            return false;
        }

        private void close() {
            if (closed) return;
            closed = true;
            if (task != null) {
                task.cancel();
                task = null;
            }
            lease.release();
        }
    }

    void onBodyMaterialized() {
        if (pendingHealthFraction == null || !customBossEntity.isValid()) return;
        double fraction = pendingHealthFraction;
        customBossEntity.setHealth(customBossEntity.getMaxHealth() * fraction);
        pendingHealthFraction = null;
        if (!customBossEntity.isValid()) return;
        customBossEntity.setCombatGracePeriod(20);
        new EventCaller(new ElitePhaseSwitchEvent(customBossEntity, this));
    }

    public void resetToFirstPhase() {
        cancelPendingTransition();
        if (isInFirstPhase()) return;
        switchPhase(bossPhases.get(0), RemovalReason.PHASE_BOSS_RESET, 1);
    }

    public void silentReset() {
        cancelPendingTransition();
        releaseDestinationHold();
        pendingHealthFraction = null;
        currentPhase = bossPhases.get(0);
        customBossEntity.setCustomBossesConfigFields(currentPhase.customBossesConfigFields);
    }

    /**
     * Used to set the respawn time in the config file of Regional Bosses
     *
     * @return The configuration file for phase 1 of Phase Bosses
     */
    public CustomBossesConfigFields getPhase1Config() {
        return bossPhases.get(0).customBossesConfigFields;
    }

    public void checkPhaseBossSwitch(EliteMobDamagedEvent event) {
        if (pendingTransition != null) {
            event.setCancelled(true);
            return;
        }
        if (bossPhases.indexOf(currentPhase) + 1 >= bossPhases.size()) return;

        BossPhase nextBossPhase = bossPhases.get(bossPhases.indexOf(currentPhase) + 1);
        double newHealth = Math.max((customBossEntity.getHealth() - event.getDamage()) / customBossEntity.getMaxHealth(), 0);
        if (newHealth > nextBossPhase.healthPercentage) return;

        event.setCancelled(true);
        switchPhase(nextBossPhase, RemovalReason.PHASE_BOSS_PHASE_END, nextBossPhase.healthPercentage);
    }

    public static class PhaseBossEntityListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEliteDamaged(EliteMobDamagedEvent event) {
            if (!(event.getEliteEntity() instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) event.getEliteEntity()).getPhaseBossEntity() == null) return;
            ((CustomBossEntity) event.getEliteEntity()).getPhaseBossEntity().checkPhaseBossSwitch(event);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onEliteRemove(EliteMobRemoveEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity customBossEntity)) return;
            PhaseBossEntity phases = customBossEntity.getPhaseBossEntity();
            if (phases == null) return;
            if (event.getRemovalReason() != RemovalReason.PHASE_BOSS_PHASE_END
                    && event.getRemovalReason() != RemovalReason.PHASE_BOSS_RESET) {
                // Phase removals retire only the outgoing body; the replacement owns the hold.
                phases.cancelPendingTransition();
                phases.releaseDestinationHold();
            }
            if (event.getRemovalReason() == RemovalReason.DEATH) phases.silentReset();
        }
    }

    public class BossPhase {
        public final CustomBossesConfigFields customBossesConfigFields;
        public final double healthPercentage;

        public BossPhase(CustomBossesConfigFields customBossesConfigFields, double healthPercentage) {
            this.customBossesConfigFields = customBossesConfigFields;
            this.healthPercentage = healthPercentage;
        }
    }
}
