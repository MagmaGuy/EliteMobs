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

public class RepairMenuConfig extends MenusConfigFields {

    public static String shopName;
    //    public static ItemStack infoButton;
    public static ItemStack eliteItemInputInfoButton;
    public static ItemStack eliteScrapInputInfoButton;
    public static ItemStack outputInfoButton;
    //    public static int infoSlot;
    public static int eliteItemInputInformationSlot;
    public static int eliteScrapInputInformationSlot;
    public static int outputInformationSlot;
    public static int eliteItemInputSlot;
    public static int eliteScrapInputSlot;
    public static int outputSlot;
    public static ItemStack cancelButton;
    public static List<Integer> cancelSlots;
    public static ItemStack confirmButton;
    public static List<Integer> confirmSlots;

    public RepairMenuConfig() {
        super("repair_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "menuTitle", ":offset_-8::elitemob_repair:", true);

        eliteItemInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputInformationSlot", 2);
        eliteItemInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputSlot", 11);
        ItemStackSerializer.serialize(
                "eliteItemInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2 Elite Item Input slots",
                        List.of("&aThis slot is for your elite item!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", fileConfiguration);
        eliteScrapInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteScrapInputInformationSlot", 4);
        eliteScrapInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteScrapInputSlot", 13);
        ItemStackSerializer.serialize(
                "eliteScrapInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Scrap Input slots",
                        List.of("&aThis slot is for your elite scrap!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteScrapInputInfoButton = ItemStackSerializer.deserialize("eliteScrapInputInformationButton", fileConfiguration);
        outputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationOutputButtonSlot", 6);
        outputSlot = ConfigurationEngine.setInt(fileConfiguration, "outputSlot", 12);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel repair!"), MetadataHandler.signatureID), fileConfiguration);
        ItemStackSerializer.serialize(
                "outputInformationButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER,
                        "&2Output slots",
                        List.of("&aThis slot previews the result of your repair!"), MetadataHandler.signatureID),
                fileConfiguration);
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlots = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlots", Arrays.asList(29, 30, 38, 39));
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm!", List.of("&aRepair item!"), 31174), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        confirmSlots = ConfigurationEngine.setInt(fileConfiguration, "confirmRepairSlots", Arrays.asList(32, 33, 41, 42));
    }

}
