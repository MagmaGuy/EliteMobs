package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.MobLevelPlaceholderFormatter;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBossMegaConsumer {
    /**
     * Boss filenames already reported by {@link #warnIfNameCanNeverRender()}, so the
     * warning is emitted once per boss rather than once per spawn.
     */
    private static final Set<String> NAMETAG_BONE_WARNINGS = ConcurrentHashMap.newKeySet();
    private final CustomBossesConfigFields customBossesConfigFields;
    private final HashSet<ElitePower> powers;
    private final int level;
    private final boolean bypassesWorldGuardSpawn;
    private final Location spawnLocation;
    private boolean disguiseQueued = false;
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
        String parsedName = parseName(customBossEntity, level);
        livingEntity.setCustomName(parsedName);
        boolean showName = DefaultConfig.isAlwaysShowNametags() || customBossEntity.customBossesConfigFields.isAlwaysShowName();
        livingEntity.setCustomNameVisible(showName);
        if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises"))
            DisguiseEntity.setDisguiseNameVisibility(showName, livingEntity, parsedName);
        customBossEntity.setName(parsedName, false);
    }

    protected static String parseName(CustomBossEntity customBossEntity, int level) {
        return ChatColorConverter.convert(MobLevelPlaceholderFormatter.replaceLevelPlaceholders(
                customBossEntity.customBossesConfigFields.getName(), customBossEntity, level));
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

        disguiseQueued = queueDisguise(parseName(customBossEntity, level));
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
        if (disguiseQueued) return;
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

    private boolean queueDisguise(String displayName) {
        if (customBossesConfigFields.getDisguise() == null ||
                CustomModel.customModelsEnabled() &&
                        customBossesConfigFields.isCustomModelExists() &&
                        customBossesConfigFields.getCustomModel() != null &&
                        !customBossesConfigFields.getCustomModel().isEmpty())
            return false;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return false;
        boolean showName = DefaultConfig.isAlwaysShowNametags() || customBossesConfigFields.isAlwaysShowName();
        return DisguiseEntity.disguiseNext(
                customBossesConfigFields.getDisguise(),
                displayName,
                showName,
                customBossesConfigFields.getCustomDisguiseData(),
                customBossesConfigFields.getFilename());
    }

    private void setCustomModel(LivingEntity livingEntity) {
        if (!customBossesConfigFields.isCustomModelExists()) return;
        if (customBossesConfigFields.getCustomModel() == null || customBossesConfigFields.getCustomModel().isEmpty())
            return;
        try {
            customBossEntity.setCustomModel(CustomModel.generateCustomModel(livingEntity, customBossesConfigFields.getCustomModel(), customBossEntity.getName()));
            warnIfNameCanNeverRender();
        } catch (Exception exception) {
            customBossEntity.setCustomModel(null);
            Logger.warn("Failed to initialize Custom Model for Custom Boss " + customBossesConfigFields.getFilename());
            exception.printStackTrace();
        }
    }

    /**
     * A modeled boss draws its name on a nametag anchor bone belonging to the model
     * (for FreeMinecraftModels, a bone named {@code tag_*}). The underlying living entity
     * is hidden by the model plugin, so its vanilla nametag never renders as a fallback.
     * A model without such a bone is therefore permanently nameless, no matter what
     * {@code name} and {@code alwaysShowName} say - and it fails completely silently,
     * which makes it very hard to diagnose from a screenshot.
     * <p>
     * Warned once per boss file so a repeatedly spawning boss cannot spam the console.
     */
    private void warnIfNameCanNeverRender() {
        CustomModel customModel = customBossEntity.getCustomModel();
        if (customModel == null) return;
        if (!nameCanNeverRender(
                DefaultConfig.isAlwaysShowNametags(),
                customBossesConfigFields.isAlwaysShowName(),
                customBossesConfigFields.getName(),
                customModel.hasNametagBone()))
            return;
        if (!NAMETAG_BONE_WARNINGS.add(customBossesConfigFields.getFilename())) return;
        Logger.warn("Custom Boss " + customBossesConfigFields.getFilename() + " asks for a visible name but its model \"" +
                customBossesConfigFields.getCustomModel() + "\" has no nametag anchor bone, so the name can never be shown. " +
                "Add a bone whose name starts with \"tag_\" (for example \"tag_head\") to that model.");
    }

    /**
     * Pure decision half of {@link #warnIfNameCanNeverRender()}, kept independent of entity
     * spawning so the rule can be verified without constructing a modeled boss.
     *
     * @param alwaysShowNametagsGlobally the server-wide "always show nametags" setting
     * @param alwaysShowNameForBoss      this boss's {@code alwaysShowName} setting
     * @param configuredName             this boss's configured {@code name}
     * @param modelHasNametagBone        whether the model can render a nametag at all
     * @return true when the boss is asking for a name that its model can never draw
     */
    static boolean nameCanNeverRender(boolean alwaysShowNametagsGlobally,
                                      boolean alwaysShowNameForBoss,
                                      String configuredName,
                                      boolean modelHasNametagBone) {
        if (modelHasNametagBone) return false;
        if (!alwaysShowNametagsGlobally && !alwaysShowNameForBoss) return false;
        return configuredName != null && !configuredName.isEmpty();
    }

    private void setFrozen(LivingEntity livingEntity) {
        if (!customBossesConfigFields.isFrozen()) return;
        AttributeManager.setAttribute(livingEntity, "generic_movement_speed", 0);
        // Note: Do NOT set collidable to false - it prevents projectiles from hitting the entity
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
        setAI(livingEntity);
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

    private void setAI(LivingEntity livingEntity) {
        if (!customBossesConfigFields.isAi() && livingEntity instanceof Mob mob)
            mob.setAI(false);
    }
}
