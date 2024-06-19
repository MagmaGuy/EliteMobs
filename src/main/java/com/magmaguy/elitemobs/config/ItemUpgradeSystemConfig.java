package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ItemUpgradeSystemConfig {

    @Getter
    private static double luckyTicketChance;
    @Getter
    private static double enchantedBookChance;
    @Getter
    private static double scrapChance;

    private ItemUpgradeSystemConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ItemUpgradeSystem.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        //luckyTicketChance = ConfigurationEngine.setDouble("luckyTicketChance", )
        //todo: odds for new special items go here
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
