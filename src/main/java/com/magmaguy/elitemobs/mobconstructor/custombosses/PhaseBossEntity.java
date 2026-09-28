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
import org.bukkit.entity.LivingEntity;
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
    // Bukkit tickets are per plugin/chunk, so concurrent phases share only tickets we acquired.
    private static final Map<DestinationTicket, Integer> destinationTickets = new HashMap<>();
    private final CustomBossEntity customBossEntity;
    @Getter
    private final List<BossPhase> bossPhases;
    private BossPhase currentPhase = null;
    private Location originalSpawnLocation;
    private Double pendingHealthFraction;
    private PendingTransition pendingTransition;
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
        // Logical transition owns the intent even if materialization waits for a loaded chunk.
        customBossEntity.spawn(true);
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

    private final class PendingTransition implements Runnable {
        private final BossPhase expectedPhase = currentPhase;
        private final LivingEntity expectedBody = customBossEntity.getLivingEntity();
        private final BossPhase nextPhase;
        private final RemovalReason removalReason;
        private final double healthPercentage;
        private final Location destination;
        private final Plugin plugin = MetadataHandler.PLUGIN;
        private final DestinationTicket ticket;
        private BukkitTask task;
        private boolean ownedTicket;
        private int elapsedTicks;

        private PendingTransition(BossPhase nextPhase, RemovalReason removalReason,
                                  double healthPercentage, Location destination) {
            this.nextPhase = nextPhase;
            this.removalReason = removalReason;
            this.healthPercentage = healthPercentage;
            this.destination = destination;
            ticket = new DestinationTicket(destination.getWorld().getUID(),
                    destination.getBlockX() >> 4, destination.getBlockZ() >> 4, plugin);
        }

        private void acquireTicket() {
            Integer owners = destinationTickets.get(ticket);
            if (owners != null) {
                destinationTickets.put(ticket, owners + 1);
                ownedTicket = true;
            } else if (!destination.getWorld().getPluginChunkTickets(ticket.x(), ticket.z()).contains(plugin)) {
                // Bukkit inserts the ticket before loading. Own cleanup before the load
                // can throw or synchronously invoke a listener which cancels this phase.
                destinationTickets.put(ticket, 1);
                ownedTicket = true;
                if (!destination.getWorld().addPluginChunkTicket(ticket.x(), ticket.z(), plugin))
                    throw new IllegalStateException("Phase destination chunk ticket could not be acquired");
            }
        }

        private boolean stillCurrent() {
            return customBossEntity.isValid() && customBossEntity.getLivingEntity() == expectedBody
                    && currentPhase == expectedPhase && plugin.isEnabled()
                    && Bukkit.getWorld(ticket.worldId()) == destination.getWorld();
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
                    // Phase scripts run synchronously during materialization; their player
                    // teleport can take over chunk admission before our ticket is released.
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
            if (!ownedTicket) return;
            ownedTicket = false;
            Integer owners = destinationTickets.get(ticket);
            if (owners == null) return;
            if (owners > 1) {
                destinationTickets.put(ticket, owners - 1);
            } else {
                destinationTickets.remove(ticket);
                destination.getWorld().removePluginChunkTicket(ticket.x(), ticket.z(), plugin);
            }
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
                    && event.getRemovalReason() != RemovalReason.PHASE_BOSS_RESET)
                phases.cancelPendingTransition();
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
