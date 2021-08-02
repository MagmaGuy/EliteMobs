package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class PhaseBossEntity {

    private final CustomBossEntity customBossEntity;
    private ArrayList<BossPhase> bossPhases = new ArrayList();
    private BossPhase currentPhase = null;

    public PhaseBossEntity(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        try {
            ArrayList<BossPhase> unsortedBossPhases = new ArrayList<>();
            unsortedBossPhases.add(new BossPhase(customBossEntity.getCustomBossesConfigFields().getFilename(), 1));
            for (String phaseConfigFile : customBossEntity.getCustomBossesConfigFields().getPhases()) {
                String customBossesConfigFields = phaseConfigFile.split(":")[0];
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
        EntityTracker.unregister(customBossEntity.getLivingEntity(), RemovalReason.PHASE_BOSS_PHASE_END);
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(bossPhase.customBossesConfigFields);
        if (customBossesConfigFields == null) {
            new WarningMessage("A phase for phase boss " + bossPhases.get(0).customBossesConfigFields + " was not valid! The boss will not be able to switch phases until it is fixed.");
            return;
        }
        customBossEntity.setCustomBossesConfigFields(customBossesConfigFields);
        if (removalReason.equals(RemovalReason.PHASE_BOSS_RESET))
            customBossEntity.spawn(customBossEntity.getSpawnLocation(), true);
        else{
            Location previousSpawnLocation = customBossEntity.getSpawnLocation().clone();
            customBossEntity.spawn(customBossEntity.getLocation(), true);
            customBossEntity.setSpawnLocation(previousSpawnLocation);
        }
        customBossEntity.setHealth(customBossEntity.getMaxHealth() * healthPercentage);
        currentPhase = bossPhase;
    }

    public void resetToFirstPhase() {
        customBossEntity.getDamagers().clear();
        switchPhase(bossPhases.get(0), RemovalReason.PHASE_BOSS_RESET, 1);
    }

    public void deathReset(){
        currentPhase = bossPhases.get(0);
        customBossEntity.setCustomBossesConfigFields(customBossEntity.getCustomBossesConfigFields());
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
    }

    private class BossPhase {
        public String customBossesConfigFields;
        public double healthPercentage;

        public BossPhase(String customBossesConfigFields, double healthPercentage) {
            this.customBossesConfigFields = customBossesConfigFields;
            this.healthPercentage = healthPercentage;
        }
    }

}
