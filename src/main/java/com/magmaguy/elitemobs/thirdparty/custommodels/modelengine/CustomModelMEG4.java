package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.NameTag;
import com.ticxo.modelengine.api.mount.controller.MountControllerType;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

public class CustomModelMEG4 implements CustomModelInterface {
    ActiveModel activeModel;
    ModeledEntity modeledEntity;
    ModelBlueprint modelBlueprint;

    @Getter
    private boolean success = false;

    public CustomModelMEG4(LivingEntity livingEntity, String modelName, String nametagName) {
        try {
            if (ModelEngineAPI.getAPI().getModelRegistry().get(modelName) == null) {
                new InfoMessage("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
                return;
            }
        } catch (NoSuchMethodError ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
            return;
        }

        modelBlueprint = ModelEngineAPI.getAPI().getModelRegistry().get(modelName);

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
            activeModel.getAnimationHandler().playAnimation("idle", .1, .1, 1, true);
            modeledEntity.setBaseEntityVisible(false);
            setName(nametagName, true);
            success = true;
        } catch (Exception exception) {
            modeledEntity.removeModel(modelName);
            new WarningMessage("Failed to make model entity " + modelName + " ! Couldn't assign model or visibility status.");
            exception.printStackTrace();
        }

    }

    public static void reloadModels() {
        try {
            ModelEngineAPI.getAPI().getModelGenerator().importModels();
        } catch (Exception ex) {
            new WarningMessage("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
        }
    }

    public static boolean modelExists(String modelName) {
        if (modelName == null || modelName.isEmpty()) return false;
        try {
            if (ModelEngineAPI.getAPI().getModelRegistry().get(modelName) == null) {
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
        else activeModel.getAnimationHandler().playAnimation("attack", .1, .1, 1, true);
    }

    public void melee() {
        if (activeModel == null) return;
        if (modelBlueprint.getAnimations().containsKey("attack_melee"))
            activeModel.getAnimationHandler().playAnimation("attack_melee", .1, .1, 1, true);
        else activeModel.getAnimationHandler().playAnimation("attack", .1, .1, 1, true);
    }

    public void playAnimationByName(String string) {
        if (activeModel == null) return;
        if (!modelBlueprint.getAnimations().containsKey(string)) return;
        activeModel.getAnimationHandler().playAnimation(string, .1, .1, 1, true);
    }

    @Override
    public void setName(String nameTagName, boolean visible) {
        if (modeledEntity == null) return;
        NameTag nametag = getNameTagBone();
        if (nametag == null) {
            new WarningMessage("Failed to get position for name tag: " + activeModel.getBlueprint().getName());
            return;
        }
        nametag.setString(nameTagName);
        nametag.setVisible(visible);
    }

    public void setNameVisible(boolean visible) {
        NameTag nameTagBone = getNameTagBone();
        if (nameTagBone == null) return;
        nameTagBone.setVisible(visible);
    }

    private NameTag getNameTagBone() {
        ModelBone bone = activeModel.getBone("name").stream().filter(modelBone -> modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG).orElse(null) != null).findFirst().orElse(null);
        if (bone == null) return null;
        return bone.getBoneBehavior(BoneBehaviorTypes.NAMETAG).orElse(null);
    }

    public void addPassenger(CustomBossEntity passenger) {
        if (passenger.getCustomBossesConfigFields().getCustomModelMountPointID() == null) {
            new WarningMessage("Attempted to add " + passenger.getCustomBossesConfigFields().getFilename() + " as a mounted entity for a custom model but it does not have customModelMountPointID set! The boss can't guess where it needs to be mounted, and therefore this will not work.");
            return;
        }
        MountManager mountManager = modeledEntity.getMountData().getMainMountManager();
        if (mountManager == null) return;
        MountControllerType controllerType = ModelEngineAPI.getMountControllerTypeRegistry().get("walking");
        try {
            mountManager.mountDriver(passenger.getLivingEntity(), controllerType);
            mountManager.mountDriver(passenger.getLivingEntity(), controllerType, mountController -> {
                mountController.setCanDamageMount(true);
//                mountController.setCanInteractMount(false);
            });
        } catch (Exception ignored) {
        }

    }

    public void switchPhase() {
        activeModel.getAnimationHandler().forceStopAllAnimations();
    }
}
