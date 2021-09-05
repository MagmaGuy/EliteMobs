package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.CustomConfig;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
