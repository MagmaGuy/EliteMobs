package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.premade.TestCustomTreasureChestConfig;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomTreasureChestsConfig extends CustomConfig {

    @Getter
    private static  HashMap<String, CustomTreasureChestConfigFields> customTreasureChestConfigFields = new HashMap<>();

    public CustomTreasureChestsConfig(){
        super("customtreasurechests", "com.magmaguy.elitemobs.config.customtreasurechests.premade", CustomTreasureChestConfigFields.class);
        customTreasureChestConfigFields = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled())
                customTreasureChestConfigFields.put(key, (CustomTreasureChestConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static void addTreasureChestEntry(Player player, String customChestFileName){
        CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
        if (customTreasureChestConfigFields == null){
            player.sendMessage("[EM] Invalid Treasure Chest file name!");
             return;
        }
        Location permanentLocation = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        customTreasureChestConfigFields.updateLocations(permanentLocation, 0);
    }

}
