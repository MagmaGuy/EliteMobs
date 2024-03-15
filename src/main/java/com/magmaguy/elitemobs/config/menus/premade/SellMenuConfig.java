package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SellMenuConfig extends MenusConfigFields {
    public static String shopName;
    public static List<Integer> storeSlots;
    public static ItemStack cancelButton;
    public static List<Integer> cancelSlots;
    public static ItemStack confirmButton;
    public static List<Integer> confirmSlots;

    public SellMenuConfig() {
        super("sell_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "menuTitle", ":offset_-8::elitemob_sell:", true);

        storeSlots = ConfigurationEngine.setList(file, fileConfiguration, "sellSlots", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23,
                24, 25), false);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel purchase!"), MetadataHandler.signatureID), fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlots = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlots", Arrays.asList(38, 39, 47, 48));
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm Sale", Arrays.asList("&aSell item for", "&a$currency_amount $currency_name"), MetadataHandler.signatureID), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        confirmSlots = ConfigurationEngine.setInt(fileConfiguration, "confirmSaleSlots", Arrays.asList(41, 42, 50, 51));
    }

}
