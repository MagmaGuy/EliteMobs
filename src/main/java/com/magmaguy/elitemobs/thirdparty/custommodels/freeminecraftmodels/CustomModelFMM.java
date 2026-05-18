package com.magmaguy.elitemobs.thirdparty.custommodels.freeminecraftmodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityManager;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityLeftClickCallback;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityRightClickCallback;
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

    public CustomModelFMM(LivingEntity livingEntity, String modelName, String nametagName,
                          ModeledEntityLeftClickCallback leftClickCallback,
                          ModeledEntityRightClickCallback rightClickCallback) {
        dynamicEntity = DynamicEntity.create(modelName, livingEntity);
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayName(nametagName);
        if (leftClickCallback != null) dynamicEntity.setLeftClickCallback(leftClickCallback);
        if (rightClickCallback != null) dynamicEntity.setRightClickCallback(rightClickCallback);
    }

    public static void reloadModels() {
        ModeledEntityManager.reload();
    }

    public static boolean modelExists(String modelName) {
        return ModeledEntityManager.modelExists(modelName);
    }

    @Override
    public void shoot() {
        if (dynamicEntity == null) return;
        if (dynamicEntity.hasAnimation("attack_ranged")) dynamicEntity.playAnimation("attack_ranged", false, false);
        else if (dynamicEntity.hasAnimation("attack")) dynamicEntity.playAnimation("attack", false, false);
    }

    @Override
    public void melee() {
        if (dynamicEntity == null) return;
        if (dynamicEntity.hasAnimation("attack_melee")) dynamicEntity.playAnimation("attack_melee", false, false);
        else if (dynamicEntity.hasAnimation("attack")) dynamicEntity.playAnimation("attack", false, false);
    }

    @Override
    public void playAnimationByName(String animationName) {
        if (dynamicEntity == null) return;
        if (!dynamicEntity.hasAnimation(animationName)) return;
        dynamicEntity.playAnimation(animationName, false, false);
    }

    @Override
    public void setName(String nametagName, boolean visible) {
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayName(nametagName);
    }

    @Override
    public void setNameVisible(boolean visible) {
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayNameVisible(visible);
    }

    @Override
    public void addPassenger(CustomBossEntity passenger) {
        //currently unimplemented
    }

    @Override
    public void switchPhase() {
        if (dynamicEntity == null) return;
        dynamicEntity.remove();
    }

    @Override
    public Location getNametagBoneLocation() {
        if (dynamicEntity == null) return null;
        List<Bone> nametagBones = dynamicEntity.getNametagBones();
        if (nametagBones == null || nametagBones.isEmpty()) return null;
        return nametagBones.get(0).getBoneLocation();
    }

    @Override
    public void setSyncMovement(boolean syncMovement) {
        if (dynamicEntity == null) return;
        dynamicEntity.setSyncMovement(syncMovement);
    }

    /**
     * Listens for {@link com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent}
     * and re-attaches the FMM display to every tracked NPC. Required because
     * FMM's reload tears down every {@code DynamicEntity}, leaving every NPC's
     * cached {@code customModel} pointing at a destroyed display entity — the
     * villager survives but is no longer visually wrapped, so it shows as an
     * invisible mob to viewers.
     * <p>
     * Registered in EventsRegistrer only when this class loads (i.e. FMM is on
     * the classpath); without FMM the class is unreferenced and no listener
     * registers.
     */
    public static class FmmReloadListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onFmmReloaded(com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent event) {
            for (com.magmaguy.elitemobs.npcs.NPCEntity npc :
                    new java.util.ArrayList<>(com.magmaguy.elitemobs.entitytracker.EntityTracker.getNpcEntities().values())) {
                try {
                    npc.refreshCustomModel();
                } catch (Throwable t) {
                    com.magmaguy.magmacore.util.Logger.warn(
                            "Failed to refresh FMM model after reload for NPC " + npc.getUuid() + ": " + t.getMessage());
                }
            }
        }
    }
}
