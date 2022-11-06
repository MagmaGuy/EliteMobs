package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;

public class InstancedBossEntity extends RegionalBossEntity implements PersistentObject, PersistentMovingEntity {
    private static final ArrayListMultimap<String, InstancedBossContainer> instancedBossEntities = ArrayListMultimap.create();
    @Getter
    private final DungeonInstance dungeonInstance;

    public InstancedBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, DungeonInstance dungeonInstance) {
        super(customBossesConfigFields, location, false, true);
        this.dungeonInstance = dungeonInstance;
    }

    public static void shutdown() {
        instancedBossEntities.clear();
    }

    public static void add(String stringLocation, CustomBossesConfigFields customBossesConfigFields) {
        String blueprintWorldName = stringLocation.split(",")[0];
        if (blueprintWorldName == null || blueprintWorldName.isEmpty()) {
            new WarningMessage("Failed to get blueprint world location for custom boss " + customBossesConfigFields.getFilename() + " !");
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
            //Set the health multipliers
            //newEntity.setHealthMultiplier(calculateHealthMultiplier(newEntity.healthMultiplier, playerCount));
            newEntity.spawn(false);
            newDungeonList.add(newEntity);
        }
        return newDungeonList;
    }

    @Override
    public void setNormalizedMaxHealth() {
        super.setNormalizedMaxHealth();
        double normalizedDungeonMaxHealth = super.getMaxHealth() * Math.pow(dungeonInstance.getPlayers().size(), 1.2);
        super.maxHealth = normalizedDungeonMaxHealth;
        super.health = maxHealth;
        if (livingEntity != null){
            livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            livingEntity.setHealth(maxHealth);
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

    @Override
    public void remove(RemovalReason removalReason) {
        super.remove(removalReason);
        if (removalReason.equals(RemovalReason.WORLD_UNLOAD))
            if (persistentObjectHandler != null) {
                persistentObjectHandler.remove();
                persistentObjectHandler = null;
            }
    }
}
