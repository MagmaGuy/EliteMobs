package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ItemUpgradeSystemConfig {

    @Getter
    private static String scrapItemName;
    @Getter
    private static List<String> scrapItemLore;
    @Getter
    private static String upgradeItemName;
    @Getter
    private static List<String> upgradeItemLore;
    @Getter
    private static Material material;

    private ItemUpgradeSystemConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ScrapItemSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        scrapItemName = ConfigurationEngine.setString(file, fileConfiguration, "scrapItemName", "&2Level $level Scrap", true);
        scrapItemLore = ConfigurationEngine.setList(file, fileConfiguration, "scrapItemLore", Arrays.asList(
                "&aGather 25 scrap of the same level in",
                "&aorder to create an item upgrade orb!",
                "&aYou can also use scrap to repair",
                "&aitems and craft higher level scrap!",
                "&2Do this at the Adventurer's Guild Hub!"), true);
        upgradeItemName = ConfigurationEngine.setString(file, fileConfiguration, "upgradeItemName", "&2Level $level Upgrade Orb", true);
        upgradeItemLore = ConfigurationEngine.setList(file, fileConfiguration, "upgradeItemLore", Arrays.asList(
                "&aUse at at the Enhancer NPC to upgrade",
                "&aitems up to level $orbLevel!"), true);
        String materialString = ConfigurationEngine.setString(file, fileConfiguration, "material", Material.HEART_OF_THE_SEA.toString(), false);
        try {
            material = Material.valueOf(materialString);
        } catch (Exception ex) {
            new WarningMessage("Failed to set upgrade item material " + materialString + " as a valid material! This will default to " + Material.HEART_OF_THE_SEA);
            material = Material.HEART_OF_THE_SEA;
        }
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
