package com.magmaguy.elitemobs.config.menus;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.premade.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MenusConfig {

    private static final ArrayList<MenusConfigFields> menusConfigFieldsList = new ArrayList<>(Arrays.asList(
            new ProceduralShopMenuConfig(),
            new CustomShopMenuConfig(),
            new BuyOrSellMenuConfig(),
            new SellMenuConfig(),
            new GetLootMenuConfig(),
            new GuildRankMenuConfig(),
            new QuestMenuConfig(),
            new PlayerStatusMenuConfig()
    ));

    public static void initializeConfigs() {
        //Checks if the directory doesn't exist
        for (MenusConfigFields menusConfigFields : menusConfigFieldsList)
            initializeConfiguration(menusConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param menusConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(MenusConfigFields menusConfigFields) {

        File file = ConfigurationEngine.fileCreator("menus", menusConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        menusConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

        return fileConfiguration;

    }

}
