package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ProceduralShopMenuConfig extends MenusConfigFields {
    public static String shopName;
    public static ItemStack rerollItem;
    public static int rerollSlot;
    public static List<Integer> storeSlots;
    public static int minTier, maxTier;
    public static String messageFullInventory;
    public ProceduralShopMenuConfig() {
        super("procedural_shop_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(fileConfiguration, "Shop name", "[EM] Procedural Item Shop");
        ItemStackSerializer.serialize(
                "Reroll button",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        Arrays.asList("&8Support the plugins you enjoy!")),
                fileConfiguration);
        rerollItem = ItemStackSerializer.deserialize("Reroll button", fileConfiguration);
        rerollSlot = ConfigurationEngine.setInt(fileConfiguration, "Reroll button slot", 4);
        storeSlots = ConfigurationEngine.setList(fileConfiguration, "Store item slots", Arrays.asList(9, 10, 11, 12,
                13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
                39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53));
        minTier = ConfigurationEngine.setInt(fileConfiguration, "Minimum store item tier", 0);
        maxTier = ConfigurationEngine.setInt(fileConfiguration, "Maximum store item tier", 5);
        messageFullInventory = ConfigurationEngine.setString(fileConfiguration, "Full inventory message",
                "&7[EliteMobs] &4Your inventory is full! You can't buy items until you get some free space.");
    }

}
