package com.magmaguy.elitemobs.thirdparty.modelengine;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class CustomModel {
    public CustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        activeModel = ModelEngineAPI.api.getModelManager().createActiveModel(modelName);
        if (activeModel == null) {
            new WarningMessage("Failed to get valid model entity from " + modelName + " ! Is the model name correct, and has the model been installed correctly?");
            return;
        }
        modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(livingEntity);
        modeledEntity.addActiveModel(activeModel);
        modeledEntity.detectPlayers();
        modeledEntity.setInvisible(true);
        setName(nametagName, true);
    }

    ActiveModel activeModel;
    ModeledEntity modeledEntity;

    public static void reloadModels() {
        try{
        ModelEngineAPI.api.getModelManager().registerModels();} catch (Exception ex){
            new WarningMessage("Failed to reload models through ModelEngine!");
        }
    }

    public void shoot() {
        if (activeModel.getState("attack_ranged") != null)
            activeModel.addState("attack_ranged", 1, 1, 1);
        else
            activeModel.addState("attack", 1, 1, 1);
    }

    public void melee() {
        if (activeModel.getState("attack_melee") != null)
            activeModel.addState("attack_melee", 1, 1, 1);
        else
            activeModel.addState("attack", 1, 1, 1);
    }

    public void setName(String nametagName, boolean visible) {
        modeledEntity.setNametag(nametagName);
        modeledEntity.setNametagVisible(visible);
    }

    public void setNameVisible(boolean visible) {
        modeledEntity.setNametagVisible(visible);
    }

    public static class ModelEntityEvents implements Listener {
        @EventHandler
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            if (!(eliteEntity instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) eliteEntity).getCustomModel() == null) return;
            ((CustomBossEntity) eliteEntity).getCustomModel().melee();
        }

        @EventHandler
        public void onRangedShot(EntitySpawnEvent event) {
            if (!(event.getEntity() instanceof Projectile)) return;
            if (!(((Projectile) event.getEntity()).getShooter() instanceof LivingEntity)) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getEntity()).getShooter());
            if (!(eliteEntity instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) eliteEntity).getCustomModel() == null) return;
            ((CustomBossEntity) eliteEntity).getCustomModel().shoot();
        }
    }
}
