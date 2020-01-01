package com.magmaguy.elitemobs.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
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
        if (customBossConfigFields.getSpawnLocation() == null) return;

        new RegionalBossEntity(customBossConfigFields);

    }

}
