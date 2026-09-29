package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstancedBossEntity extends RegionalBossEntity implements PersistentObject, PersistentMovingEntity {
    private static final ArrayListMultimap<String, InstancedBossContainer> instancedBossEntities = ArrayListMultimap.create();
    @Getter
    private  DungeonInstance dungeonInstance = null;
    private String externalDifficultyID = "0";
    private DifficultyResolver externalDifficultyResolver;
    @Getter @Setter
    private Set<Player> lockoutPlayers = new HashSet<>();

    public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, DungeonInstance dungeonInstance) {
        super(customBossesConfigFields, location, false, true);
        this.dungeonInstance = dungeonInstance;
        super.setElitePowers(ElitePowerParser.parsePowers(customBossesConfigFields, this));
        if (level == -1) {
            // For dynamic dungeons, use the player-selected level instead of calculating from gear
            if (dungeonInstance instanceof DynamicDungeonInstance dynamicDungeonInstance) {
                level = dynamicDungeonInstance.getSelectedLevel();
            } else if (dungeonInstance.getPlayers().isEmpty()) {
                Logger.warn("Failed to get players for new instance when assigning dynamic level! The bosses will default to level 1.");
            } else {
                level = ElitePlayerInventory.getPlayer(dungeonInstance.getPlayers().stream().findFirst().get()).getNaturalMobSpawnLevel(true);
            }
        }
    }

    public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, int level) {
        this(customBossesConfigFields, location, level, "0");
    }

    public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, int level,
                              String difficultyID) {
        super(customBossesConfigFields, location, false, true);
        if (!DifficultyResolver.isCanonical(difficultyID))
            throw new IllegalArgumentException("External instance difficulty must be 0, 1 or 2");
        externalDifficultyID = difficultyID;
        externalDifficultyResolver = new DifficultyResolver(customBossesConfigFields.getFilename(), List.of());
        super.level = level;
        super.setElitePowers(ElitePowerParser.parsePowers(customBossesConfigFields, this));
    }

    public static CustomBossEntity createInstancedBossEntity(String filename, Location location, int level){
        return createInstancedBossEntity(filename, location, level, "0");
    }

    public static CustomBossEntity createInstancedBossEntity(String filename, Location location, int level,
                                                             String difficultyID) {
        CustomBossesConfigFields configFields = CustomBossesConfig.getCustomBoss(filename);
        if (configFields == null){

            Logger.warn("Failed to spawn instanced boss entity " + filename + " via API!");
            return null;
        }
        return new InstancedBossEntity(configFields, location, level, difficultyID);
    }

    public String getDifficultyID() {
        return dungeonInstance == null ? externalDifficultyID : dungeonInstance.getDifficultyID();
    }

    public String getResolvedDifficultyID() {
        return dungeonInstance == null ? externalDifficultyID : dungeonInstance.getResolvedDifficultyID();
    }

    public boolean matchesDifficulty(List<String> filter, String source) {
        if (dungeonInstance != null) return dungeonInstance.matchesDifficulty(filter, source);
        // The superclass parses powers before the instance's difficulty is assigned.
        // Both constructors parse them again once the actual context exists.
        return externalDifficultyResolver == null
                || externalDifficultyResolver.matches(filter, externalDifficultyID, source);
    }

    public static void shutdown() {
        instancedBossEntities.clear();
    }

    public static void add(String stringLocation, CustomBossesConfigFields customBossesConfigFields) {
        String blueprintWorldName = stringLocation.split(",")[0];
        if (blueprintWorldName == null || blueprintWorldName.isEmpty()) {
            Logger.warn("Failed to get blueprint world location for custom boss " + customBossesConfigFields.getFilename() + " !");
            return;
        }
        instancedBossEntities.put(blueprintWorldName, new InstancedBossContainer(ConfigurationLocation.serialize(stringLocation, true), customBossesConfigFields));
    }

    public static List<InstancedBossEntity> initializeInstancedBosses(String blueprintWorldName, World newWorld, int playerCount, DungeonInstance dungeonInstance) {
        List<InstancedBossEntity> newDungeonList = new ArrayList<>();
        List<InstancedBossContainer> rawBosses = instancedBossEntities.get(blueprintWorldName);
        for (InstancedBossContainer containers : rawBosses) {
            Location newLocation = containers.getLocation().clone();
            newLocation.setWorld(newWorld);
            InstancedBossEntity newEntity = new InstancedBossEntity(containers.getCustomBossesConfigFields(), newLocation, dungeonInstance);
            newEntity.spawn(false);
            newDungeonList.add(newEntity);
        }
        return newDungeonList;
    }

    public void setNormalizedMaxHealth(int playerCount) {
        super.setNormalizedMaxHealth();
        if (dungeonInstance != null && playerCount >= 2) {
            // The base normalization already clamps health. Party scaling must respect
            // the same limit before writing either the attribute or the current health.
            maxHealth = Math.min(maxHealth * .75 * playerCount, LevelScaling.getMinecraftMaxHealth());
        }
        if (livingEntity != null) {
            AttributeManager.setAttribute(livingEntity, "generic_max_health", maxHealth);
            livingEntity.setHealth(maxHealth);
        }
        health = maxHealth;
    }

    @Override
    public void setNormalizedMaxHealth() {
        setNormalizedMaxHealth(dungeonInstance == null ? 1 : dungeonInstance.getPlayers().size());
    }

    @Override
    public void setMaxHealth() {
        Double previousHealth = health;
        setNormalizedMaxHealth();
        // Recalculating or rematerializing an existing boss must not heal it.
        if (previousHealth != null) {
            health = Math.min(previousHealth, maxHealth);
            if (livingEntity != null) livingEntity.setHealth(health);
        }
    }

    public void setEntityLevel(int level) {
        this.level = level;
        setMaxHealth();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        beginRemovalCall();
        try {
            super.remove(removalReason);
            if (removalReason.equals(RemovalReason.WORLD_UNLOAD))
                if (persistentObjectHandler != null) {
                    persistentObjectHandler.remove();
                    persistentObjectHandler = null;
                }
            if (removalReason.equals(RemovalReason.WORLD_UNLOAD) ||
                    removalReason.equals(RemovalReason.SHUTDOWN) ||
                    removalReason.equals(RemovalReason.ARENA_RESET)) {
                dungeonInstance = null;
                lockoutPlayers.clear();
            }
        } finally {
            finishRemovalCall();
        }
    }

    private static class InstancedBossContainer {
        @Getter
        private final Location location;
        @Getter
        private final CustomBossesConfigFields customBossesConfigFields;

        public InstancedBossContainer(Location location, CustomBossesConfigFields customBossesConfigFields) {
            this.location = location;
            this.customBossesConfigFields = customBossesConfigFields;
        }
    }
}
