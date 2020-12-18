package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PhaseBossEntity {

    public static HashMap<UUID, PhaseBossEntity> phaseBosses = new HashMap<>();

    public UUID uuid = UUID.randomUUID();
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
        phaseBosses.put(uuid, this);
        customBossEntity.phaseBossID = uuid;
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

        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(currentPhase.customBossConfigFields.getFileName(), eliteMobEntity.getLivingEntity().getLocation(), eliteMobEntity.getLevel(), currentPhase.healthPercentage, uuid);
        customBossEntity.addDamagers(eliteMobEntity.getDamagers());

        if (regionalBossEntity != null)
            regionalBossEntity.customBossEntity = customBossEntity;

        eliteMobEntity.getLivingEntity().remove();
        EntityTracker.unregisterEliteMob(eliteMobEntity);
    }

    public void fullHeal(EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity.getLivingEntity().isInsideVehicle())
            eliteMobEntity.getLivingEntity().getVehicle().remove();
        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(bossPhases.get(0).customBossConfigFields.getFileName(), eliteMobEntity.getLivingEntity().getLocation(), eliteMobEntity.getLevel(), regionalBossEntity, true);
        if (regionalBossEntity != null) {
            regionalBossEntity.customBossEntity = customBossEntity;
            customBossEntity.getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2));
        }
        eliteMobEntity.remove();
        EntityTracker.unregisterEliteMob(eliteMobEntity);
    }

    public static class PhaseBossEntityListener implements Listener {
        @EventHandler
        public void onEliteDamaged(EliteMobDamagedEvent event) {
            if (event.isCancelled()) return;
            if (event.getEliteMobEntity().phaseBossID == null) return;
            phaseBosses.get(event.getEliteMobEntity().phaseBossID).checkPhaseBossSwitch(event.getEliteMobEntity());
        }

        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            if (event.getEliteMobEntity().phaseBossID == null) return;
            phaseBosses.remove(event.getEliteMobEntity().phaseBossID);
        }
    }

}
