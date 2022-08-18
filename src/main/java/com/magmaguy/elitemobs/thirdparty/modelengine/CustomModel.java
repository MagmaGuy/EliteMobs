package com.magmaguy.elitemobs.thirdparty.modelengine;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class CustomModel {

    ActiveModel activeModel;
    ModeledEntity modeledEntity;
    /*
    ModelBlueprint modelBlueprint;
     */
    @Getter
    private boolean success = false;
//todo: code for ModelEngine B3.0.0 is here, not live because of several issues like having to generate a new resource pack and also the code not fully working

    private CustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        try {
            /*
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null)
             */
            if (ModelEngineAPI.api.getModelManager().getModelRegistry().getModelBlueprint(modelName) == null)
                new InfoMessage("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
        } catch (NoSuchMethodError ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine B3.0.0, documentation for other versions doesn't exist.");
            return;
        }

        /*
        modelBlueprint = ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName);
         */
        activeModel = ModelEngineAPI.api.getModelManager().createActiveModel(modelName);
        /*
        activeModel = ModelEngineAPI.createActiveModel(modelName);
         */
        if (activeModel == null) {
            new WarningMessage("Failed to load model from " + modelName + " ! Is the model name correct, and has the model been installed correctly?");
            return;
        }

        modeledEntity = ModelEngineAPI.createModeledEntity(livingEntity);

        if (modeledEntity == null) {
            new WarningMessage("Failed to create model entity " + modelName + " ! This means the entity that was meant to get disguised has a problem!");
            return;
        }

        try {
            /*
            modeledEntity.addModel(activeModel, true);
            modeledEntity.setBaseEntityVisible(false);
            activeModel.playDefaultAnimation(ModelState.IDLE);
            setName(nametagName, true);
            success = true;
             */
            modeledEntity.addActiveModel(activeModel);
            modeledEntity.detectPlayers();
            modeledEntity.setInvisible(true);
            success = true;
        } catch (Exception exception) {
            modeledEntity.removeModel(modelName);
            new WarningMessage("Failed to make model entity " + modelName + " ! Couldn't assign model or visibility status.");
            exception.printStackTrace();
        }

    }

    public static CustomModel generateCustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        CustomModel customModel = new CustomModel(livingEntity, modelName, nametagName);
        return customModel.isSuccess() ? customModel : null;
    }

    public static void reloadModels() {
        try {
            /*
            ModelEngineAPI.api.getGenerator().importModelsAsync();
             */
            ModelEngineAPI.api.getModelManager().registerModels();
        } catch (Exception ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R2.5.0, documentation for other versions doesn't exist.");
        }
    }

    public void shoot() {
        if (activeModel == null) return;
        /*
        if (modelBlueprint.getAnimations().containsKey("attack_ranged"))
            activeModel.getAnimationHandler().playAnimation("attack_ranged", 1, 1, 1);
        else
            activeModel.getAnimationHandler().playAnimation("attack", 1, 1, 1);
         */
        if (activeModel.getState("attack_ranged") != null)
            activeModel.addState("attack_ranged", 1, 1, 1);
        else
            activeModel.addState("attack", 1, 1, 1);
    }

    public void melee() {
        /*
        if (activeModel == null) return;
        if (modelBlueprint.getAnimations().containsKey("attack_melee"))
            activeModel.getAnimationHandler().playAnimation("attack_melee", 1, 1, 1);
        else
            activeModel.getAnimationHandler().playAnimation("attack", 1, 1, 1);
         */
        if (activeModel.getState("attack_melee") != null)
            activeModel.addState("attack_melee", 1, 1, 1);
        else
            activeModel.addState("attack", 1, 1, 1);
    }

    public void setName(String nametagName, boolean visible) {
        if (modeledEntity == null) return;
        /*
        activeModel.getNametagHandler().getBones().get("hitbox").setCustomName(nametagName);
        activeModel.getNametagHandler().getBones().get("hitbox").setCustomNameVisible(visible);
         */
        modeledEntity.getNametagHandler().setCustomName("hitbox", nametagName);
        modeledEntity.getNametagHandler().setCustomNameVisibility("hitbox", true);
    }

    public void switchPhase() {
        //activeModel.getAnimationHandler().forceStopAllAnimations();
    }

    public void setNameVisible(boolean visible) {
        /*
        activeModel.getNametagHandler().getBones().get("hitbox").setCustomNameVisible(visible);

         */
        modeledEntity.getNametagHandler().setCustomNameVisibility("hitbox", visible);
    }

    public static boolean modelIsLoaded(String modelName) {
        if (modelName == null || modelName.isEmpty()) return false;
        try {
            /*
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null)
             */
            if (ModelEngineAPI.api.getModelManager().getModelRegistry().getModelBlueprint(modelName) == null)
                new InfoMessage("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
        } catch (NoSuchMethodError ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine B3.0.0, documentation for other versions doesn't exist.");
            return false;
        }

        /*
        modelBlueprint = ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName);
         */
        ActiveModel activeModel = ModelEngineAPI.api.getModelManager().createActiveModel(modelName);
        /*
        activeModel = ModelEngineAPI.createActiveModel(modelName);
         */
        if (activeModel == null) {
            new WarningMessage("Failed to load model from " + modelName + " ! Is the model name correct, and has the model been installed correctly?");
            return false;
        }
        return true;
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
