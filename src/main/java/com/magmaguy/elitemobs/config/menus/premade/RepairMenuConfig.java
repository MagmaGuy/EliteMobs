package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RepairMenuConfig extends MenusConfigFields {

    public static String shopName;
    public static ItemStack infoButton;
    public static ItemStack eliteItemInputInfoButton;
    public static ItemStack eliteScrapInputInfoButton;
    public static ItemStack outputInfoButton;
    public static int infoSlot;
    public static int eliteItemInputInformationSlot;
    public static int eliteScrapInputInformationSlot;
    public static int outputInformationSlot;
    public static int eliteItemInputSlot;
    public static int eliteScrapInputSlot;
    public static int outputSlot;
    public static ItemStack cancelButton;
    public static int cancelSlot;
    public static ItemStack confirmButton;
    public static int confirmSlot;

    public RepairMenuConfig() {
        super("repair_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "shopName", "[EM] Repair menu!", true);
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        new ArrayList<>(List.of("&8Support the plugins you enjoy!",
                                "&aUse scrap to repair elite items!"))),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        CustomModelAdder.addCustomModel(infoButton, CustomModelsConfig.goldenQuestionMark);
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoButtonSlot", 4);
        eliteItemInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputInformationSlot", 20);
        eliteItemInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputSlot", 29);
        ItemStackSerializer.serialize(
                "eliteItemInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2 Elite Item Input slots",
                        List.of("&aThis slot is for your elite item!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", fileConfiguration);
        CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxInput);
        eliteScrapInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteScrapInputInformationSlot", 22);
        eliteScrapInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteScrapInputSlot", 31);
        ItemStackSerializer.serialize(
                "eliteScrapInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Scrap Input slots",
                        List.of("&aThis slot is for your elite scrap!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteScrapInputInfoButton = ItemStackSerializer.deserialize("eliteScrapInputInformationButton", fileConfiguration);
        CustomModelAdder.addCustomModel(eliteScrapInputInfoButton, CustomModelsConfig.boxInput);
        outputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationOutputButtonSlot", 24);
        outputSlot = ConfigurationEngine.setInt(fileConfiguration, "outputSlot", 33);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel repair!"), MetadataHandler.signatureID), fileConfiguration);
        ItemStackSerializer.serialize(
                "outputInformationButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER,
                        "&2Output slots",
                        List.of("&aThis slot previews the result of your repair!"), MetadataHandler.signatureID),
                fileConfiguration);
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration);
        CustomModelAdder.addCustomModel(outputInfoButton, CustomModelsConfig.boxOutput);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm!", List.of("&aRepair item!")), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmRepairSlot", 35);
    }

}
