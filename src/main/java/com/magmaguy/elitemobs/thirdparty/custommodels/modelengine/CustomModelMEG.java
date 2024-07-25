package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.magmaguy.magmacore.util.Logger;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.state.ModelState;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.Nameable;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

public class CustomModelMEG implements CustomModelInterface {

    ActiveModel activeModel;
    ModeledEntity modeledEntity;

    ModelBlueprint modelBlueprint;

    @Getter
    private boolean success = false;

    public CustomModelMEG(LivingEntity livingEntity, String modelName, String nametagName) {
        try {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null) {
                Logger.info("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
                return;
            }
        } catch (NoSuchMethodError ex) {
            Logger.warn("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
            return;
        }

        modelBlueprint = ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName);

        activeModel = ModelEngineAPI.createActiveModel(modelBlueprint);

        if (activeModel == null) {
            Logger.warn("Failed to load model from " + modelName + " ! Is the model name correct, and has the model been installed correctly?");
            return;
        }

        modeledEntity = ModelEngineAPI.createModeledEntity(livingEntity);

        if (modeledEntity == null) {
            Logger.warn("Failed to create model entity " + modelName + " ! This means the entity that was meant to get disguised has a problem!");
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
            Logger.warn("Failed to make model entity " + modelName + " ! Couldn't assign model or visibility status.");
            exception.printStackTrace();
        }

    }

    public static CustomModelMEG generateCustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        CustomModelMEG customModel = new CustomModelMEG(livingEntity, modelName, nametagName);
        return customModel.isSuccess() ? customModel : null;
    }

    public static void reloadModels() {
        try {
            ModelEngineAPI.api.getGenerator().importModelsAsync();
        } catch (Exception ex) {
            Logger.warn("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
        }
    }

    public static boolean modelExists(String modelName) {
        if (modelName == null || modelName.isEmpty()) return false;
        try {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null) {
                Logger.info("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
                return false;
            }
        } catch (NoSuchMethodError ex) {
            Logger.warn("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0, documentation for other versions doesn't exist.");
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

    @Override
    public void setName(String nametagName, boolean visible) {
        if (modeledEntity == null) return;
        Nameable nametag = getNameableBone();
        if (nametag == null) {
            Logger.warn("Failed to get hitbox nametag for disguise!");
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
        for (Nameable nameable : activeModel.getNametagHandler().getBones().values()) return nameable;
        return null;
    }

    public void addPassenger(CustomBossEntity passenger) {
        if (passenger.getCustomBossesConfigFields().getCustomModelMountPointID() == null) {
            Logger.warn("Attempted to add " + passenger.getCustomBossesConfigFields().getFilename() + " as a mounted entity for a custom model but it does not have customModelMountPointID set! The boss can't guess where it needs to be mounted, and therefore this will not work.");
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

}
