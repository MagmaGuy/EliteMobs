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
    public static ItemStack repairCostInfoButton;
    public static ItemStack repairCostButton;
    public static ItemStack outputInfoButton;
    public static int infoSlot;
    public static int eliteItemInputInformationSlot;
    public static int repairCostInformationSlot;
    public static int outputInformationSlot;
    public static int eliteItemInputSlot;
    public static int repairCostSlot;
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
                "infoButtonV2",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        new ArrayList<>(List.of("&8Support the plugins you enjoy!",
                                "&aPay elite coins to repair elite items!",
                                "&aThe more damaged and the higher level",
                                "&athe item, the more the repair costs."))),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButtonV2", fileConfiguration, file);
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
        eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", fileConfiguration, file);
        CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxInput);
        repairCostInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "repairCostInformationSlot", 22);
        repairCostSlot = ConfigurationEngine.setInt(fileConfiguration, "repairCostSlot", 31);
        ItemStackSerializer.serialize(
                "repairCostInformationButton",
                ItemStackGenerator.generateItemStack(Material.GOLD_INGOT,
                        "&2Repair cost",
                        List.of("&aThe slot below shows what the repair costs!"), MetadataHandler.signatureID),
                fileConfiguration);
        repairCostInfoButton = ItemStackSerializer.deserialize("repairCostInformationButton", fileConfiguration, file);
        CustomModelAdder.addCustomModel(repairCostInfoButton, CustomModelsConfig.bagOfCoins);
        // $price, $currencyName and $currentAmount are filled in for each item placed in the menu.
        ItemStackSerializer.serialize(
                "repairCostButton",
                ItemStackGenerator.generateItemStack(Material.GOLD_NUGGET,
                        "&6Repair cost: $price $currencyName",
                        List.of("&7You have $currentAmount $currencyName"), MetadataHandler.signatureID),
                fileConfiguration);
        repairCostButton = ItemStackSerializer.deserialize("repairCostButton", fileConfiguration, file);
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
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration, file);
        CustomModelAdder.addCustomModel(outputInfoButton, CustomModelsConfig.boxOutput);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration, file);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm!", List.of("&aRepair item!")), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration, file);
        CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmRepairSlot", 35);
    }

}
