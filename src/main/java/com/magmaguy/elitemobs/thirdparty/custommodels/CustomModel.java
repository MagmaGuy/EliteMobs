package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.freeminecraftmodels.CustomModelFMM;
import com.magmaguy.elitemobs.thirdparty.custommodels.modelengine.CustomModelMEG;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;


public class CustomModel implements CustomModelInterface {
    @Getter
    private static final boolean usingModels = false;
    @Getter
    private static ModelPlugin modelPlugin;
    private CustomModelMEG customModelMEG;
    private CustomModelFMM customModelFMM;
    private boolean initialized = false;

    private CustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS:
                customModelFMM = new CustomModelFMM(livingEntity, modelName, nametagName);
                initialized = true;
                break;
            case MODEL_ENGINE:
                customModelMEG = new CustomModelMEG(livingEntity, modelName, nametagName);
                initialized = true;
                break;
        }
    }

    public static void initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels"))
            modelPlugin = ModelPlugin.FREE_MINECRAFT_MODELS;
        else if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine") && Bukkit.getPluginManager().getPlugin("ModelEngine").getDescription().getVersion().contains("R3"))
            modelPlugin = ModelPlugin.MODEL_ENGINE;
        else modelPlugin = ModelPlugin.NONE;
    }

    public static void reloadModels() {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> CustomModelFMM.reloadModels();
            case MODEL_ENGINE -> CustomModelMEG.reloadModels();
        }
    }

    public static boolean modelExists(String modelName) {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS:
                return CustomModelFMM.modelExists(modelName);
            case MODEL_ENGINE:
                return CustomModelMEG.modelExists(modelName);
        }
        return false;
    }

    public static CustomModel generateCustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
        CustomModel customModel = new CustomModel(livingEntity, modelName, nametagName);
        return customModel.initialized ? customModel : null;
    }

    public static boolean customModelsEnabled() {
        return modelPlugin != ModelPlugin.NONE;
    }

    @Override
    public void shoot() {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.shoot();
            case MODEL_ENGINE -> customModelMEG.shoot();
        }
    }

    @Override
    public void melee() {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.melee();
            case MODEL_ENGINE -> customModelMEG.melee();
        }
    }

    @Override
    public void playAnimationByName(String animationName) {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.playAnimationByName(animationName);
            case MODEL_ENGINE -> customModelMEG.playAnimationByName(animationName);
        }
    }

    @Override
    public void setName(String nametagName, boolean visible) {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.setName(nametagName, visible);
            case MODEL_ENGINE -> customModelMEG.setName(nametagName, visible);
        }
    }

    @Override
    public void setNameVisible(boolean visible) {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.setNameVisible(visible);
            case MODEL_ENGINE -> customModelMEG.setNameVisible(visible);
        }
    }

    @Override
    public void addPassenger(CustomBossEntity passenger) {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.addPassenger(passenger);
            case MODEL_ENGINE -> customModelMEG.addPassenger(passenger);
        }
    }

    @Override
    public void switchPhase() {
        switch (modelPlugin) {
            case FREE_MINECRAFT_MODELS -> customModelFMM.switchPhase();
            case MODEL_ENGINE -> customModelMEG.switchPhase();
        }
    }

    public enum ModelPlugin {
        NONE, FREE_MINECRAFT_MODELS, MODEL_ENGINE
    }

    public static class ModelEntityEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onMeleeHit(EntityDamageByEntityEvent event) {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            if (!(eliteEntity instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) eliteEntity).getCustomModel() == null) return;
            ((CustomBossEntity) eliteEntity).getCustomModel().melee();
        }

        @EventHandler(ignoreCancelled = true)
        public void onRangedShot(EntitySpawnEvent event) {
            if (!(event.getEntity() instanceof Projectile)) return;
            if (!(((Projectile) event.getEntity()).getShooter() instanceof LivingEntity)) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getEntity()).getShooter());
            if (!(eliteEntity instanceof CustomBossEntity)) return;
            if (((CustomBossEntity) eliteEntity).getCustomModel() == null) return;
            ((CustomBossEntity) eliteEntity).getCustomModel().shoot();
        }

        @EventHandler
        public void onDeathEvent(EliteMobDeathEvent event) {
            if (event.getEliteEntity() instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null)
                customBossEntity.getCustomModel().playAnimationByName("death");
        }
    }
}
