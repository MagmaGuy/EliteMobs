package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BuyOrSellMenuConfig extends MenusConfigFields {
    public static String SHOP_NAME;
    //    public static ItemStack INFORMATION_ITEM;
//    public static int INFORMATION_SLOT;
    public static ItemStack BUY_PROCEDURAL_ITEM;
    public static ItemStack BUY_CUSTOM_ITEM;
    public static int BUY_SLOT;
    public static ItemStack SELL_ITEM;
    public static int SELL_SLOT;

    public BuyOrSellMenuConfig() {
        super("buy_or_sell_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        SHOP_NAME = ConfigurationEngine.setString(file, fileConfiguration, "Shop name", "[EM] Buy or Sell", true);
//        ItemStackSerializer.serialize(
//                "Information button",
//                ItemStackGenerator.generateSkullItemStack("magmaguy",
//                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
//                        Arrays.asList("&8Support the plugins you enjoy!",
//                                "&aClick on the emerald to buy items!",
//                                "&cClick on the redstone to sell items!"), MetadataHandler.signatureID),
//                fileConfiguration);
//        INFORMATION_ITEM = ItemStackSerializer.deserialize("Information button", fileConfiguration);
//        INFORMATION_SLOT = ConfigurationEngine.setInt(fileConfiguration, "Information button slot", 4);
        ItemStackSerializer.serialize("Buy procedurally generated items",
                ItemStackGenerator.generateItemStack(Material.EMERALD, "Buy items", new ArrayList<>(), MetadataHandler.signatureID), fileConfiguration);
        BUY_PROCEDURAL_ITEM = ItemStackSerializer.deserialize("Buy procedurally generated items", fileConfiguration);
        ItemStackSerializer.serialize("Buy custom items",
                ItemStackGenerator.generateItemStack(Material.EMERALD, "Buy custom items", new ArrayList<>(), MetadataHandler.signatureID), fileConfiguration);
        BUY_CUSTOM_ITEM = ItemStackSerializer.deserialize("Buy custom items", fileConfiguration);
        ItemStackSerializer.serialize("Sell items",
                ItemStackGenerator.generateItemStack(Material.REDSTONE, "Sell items", new ArrayList<>(), MetadataHandler.signatureID), fileConfiguration);
        SELL_ITEM = ItemStackSerializer.deserialize("Sell items", fileConfiguration);
        BUY_SLOT = ConfigurationEngine.setInt(fileConfiguration, "Buy slot", 11);
        SELL_SLOT = ConfigurationEngine.setInt(fileConfiguration, "Sell slot", 15);
    }

}
