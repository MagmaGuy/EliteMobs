package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    /**
     * Attempts to spawn a {@link CustomBossEntity} whose spawn location has already been set.
     *
     * @return Whether the spawn succeeded or not.
     */
    public LivingEntity spawn() {
        if (spawnLocation == null) {
            new WarningMessage("Custom Boss Entity " + customBossesConfigFields.getFilename() + " tried to spawn without a valid spawn location getting assigned! Report this to the developer!");
            return null;
        }
        if (EliteMobs.worldGuardIsEnabled) {
            if (!WorldGuardFlagChecker.checkFlag(spawnLocation, WorldGuardCompatibility.getEliteMobsSpawnFlag())) {
                new WarningMessage("Attempted to spawn " + customBossesConfigFields.getFilename() + " in location " +
                        spawnLocation.toString() + " which is protected by WorldGuard with elitemobs-spawning deny! This should not have happened.");
                return null;
            }
            if (bypassesWorldGuardSpawn || customBossEntity instanceof RegionalBossEntity)
                WorldGuardSpawnEventBypasser.forceSpawn();
        }

        LivingEntity livingEntity = (LivingEntity) spawnLocation.getWorld().spawn(spawnLocation,
                customBossesConfigFields.getEntityType().getEntityClass(),
                entity -> applyBossFeatures((LivingEntity) entity));
        customBossEntity.setLivingEntity(livingEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return livingEntity;
    }

    public void applyBossFeatures(LivingEntity livingEntity) {
        for (ElitePower elitePower : powers)
            elitePower.applyPowers(livingEntity);
        setEquipment(livingEntity);
        setBaby(livingEntity);
        setFrozen(livingEntity);
        setDisguise(livingEntity);
        setName(livingEntity);
        setFollowRange(livingEntity);

        if (livingEntity.getType().equals(EntityType.ENDER_DRAGON)) {
            ((EnderDragon) livingEntity).setPhase(EnderDragon.Phase.CIRCLING);
            ((EnderDragon) livingEntity).getDragonBattle().generateEndPortal(false);
        }
    }

    private void setBaby(LivingEntity livingEntity) {
        if (livingEntity instanceof Ageable)
            if (customBossesConfigFields.isBaby())
                ((Ageable) livingEntity).setBaby();
            else
                ((Ageable) livingEntity).setAdult();
    }


    private void setDisguise(LivingEntity livingEntity) {
        if (customBossesConfigFields.getDisguise() == null) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        try {
            DisguiseEntity.disguise(customBossesConfigFields.getDisguise(), livingEntity, customBossesConfigFields.getCustomDisguiseData(), customBossesConfigFields.getFilename());
        } catch (Exception ex) {
            new WarningMessage("Failed to load LibsDisguises disguise correctly!");
        }
    }

    private void setFrozen(LivingEntity livingEntity) {
        if (!customBossesConfigFields.isFrozen()) return;
        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10));
        livingEntity.setCollidable(false);
        livingEntity.setGravity(false);
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
            new WarningMessage("Tried to assign a material slot to an invalid entity! Boss is from file" + customBossesConfigFields.getFilename());
        }
    }

    private void setName(LivingEntity livingEntity) {
        String parsedName = ChatColorConverter.convert(customBossesConfigFields.getName().replace("$level", this.level + "")
                .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + this.level + "&2]&f"))
                .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + this.level + "&6〗&f"))
                .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + this.level + "&4』&f"))
                .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + this.level + "&8〕&f")
                .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + this.level + "&4」&f")));
        livingEntity.setCustomName(parsedName);
        livingEntity.setCustomNameVisible(DefaultConfig.alwaysShowNametags);
        DisguiseEntity.setDisguiseNameVisibility(DefaultConfig.alwaysShowNametags, livingEntity);
        customBossEntity.setName(parsedName, false);
    }

    private void setFollowRange(LivingEntity livingEntity) {
        if (customBossesConfigFields.getFollowDistance() != null &&
                customBossesConfigFields.getFollowDistance() > 0 &&
                livingEntity instanceof Mob)
            livingEntity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(customBossesConfigFields.getFollowDistance());
    }

}
