package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ItemUpgradeSystemConfig {

    public static String scrapItemName;
    public static List<String> scrapItemLore;
    public static String upgradeItemName;
    public static List<String> upgradeItemLore;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ScrapItemSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        scrapItemName = ConfigurationEngine.setString(fileConfiguration, "scrapItemName", "&2Level $level Scrap");
        scrapItemLore = ConfigurationEngine.setList(fileConfiguration, "scrapItemLore", Arrays.asList(
                "&aGather 25 scrap of the same level in",
                "&aorder to create an item upgrade orb!",
                "&aYou can also use scrap to repair",
                "&aitems and craft higher level scrap!",
                "&2Do this at the Adventurer's Guild Hub!"));
        upgradeItemName = ConfigurationEngine.setString(fileConfiguration, "upgradeItemName", "&2Level $level Upgrade Orb");
        upgradeItemLore = ConfigurationEngine.setList(fileConfiguration, "upgradeItemLore", Arrays.asList(
                "&aUse at at the Enhancer NPC to upgrade",
                "&aitems from level $itemLevel to level $orbLevel!"));
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
