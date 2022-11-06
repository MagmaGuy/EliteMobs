package com.magmaguy.elitemobs.thirdparty.modelengine;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.state.ModelState;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.Nameable;
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

    ModelBlueprint modelBlueprint;

    @Getter
    private boolean success = false;

    private CustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        try {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null) {
                new InfoMessage("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
                return;
            }
        } catch (NoSuchMethodError ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
            return;
        }

        modelBlueprint = ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName);

        activeModel = ModelEngineAPI.createActiveModel(modelBlueprint);

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
            modeledEntity.addModel(activeModel, true);
            activeModel.playDefaultAnimation(ModelState.IDLE);
            modeledEntity.setBaseEntityVisible(false);
            setName(nametagName, true);
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
            ModelEngineAPI.api.getGenerator().importModelsAsync();
        } catch (Exception ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
        }
    }

    public static boolean modelExists(String modelName) {
        if (modelName == null || modelName.isEmpty()) return false;
        try {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null) {
                new InfoMessage("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
                return false;
            }
        } catch (NoSuchMethodError ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0, documentation for other versions doesn't exist.");
            return false;
        }

        return true;
    }

    public void shoot() {
        if (activeModel == null) return;

        if (modelBlueprint.getAnimations().containsKey("attack_ranged"))
            activeModel.getAnimationHandler().playAnimation("attack_ranged", .1, .1, 1, true);
        else
            activeModel.getAnimationHandler().playAnimation("attack", .1, .1, 1, true);
    }

    public void melee() {

        if (activeModel == null) return;
        if (modelBlueprint.getAnimations().containsKey("attack_melee"))
            activeModel.getAnimationHandler().playAnimation("attack_melee", .1, .1, 1, true);
        else
            activeModel.getAnimationHandler().playAnimation("attack", .1, .1, 1, true);
    }

    public void playAnimationByName(String string) {
        if (activeModel == null) return;
        if (!modelBlueprint.getAnimations().containsKey(string)) return;
        activeModel.getAnimationHandler().playAnimation(string, .1, .1, 1, true);
    }

    public void setName(String nametagName, boolean visible) {
        if (modeledEntity == null) return;
        Nameable nametag = getNameableBone();
        if (nametag == null) {
            new WarningMessage("Failed to get hitbox nametag for disguise!");
            return;
        }
        nametag.setCustomName(nametagName);
        nametag.setCustomNameVisible(visible);
    }

    public void setNameVisible(boolean visible) {
        Nameable nametag = getNameableBone();
        if (nametag == null) {
            return;
        }
        nametag.setCustomNameVisible(visible);
    }

    private Nameable getNameableBone() {
        //Developer.message("Nameable bones: " + activeModel.getNametagHandler().getBones().size());
        for (Nameable nameable : activeModel.getNametagHandler().getBones().values())
            return nameable;
        return null;
    }

    public void addPassenger(CustomBossEntity passenger) {
        if (passenger.getCustomBossesConfigFields().getCustomModelMountPointID() == null) {
            new WarningMessage("Attempted to add " + passenger.getCustomBossesConfigFields().getFilename() + " as a mounted entity for a custom model but it does not have customModelMountPointID set! The boss can't guess where it needs to be mounted, and therefore this will not work.");
            return;
        }
        modeledEntity.getMountManager().addPassengerToSeat(
                modelBlueprint.getModelId(),
                passenger.getCustomBossesConfigFields().getCustomModelMountPointID(),
                passenger.getLivingEntity(),
                modeledEntity.getMountManager().getDriverController());
    }

    public void switchPhase() {
        activeModel.getAnimationHandler().forceStopAllAnimations();
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
