package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class RegionalBossHandler implements Listener {

    private static final HashMap<CustomBossConfigFields, CustomBossEntity> livingBossEntities = new HashMap<>();

    public static void initialize() {

        for (CustomBossConfigFields customBossConfigFields : CustomBossConfigFields.getRegionalElites())
            spawnRegionalBoss(customBossConfigFields);

    }

    private static void spawnRegionalBoss(CustomBossConfigFields customBossConfigFields) {

        if (!customBossConfigFields.isEnabled()) return;
        if (customBossConfigFields.getConfigRegionalEntities().size() < 1)
            return;

        for (CustomBossConfigFields.ConfigRegionalEntity configRegionalEntities : customBossConfigFields.getConfigRegionalEntities().values()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    new RegionalBossEntity(customBossConfigFields, configRegionalEntities);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, customBossConfigFields.getTicksBeforeRespawn(configRegionalEntities.uuid));
        }

    }

}
