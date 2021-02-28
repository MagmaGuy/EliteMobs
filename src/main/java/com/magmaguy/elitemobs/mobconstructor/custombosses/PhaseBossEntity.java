package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class PhaseBossEntity {

    public ArrayList<BossPhases> bossPhases = new ArrayList();
    private BossPhases currentPhase = null;
    private RegionalBossEntity regionalBossEntity = null;

    private class BossPhases {
        public CustomBossConfigFields customBossConfigFields;
        public double healthPercentage;

        public BossPhases(CustomBossConfigFields customBossConfigFields, double healthPercentage) {
            this.customBossConfigFields = customBossConfigFields;
            this.healthPercentage = healthPercentage;
        }
    }

    public PhaseBossEntity(CustomBossEntity customBossEntity, RegionalBossEntity regionalBossEntity) {
        this.regionalBossEntity = regionalBossEntity;
        initializePhaseBossEntity(customBossEntity);
    }

    public PhaseBossEntity(CustomBossEntity customBossEntity) {
        initializePhaseBossEntity(customBossEntity);
    }

    private void initializePhaseBossEntity(CustomBossEntity customBossEntity) {
        customBossEntity.phaseBossEntity = this;
        try {
            ArrayList<BossPhases> unsortedBossPhases = new ArrayList<>();
            unsortedBossPhases.add(new BossPhases(customBossEntity.customBossConfigFields, 1));
            for (String phaseConfigFile : customBossEntity.customBossConfigFields.getPhases()) {
                CustomBossConfigFields customBossConfigFields = CustomBossesConfig.getCustomBoss(phaseConfigFile.split(":")[0]);
                double healthPercentage = Double.parseDouble(phaseConfigFile.split(":")[1]);
                unsortedBossPhases.add(new BossPhases(customBossConfigFields, healthPercentage));
            }
            unsortedBossPhases.sort((o1, o2) -> (int) (o2.healthPercentage * 100 - o1.healthPercentage * 100));
            this.bossPhases = unsortedBossPhases;
            currentPhase = bossPhases.get(0);
        } catch (Exception ex) {
            new WarningMessage("Your phase boss " + customBossEntity.customBossConfigFields.getFileName() + " does not have a valid phases setup. Its phases will not work.");
            return;
        }
    }

    public void checkPhaseBossSwitch(EliteMobEntity eliteMobEntity) {

        if (bossPhases.indexOf(currentPhase) + 1 >= bossPhases.size()) return;
        BossPhases nextBossPhase = bossPhases.get(bossPhases.indexOf(currentPhase) + 1);
        if (eliteMobEntity.getHealth() / eliteMobEntity.getMaxHealth() > nextBossPhase.healthPercentage) return;

        currentPhase = nextBossPhase;

        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(currentPhase.customBossConfigFields.getFileName(), eliteMobEntity.getLivingEntity().getLocation(), eliteMobEntity.getLevel(), currentPhase.healthPercentage);
        if (customBossEntity == null) {
            new WarningMessage("Phase boss failed to transition into phase " + currentPhase + " ! It was generated from file " + currentPhase.customBossConfigFields.getFileName() + " . The boss won't spawn! Fix the phase file, the region settings or report this to the dev!");
            return;
        }
        customBossEntity.addDamagers(eliteMobEntity.getDamagers());

        if (regionalBossEntity != null)
            regionalBossEntity.customBossEntity = customBossEntity;

        customBossEntity.phaseBossEntity = this;

        eliteMobEntity.getLivingEntity().remove();
        EntityTracker.unregister(eliteMobEntity.uuid, RemovalReason.PHASE_BOSS_PHASE_END);
    }

    public void fullHeal(EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity.getLivingEntity().isInsideVehicle())
            eliteMobEntity.getLivingEntity().getVehicle().remove();
        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(bossPhases.get(0).customBossConfigFields, eliteMobEntity.getLivingEntity().getLocation(), eliteMobEntity.getLevel(), regionalBossEntity, true);
        if (regionalBossEntity != null) {
            regionalBossEntity.customBossEntity = customBossEntity;
            customBossEntity.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2));
        }
        eliteMobEntity.remove(true);
        EntityTracker.unregister(eliteMobEntity.uuid, RemovalReason.PHASE_BOSS_RESET);
    }

    /**
     * Phase bosses tend to die on a phase different from the first one. This means that the plugin is unable to detect
     * that bosses have died. As such, regional bosses have to be informed that they died.
     */
    public void deathHandler(){
        if (regionalBossEntity == null) return;
        if (currentPhase == bossPhases.get(0)) return;
        regionalBossEntity.respawnRegionalBoss();
    }

    public static class PhaseBossEntityListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEliteDamaged(EliteMobDamagedEvent event) {
            if (event.getEliteMobEntity().phaseBossEntity == null) return;
            event.getEliteMobEntity().phaseBossEntity.checkPhaseBossSwitch(event.getEliteMobEntity());
        }
    }

}
