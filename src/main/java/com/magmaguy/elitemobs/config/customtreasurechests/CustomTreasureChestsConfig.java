package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class CustomTreasureChestsConfig extends CustomConfig {

    @Getter
    private static HashMap<String, CustomTreasureChestConfigFields> customTreasureChestConfigFields = new HashMap<>();

    public CustomTreasureChestsConfig() {
        super("customtreasurechests", "com.magmaguy.elitemobs.config.customtreasurechests.premade", CustomTreasureChestConfigFields.class);
        customTreasureChestConfigFields = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled())
                customTreasureChestConfigFields.put(key, (CustomTreasureChestConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static void addTreasureChestEntry(Player player, String customChestFileName) {
        CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
        if (customTreasureChestConfigFields == null) {
            player.sendMessage(CommandMessagesConfig.getInvalidTreasureChestFilename());
            return;
        }
        addTreasureChestEntry(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()), customChestFileName);
    }

    public static TreasureChest addTreasureChestEntry(Location location, String customChestFileName) {
        CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
        if (location == null) {
            Logger.warn("Failed to commit a location for a treasure chest!");
            return null;
        }
        return customTreasureChestConfigFields.addTreasureChest(location, 0);
    }

    public static void removeTreasureChestEntry(Location location, String customChestFileName) {
        CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
        if (location == null || customTreasureChestConfigFields == null) {
            Logger.warn("Failed to remove a location for a treasure chest!");
            return;
        }
        customTreasureChestConfigFields.purgeLocations();
    }

}
