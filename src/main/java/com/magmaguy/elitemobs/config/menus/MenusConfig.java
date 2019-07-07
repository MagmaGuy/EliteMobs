package com.magmaguy.elitemobs.config.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.UnusedNodeHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MenusConfig {

    private static ArrayList<MenusConfigFields> menusConfigFieldsList = new ArrayList<>(Arrays.asList(
            new ProceduralShopMenuConfig(),
            new CustomShopMenuConfig(),
            new BuyOrSellMenuConfig()
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

        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/menus", menusConfigFields.getFileName());

        if (!file.exists())
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                Bukkit.getLogger().warning("[EliteMobs] Error generating the plugin file: " + file.getName());
            }

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        menusConfigFields.generateConfigDefaults(fileConfiguration);
        fileConfiguration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(fileConfiguration);

        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileConfiguration;

    }

}
