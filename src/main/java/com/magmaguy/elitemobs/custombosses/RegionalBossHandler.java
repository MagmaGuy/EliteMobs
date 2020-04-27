package com.magmaguy.elitemobs.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class RegionalBossHandler implements Listener {

    private static HashMap<CustomBossConfigFields, CustomBossEntity> livingBossEntities = new HashMap<>();

    public static void initialize() {

        for (CustomBossConfigFields customBossConfigFields : CustomBossConfigFields.getRegionalElites())
            spawnRegionalBoss(customBossConfigFields);

    }

    private static void spawnRegionalBoss(CustomBossConfigFields customBossConfigFields) {

        if (!customBossConfigFields.isEnabled()) return;
        if (customBossConfigFields.getSpawnLocation() == null && customBossConfigFields.getSpawnLocations().size() < 1)
            return;

        if (customBossConfigFields.getSpawnLocations().size() > 0) {
            for (Location newSpawnLocation : customBossConfigFields.getSpawnLocations())
                new RegionalBossEntity(customBossConfigFields, newSpawnLocation);
            return;
        }

        new RegionalBossEntity(customBossConfigFields, customBossConfigFields.getSpawnLocation());

    }

}
