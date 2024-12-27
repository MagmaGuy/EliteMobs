package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EliteScrollMenuConfig extends MenusConfigFields {
    @Getter
    private static String menuName;
    @Getter
    private static int infoSlot;
    @Getter
    private static ItemStack infoButton;
    @Getter
    private static int cancelSlot;
    @Getter
    private static ItemStack cancelButton;
    @Getter
    private static int confirmSlot;
    @Getter
    private static ItemStack confirmButton;
    @Getter
    private static int nonEliteItemSlot;
    @Getter
    private static int nonEliteItemInfoSlot;
    @Getter
    private static ItemStack nonEliteItemInfoButton;
    @Getter
    private static int eliteScrollItemSlot;
    @Getter
    private static int eliteScrollItemInfoSlot;
    @Getter
    private static ItemStack eliteScrollItemInfoButton;
    @Getter
    private static ItemStack outputInfoButton;
    @Getter
    private static int outputInfoSlot;
    @Getter
    private static int outputSlot;


    public EliteScrollMenuConfig() {
        super("elite_scroll_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        menuName = ConfigurationEngine.setString(
                List.of("Sets the display name of the menu"),
                file, fileConfiguration, "menuName", "&6Elite Scroll Menu", true);
        infoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the information button in the chest menu."),
                fileConfiguration, "infoSlot", 4);
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&2Elite Scroll info:",
                        new ArrayList<>(List.of(
                                "&2This menu converts non-elite items into elite items!",
                                "&2Elite items have elite dps or elite defense which applies",
                                "&2to elite mobs.",
                                "&2To convert a non-elite item into an elite item, place",
                                "&2your non-elite item below, as well as a Elite Scroll!"))),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        CustomModelAdder.addCustomModel(infoButton, CustomModelsConfig.goldenQuestionMark);
        cancelSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the cancel button in the chest menu."),
                fileConfiguration, "cancelSlot", 27);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel!"), MetadataHandler.signatureID), fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        CustomModelAdder.addCustomModel(cancelButton, CustomModelsConfig.redCross);
        confirmSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the confirm button in the chest menu."),
                fileConfiguration, "confirmSlot", 35);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Apply!", new ArrayList<>(),
                "elitemobs:ui/anvilhammer"), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
        nonEliteItemSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the non-elite item in the chest menu."),
                fileConfiguration, "itemSlot", 29);
        nonEliteItemInfoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the non-elite item info button in the chest menu."),
                fileConfiguration, "itemInfoSlot", 20);
        ItemStackSerializer.serialize(
                "itemInfoButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Non-Elite Item Input slot",
                        List.of("&aPut your non-elite item here!"), MetadataHandler.signatureID),
                fileConfiguration);
        nonEliteItemInfoButton = ItemStackSerializer.deserialize("itemInfoButton", fileConfiguration);
        CustomModelAdder.addCustomModel(nonEliteItemInfoButton, CustomModelsConfig.boxInput);
        eliteScrollItemInfoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the elite scroll item info button in the chest menu."),
                fileConfiguration, "eliteScrollItemInfoSlot", 22);
        ItemStackSerializer.serialize(
                "eliteScrollItemInfoButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Scroll Input slot",
                        List.of("&aPut your elite scroll here!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteScrollItemInfoButton = ItemStackSerializer.deserialize("eliteScrollItemInfoButton", fileConfiguration);
        eliteScrollItemSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of elite scroll in the chest menu."),
                fileConfiguration, "eliteScrollItemSlot", 31);

        outputInfoButton = ConfigurationEngine.setItemStack(file, fileConfiguration, "outputInfoButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER, "&2Result"), true);
        outputInfoSlot = ConfigurationEngine.setInt(fileConfiguration, "outputInfoSlot", 24);
        outputSlot = ConfigurationEngine.setInt(fileConfiguration, "outputSlot", 33);
        CustomModelAdder.addCustomModel(eliteScrollItemInfoButton, CustomModelsConfig.boxInput);
    }
}
