package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.ElitePhaseSwitchEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WarningMessage;
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
    private List<BossPhase> bossPhases = new ArrayList();
    private BossPhase currentPhase = null;

    public PhaseBossEntity(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        try {
            ArrayList<BossPhase> unsortedBossPhases = new ArrayList<>();
            unsortedBossPhases.add(new BossPhase(customBossEntity.getCustomBossesConfigFields(), 1));
            for (String phaseConfigFile : customBossEntity.getCustomBossesConfigFields().getPhases()) {
                CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(phaseConfigFile.split(":")[0]);
                if (customBossesConfigFields == null) {
                    new WarningMessage("Phase boss " + customBossEntity.getCustomBossesConfigFields() + " has an invalid config entry for phase " + phaseConfigFile.split(":")[0] + " - this file could not be found. The boss will not be able to do this phase until it is fixed!");
                }
                double healthPercentage = Double.parseDouble(phaseConfigFile.split(":")[1]);
                unsortedBossPhases.add(new BossPhase(customBossesConfigFields, healthPercentage));
            }
            unsortedBossPhases.sort((o1, o2) -> (int) (o2.healthPercentage * 100 - o1.healthPercentage * 100));
            this.bossPhases = unsortedBossPhases;
            currentPhase = bossPhases.get(0);
        } catch (Exception ex) {
            new WarningMessage("Your phase boss " + customBossEntity.customBossesConfigFields.getFilename() + " does not have a valid phases setup. Its phases will not work.");
        }
    }

    public boolean isInFirstPhase() {
        return bossPhases.get(0).equals(currentPhase);
    }

    private void switchPhase(BossPhase bossPhase, RemovalReason removalReason, double healthPercentage) {
        if (bossPhase.equals(currentPhase)) {
            new WarningMessage("Attempted to change the boss phase to what it already was.", true);
            return;
        }
        customBossEntity.remove(removalReason);
        if (bossPhase.customBossesConfigFields == null) {
            new WarningMessage("A phase for phase boss " + bossPhases.get(0).customBossesConfigFields.getFilename() + " was not valid! The boss will not be able to switch phases until it is fixed.");
            return;
        }
        customBossEntity.setCustomBossesConfigFields(bossPhase.customBossesConfigFields);
        if (removalReason.equals(RemovalReason.PHASE_BOSS_RESET)) {
            customBossEntity.setBossMusic(new BossMusic(bossPhase.customBossesConfigFields.getSong()));
            customBossEntity.spawn(true);
        } else {
            if (bossPhase.customBossesConfigFields.getPhaseSpawnLocation() != null) {
                Location location = ConfigurationLocation.serialize(bossPhase.customBossesConfigFields.getPhaseSpawnLocation(), true);
                if (bossPhase.customBossesConfigFields.getPhaseSpawnLocation() != null &&
                        bossPhase.customBossesConfigFields.getPhaseSpawnLocation().split(",")[0].equalsIgnoreCase("same_as_boss"))
                    location.setWorld(customBossEntity.getLocation().getWorld());
                if (location != null) {
                    customBossEntity.setSpawnLocation(location);
                    customBossEntity.setRespawnOverrideLocation(location);
                    customBossEntity.setPersistentLocation(location);
                } else {
                    customBossEntity.setRespawnOverrideLocation(customBossEntity.getLocation());
                    customBossEntity.setPersistentLocation(location);
                }
            } else
                customBossEntity.setRespawnOverrideLocation(customBossEntity.getLocation());
            //Handle music, soundtrack shouldn't change if the new one is the same
            if (bossPhase.customBossesConfigFields.getSong() != null
                    && currentPhase.customBossesConfigFields.getSong() != null &&
                    !bossPhase.customBossesConfigFields.getSong().equals(currentPhase.customBossesConfigFields.getSong())) {
                if (customBossEntity.getBossMusic() != null) customBossEntity.getBossMusic().stop();
                customBossEntity.setBossMusic(new BossMusic(bossPhase.customBossesConfigFields.getSong()));
            }
            customBossEntity.spawn(true);
        }
        customBossEntity.setHealth(customBossEntity.getMaxHealth() * healthPercentage);
        currentPhase = bossPhase;
        if (customBossEntity.getCustomModel() != null) customBossEntity.getCustomModel().switchPhase();
        ElitePhaseSwitchEvent elitePhaseSwitchEvent = new ElitePhaseSwitchEvent(customBossEntity, this);
        new EventCaller(elitePhaseSwitchEvent);
    }

    public void resetToFirstPhase() {
        switchPhase(bossPhases.get(0), RemovalReason.PHASE_BOSS_RESET, 1);
    }

    public void silentReset() {
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

    public void checkPhaseBossSwitch() {
        if (bossPhases.indexOf(currentPhase) + 1 >= bossPhases.size()) return;
        BossPhase nextBossPhase = bossPhases.get(bossPhases.indexOf(currentPhase) + 1);
        if (customBossEntity.getHealth() / customBossEntity.getMaxHealth() > nextBossPhase.healthPercentage) return;
        switchPhase(nextBossPhase, RemovalReason.PHASE_BOSS_PHASE_END, customBossEntity.getHealth() / customBossEntity.getMaxHealth());
    }

    public static class PhaseBossEntityListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEliteDamaged(EliteMobDamagedEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) event.getEliteMobEntity()).getPhaseBossEntity() == null) return;
            ((CustomBossEntity) event.getEliteMobEntity()).getPhaseBossEntity().checkPhaseBossSwitch();
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onEliteDeath(EliteMobDeathEvent event) {
            if (!(event.getEliteEntity() instanceof CustomBossEntity customBossEntity)) return;
            if (customBossEntity.getPhaseBossEntity() != null)
                customBossEntity.phaseBossEntity.silentReset();
        }
    }

    public class BossPhase {
        public CustomBossesConfigFields customBossesConfigFields;
        public double healthPercentage;

        public BossPhase(CustomBossesConfigFields customBossesConfigFields, double healthPercentage) {
            this.customBossesConfigFields = customBossesConfigFields;
            this.healthPercentage = healthPercentage;
        }
    }
}
