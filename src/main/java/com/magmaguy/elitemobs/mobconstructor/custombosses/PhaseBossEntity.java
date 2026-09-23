package com.magmaguy.elitemobs.mobconstructor.custombosses;

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
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class PhaseBossEntity {

    private final CustomBossEntity customBossEntity;
    @Getter
    private final List<BossPhase> bossPhases;
    private BossPhase currentPhase = null;
    private Location originalSpawnLocation;
    private Double pendingHealthFraction;
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
        try {
            if (destination == null || destination.getWorld() == null)
                throw new IllegalArgumentException("Phase destination has no loaded world");
            destination.checkFinite();
            destination = destination.clone();
        } catch (IllegalArgumentException failure) {
            if (reportedInvalidLocations.add(bossPhase))
                Logger.warn("Refused phase transition from " + currentPhase.customBossesConfigFields.getFilename()
                        + " to " + bossPhase.customBossesConfigFields.getFilename() + ": " + failure.getMessage()
                        + ". The current phase was retained; correct phaseSpawnLocation.");
            return;
        }
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
        switchPhase(bossPhases.get(0), RemovalReason.PHASE_BOSS_RESET, 1);
    }

    public void silentReset() {
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
            if (!event.getRemovalReason().equals(RemovalReason.DEATH)) return;
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity customBossEntity)) return;
            if (customBossEntity.getPhaseBossEntity() != null)
                customBossEntity.phaseBossEntity.silentReset();
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
