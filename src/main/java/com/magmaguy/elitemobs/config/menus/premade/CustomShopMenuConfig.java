package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;


public class CustomShopMenuConfig extends MenusConfigFields {
    public CustomShopMenuConfig() {
        super("custom_shop_menu.yml");
    }

    public static String SHOP_NAME;
    public static ItemStack REROLL_ITEM;
    public static int REROLL_SLOT;
    public static List<Integer> STORE_SLOTS;
    public static String MESSAGE_FULL_INVENTORY;

    @Override
    public FileConfiguration generateConfigDefaults(FileConfiguration fileConfiguration) {
        SHOP_NAME = ConfigurationEngine.setString(fileConfiguration, "Shop name", "[EM] Buy or Sell");
        ItemStackSerializer.serialize("Reroll button",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        Arrays.asList("&8Support the plugins you enjoy!")),
                fileConfiguration);
        REROLL_ITEM = ItemStackSerializer.deserialize("Reroll button", fileConfiguration);
        REROLL_SLOT = ConfigurationEngine.setInt(fileConfiguration, "Reroll button slot", 4);
        STORE_SLOTS = ConfigurationEngine.setList(fileConfiguration, "Store item slots", Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
                43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53));
        MESSAGE_FULL_INVENTORY = ConfigurationEngine.setString(fileConfiguration, "Full inventory message",
                "[EliteMobs] &4Your inventory is full! You can't buy items until you get some free space.");
        return fileConfiguration;
    }

}
