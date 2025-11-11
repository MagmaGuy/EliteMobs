package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;

public class CustomBossMegaConsumer {
    private final CustomBossesConfigFields customBossesConfigFields;
    private final HashSet<ElitePower> powers;
    private final int level;
    private final boolean bypassesWorldGuardSpawn;
    private final Location spawnLocation;
    CustomBossEntity customBossEntity;

    /**
     * The objective of this class is to set as many fields as possible as a consumer for spawning a Custom Boss.
     * <p>
     * By setting the values through a consumer, the entity shows up already modified by the time it gets to the spawn event.
     *
     * @param customBossEntity {@link CustomBossEntity spawning the {@link LivingEntity}}
     */
    public CustomBossMegaConsumer(CustomBossEntity customBossEntity) {
        this.customBossesConfigFields = customBossEntity.getCustomBossesConfigFields();
        this.customBossEntity = customBossEntity;
        if (customBossEntity.getRespawnOverrideLocation() != null) {
            this.spawnLocation = customBossEntity.getRespawnOverrideLocation();
            customBossEntity.setRespawnOverrideLocation(null);
        } else {
            this.spawnLocation = customBossEntity.getSpawnLocation();
        }
        this.powers = customBossEntity.getElitePowers();
        this.level = customBossEntity.getLevel();
        this.bypassesWorldGuardSpawn = customBossEntity.getBypassesProtections();
    }

    protected static void setName(LivingEntity livingEntity, CustomBossEntity customBossEntity, int level) {
        String parsedName = ChatColorConverter.convert(customBossEntity.customBossesConfigFields.getName().replace("$level", level + "")
                .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + level + "&2]&f"))
                .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + level + "&6〗&f"))
                .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + level + "&4』&f"))
                .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + level + "&8〕&f")
                .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + level + "&4」&f")));
        livingEntity.setCustomName(parsedName);
        livingEntity.setCustomNameVisible(DefaultConfig.isAlwaysShowNametags());
        if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises"))
            DisguiseEntity.setDisguiseNameVisibility(DefaultConfig.isAlwaysShowNametags(), livingEntity, parsedName);
        customBossEntity.setName(parsedName, false);
    }

    /**
     * Attempts to spawn a {@link CustomBossEntity} whose spawn location has already been set.
     *
     * @return Whether the spawn succeeded or not.
     */
    public LivingEntity spawn() {
        if (spawnLocation == null) {
            Logger.warn("Custom Boss Entity " + customBossesConfigFields.getFilename() + " tried to spawn without a valid spawn location getting assigned! Report this to the developer!");
            return null;
        }
        if (EliteMobs.worldGuardIsEnabled) {
            if (!WorldGuardFlagChecker.doEliteMobsSpawnFlag(spawnLocation)) {
                Logger.warn("Attempted to spawn " + customBossesConfigFields.getFilename() + " in location " +
                        spawnLocation + " which is protected by WorldGuard with elitemobs-spawning deny! This should not have happened.");
                return null;
            }
            if (bypassesWorldGuardSpawn || customBossEntity instanceof RegionalBossEntity)
                WorldGuardSpawnEventBypasser.forceSpawn();
        }

        LivingEntity livingEntity = (LivingEntity) spawnLocation.getWorld().spawn(spawnLocation,
                customBossesConfigFields.getEntityType().getEntityClass(),
                entity -> applyBossFeatures((LivingEntity) entity));
        setCustomModel(livingEntity);
        customBossEntity.setLivingEntity(livingEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return livingEntity;
    }

    private void setBaby(LivingEntity livingEntity) {
        if (livingEntity instanceof Ageable)
            if (customBossesConfigFields.isBaby())
                ((Ageable) livingEntity).setBaby();
            else
                ((Ageable) livingEntity).setAdult();
    }


    private void setDisguise(LivingEntity livingEntity) {
        if (customBossesConfigFields.getDisguise() == null ||
                CustomModel.customModelsEnabled() &&
                        customBossesConfigFields.isCustomModelExists() &&
                        customBossesConfigFields.getCustomModel() != null &&
                        !customBossesConfigFields.getCustomModel().isEmpty())
            return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        try {
            DisguiseEntity.disguise(customBossesConfigFields.getDisguise(), livingEntity, customBossesConfigFields.getCustomDisguiseData(), customBossesConfigFields.getFilename());
        } catch (Exception ex) {
            Logger.warn("Failed to load LibsDisguises disguise correctly!");
        }
    }

    private void setCustomModel(LivingEntity livingEntity) {
        if (!customBossesConfigFields.isCustomModelExists()) return;
        if (customBossesConfigFields.getCustomModel() == null || customBossesConfigFields.getCustomModel().isEmpty())
            return;
        try {
            customBossEntity.setCustomModel(CustomModel.generateCustomModel(livingEntity, customBossesConfigFields.getCustomModel(), customBossEntity.getName()));
        } catch (Exception exception) {
            customBossEntity.setCustomModel(null);
            Logger.warn("Failed to initialize Custom Model for Custom Boss " + customBossesConfigFields.getFilename());
            exception.printStackTrace();
        }
    }

    private void setFrozen(LivingEntity livingEntity) {
        if (!customBossesConfigFields.isFrozen()) return;
        AttributeManager.setAttribute(livingEntity, "generic_movement_speed", 0);
        livingEntity.setCollidable(false);
    }

    private void setEquipment(LivingEntity livingEntity) {
        try {
            livingEntity.getEquipment().setHelmet(customBossesConfigFields.getHelmet());
            livingEntity.getEquipment().setChestplate(customBossesConfigFields.getChestplate());
            livingEntity.getEquipment().setLeggings(customBossesConfigFields.getLeggings());
            livingEntity.getEquipment().setBoots(customBossesConfigFields.getBoots());
            livingEntity.getEquipment().setItemInMainHand(customBossesConfigFields.getMainHand());
            livingEntity.getEquipment().setItemInOffHand(customBossesConfigFields.getOffHand());
            if (livingEntity.getEquipment().getHelmet() != null) {
                ItemMeta helmetMeta = livingEntity.getEquipment().getHelmet().getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setUnbreakable(true);
                    livingEntity.getEquipment().getHelmet().setItemMeta(helmetMeta);
                }
            }
        } catch (Exception ex) {
            Logger.warn("Tried to assign a material slot to an invalid entity! Boss is from file" + customBossesConfigFields.getFilename());
        }
    }

    public void applyBossFeatures(LivingEntity livingEntity) {
        for (ElitePower elitePower : powers)
            elitePower.applyPowers(livingEntity);
        setEquipment(livingEntity);
        setBaby(livingEntity);
        setDisguise(livingEntity);
        setName(livingEntity, customBossEntity, level);
        setFollowRange(livingEntity);
        setMovementSpeed(livingEntity);
        setFrozen(livingEntity);
        setScale(livingEntity);
        setSilent(livingEntity);
        customBossEntity.setMovementSpeedAttribute(AttributeManager.getAttributeBaseValue(livingEntity, "generic_movement_speed"));
        customBossEntity.setFollowDistance(AttributeManager.getAttributeBaseValue(livingEntity, "generic_follow_range"));

        if (livingEntity.getType().equals(EntityType.ENDER_DRAGON)) {
            ((EnderDragon) livingEntity).setPhase(EnderDragon.Phase.CIRCLING);
            if (((EnderDragon) livingEntity).getDragonBattle() != null)
                ((EnderDragon) livingEntity).getDragonBattle().generateEndPortal(false);
        }

        if (livingEntity instanceof Slime) {
            ((Slime) livingEntity).setSize(customBossEntity.getCustomBossesConfigFields().getSlimeSize());
        }
        customBossEntity.setUnsyncedLivingEntity(livingEntity);
        EntityTracker.registerEliteMob(customBossEntity, livingEntity);
    }

    private void setFollowRange(LivingEntity livingEntity) {
        if (customBossesConfigFields.getFollowDistance() != null &&
                customBossesConfigFields.getFollowDistance() > 0 &&
                livingEntity instanceof Mob)
            AttributeManager.setAttribute(livingEntity, "generic_follow_range", customBossesConfigFields.getFollowDistance());
    }

    private void setMovementSpeed(LivingEntity livingEntity) {
        if (customBossesConfigFields.getMovementSpeedAttribute() != null)
            AttributeManager.setAttribute(livingEntity, "generic_movement_speed", customBossesConfigFields.getMovementSpeedAttribute());
    }

    private void setScale(LivingEntity livingEntity) {
        if (customBossesConfigFields.getScale() != 1D)
            AttributeManager.setAttribute(livingEntity, "generic_scale", customBossesConfigFields.getScale());
    }

    private void setSilent(LivingEntity livingEntity){
        if (customBossesConfigFields.isSilent()) livingEntity.setSilent(true);
    }
}
