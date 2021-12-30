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

public class ModelEntity {
    ActiveModel activeModel;
    ModeledEntity modeledEntity;

    public ModelEntity(LivingEntity livingEntity, String modelName) {
        activeModel = ModelEngineAPI.api.getModelManager().createActiveModel(modelName);
        if (activeModel == null) {
            new WarningMessage("Failed to get valid model entity from " + modelName + " ! Is the model name correct, and has the model been installed correctly?");
            return;
        }
        modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(livingEntity);

        modeledEntity.addActiveModel(activeModel);
        modeledEntity.detectPlayers();
        modeledEntity.setInvisible(true);
    }

    public void shoot() {
        activeModel.addState("attack", 1, 1, 1);
    }

    public void melee() {
        activeModel.addState("attack", 1, 1, 1);
    }

    public static class ModelEntityEvents implements Listener {
        @EventHandler
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            if (!(eliteEntity instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) eliteEntity).getModelEntity() == null) return;
            ((CustomBossEntity) eliteEntity).getModelEntity().melee();
        }

        @EventHandler
        public void onRangedShot(EntitySpawnEvent event) {
            if (!(event.getEntity() instanceof Projectile)) return;
            if (!(((Projectile) event.getEntity()).getShooter() instanceof LivingEntity)) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getEntity()).getShooter());
            if (!(eliteEntity instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) eliteEntity).getModelEntity() == null) return;
            ((CustomBossEntity) eliteEntity).getModelEntity().shoot();
        }
    }
}
