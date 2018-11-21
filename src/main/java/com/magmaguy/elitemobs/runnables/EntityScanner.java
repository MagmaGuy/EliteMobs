package com.magmaguy.elitemobs.runnables;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobscanner.EliteMobScanner;
import com.magmaguy.elitemobs.mobscanner.SuperMobScanner;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EntityScanner extends BukkitRunnable {

    @Override
    public void run() {
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.AGGRESSIVE_MOB_STACKING)) {
            BukkitTask scanElites = new BukkitRunnable() {
                @Override
                public void run() {
                    EliteMobScanner.scanElites();
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 5, 20 * 5);
        }


        if (ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS)) {
            BukkitTask scanSuperMobs = new BukkitRunnable() {
                @Override
                public void run() {
                    SuperMobScanner.scanSuperMobs();
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 12, 20 * 12);
        }


    }

}
