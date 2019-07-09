package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BuyOrSellMenuConfig extends MenusConfigFields {
    public BuyOrSellMenuConfig() {
        super("buy_or_sell_menu.yml");
    }

    public static String SHOP_NAME;
    public static ItemStack INFORMATION_ITEM;
    public static int INFORMATION_SLOT;
    public static ItemStack BUY_PROCEDURAL_ITEM;
    public static ItemStack BUY_CUSTOM_ITEM;
    public static int BUY_SLOT;
    public static ItemStack SELL_ITEM;
    public static int SELL_SLOT;

    @Override
    public FileConfiguration generateConfigDefaults(FileConfiguration fileConfiguration) {
        SHOP_NAME = ConfigurationEngine.setString(fileConfiguration, "Shop name", "[EM] Custom Item Shop");
        ItemStackSerializer.serialize(
                "Information button",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        Arrays.asList("&8Support the plugins you enjoy!",
                                "&aClick on the emerald to buy items!",
                                "&cClick on the redstone to sell items!")),
                fileConfiguration);
        INFORMATION_ITEM = ItemStackSerializer.deserialize("Information button", fileConfiguration);
        INFORMATION_SLOT = ConfigurationEngine.setInt(fileConfiguration, "Information button slot", 4);
        ItemStackSerializer.serialize("Buy procedurally generated items",
                ItemStackGenerator.generateItemStack(Material.EMERALD, "Buy items"), fileConfiguration);
        BUY_PROCEDURAL_ITEM = ItemStackSerializer.deserialize("Buy procedurally generated items", fileConfiguration);
        ItemStackSerializer.serialize("Buy custom items",
                ItemStackGenerator.generateItemStack(Material.EMERALD, "Buy custom items"), fileConfiguration);
        BUY_CUSTOM_ITEM = ItemStackSerializer.deserialize("Buy custom items", fileConfiguration);
        ItemStackSerializer.serialize("Sell items",
                ItemStackGenerator.generateItemStack(Material.REDSTONE, "Sell items"), fileConfiguration);
        SELL_ITEM = ItemStackSerializer.deserialize("Sell items", fileConfiguration);
        BUY_SLOT = ConfigurationEngine.setInt(fileConfiguration, "&a&lBuy slot", 11);
        SELL_SLOT = ConfigurationEngine.setInt(fileConfiguration, "&c&lSell slot", 15);

        return fileConfiguration;
    }

}
