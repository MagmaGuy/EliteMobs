package com.magmaguy.elitemobs.thirdparty.custommodels.freeminecraftmodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityManager;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.customentity.core.Bone;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class CustomModelFMM implements CustomModelInterface {
    @Getter
    private final DynamicEntity dynamicEntity;

    public CustomModelFMM(LivingEntity livingEntity, String modelName, String nametagName) {
        dynamicEntity = DynamicEntity.create(modelName, livingEntity);
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayName(nametagName);
    }

    public static void reloadModels() {
        ModeledEntityManager.reload();
    }

    public static boolean modelExists(String modelName) {
        return ModeledEntityManager.modelExists(modelName);
    }

    @Override
    public void shoot() {
        if (dynamicEntity.hasAnimation("attack_ranged")) dynamicEntity.playAnimation("attack_ranged", false, false);
        else dynamicEntity.playAnimation("attack", false, false);
    }

    @Override
    public void melee() {
        if (dynamicEntity.hasAnimation("attack_melee")) dynamicEntity.playAnimation("attack_melee", false, false);
        else dynamicEntity.playAnimation("attack", false, false);
    }

    @Override
    public void playAnimationByName(String animationName) {
        dynamicEntity.playAnimation(animationName, false, false);
    }

    @Override
    public void setName(String nametagName, boolean visible) {
        dynamicEntity.setDisplayName(nametagName);
    }

    @Override
    public void setNameVisible(boolean visible) {
        dynamicEntity.setDisplayNameVisible(visible);
    }

    @Override
    public void addPassenger(CustomBossEntity passenger) {
        //currently unimplemented
    }

    @Override
    public void switchPhase() {
        dynamicEntity.remove();
    }

    @Override
    public Location getNametagBoneLocation() {
        if (dynamicEntity == null) return null;
        List<Bone> nametagBones = dynamicEntity.getNametagBones();
        if (nametagBones == null || nametagBones.isEmpty()) return null;
        return nametagBones.get(0).getBoneLocation();
    }
}
