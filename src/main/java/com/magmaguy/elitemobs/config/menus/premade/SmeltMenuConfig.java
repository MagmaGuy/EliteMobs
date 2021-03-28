package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SmeltMenuConfig extends MenusConfigFields {

    public static String shopName;
    public static ItemStack infoButton;
    public static ItemStack inputInfoButton;
    public static ItemStack outputInfoButton;
    public static int infoSlot;
    public static int inputInformationSlot;
    public static int outputInformationSlot;
    public static List<Integer> inputSlots;
    public static List<Integer> outputSlots;
    public static ItemStack cancelButton;
    public static int cancelSlot;
    public static ItemStack confirmButton;
    public static int confirmSlot;

    public SmeltMenuConfig() {
        super("smelt_menu");
    }

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        shopName = ConfigurationEngine.setString(fileConfiguration, "shopName", "[EM] Place 25 scrap!");
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        Arrays.asList("&8Support the plugins you enjoy!",
                                "&aClick on &c25 &ascrap in your inventory",
                                "&ain order to craft item upgrade orbs!")),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoButtonSlot", 4);
        inputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationInputButtonSlot", 11);
        inputSlots = ConfigurationEngine.setList(fileConfiguration, "inputSlots", Arrays.asList(19, 20, 21, 28, 29, 30, 37, 38, 39));
        ItemStackSerializer.serialize(
                "inputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Scrap Input slots",
                        Arrays.asList("&aPut &c25 &ascrap of the same level", "&ahere in order to craft an Item Upgrade Orb!")),
                fileConfiguration);
        inputInfoButton = ItemStackSerializer.deserialize("inputInformationButton", fileConfiguration);
        outputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationOutputButtonSlot", 15);
        outputSlots = ConfigurationEngine.setList(fileConfiguration, "outputSlots", Arrays.asList(23, 24, 25, 32, 33, 34, 41, 42, 43));
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", Arrays.asList("&cCancel craft!")), fileConfiguration);
        ItemStackSerializer.serialize(
                "outputInformationButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER,
                        "&2Elite Orb Output slots",
                        Arrays.asList("&aPreview what you're crafting here!")),
                fileConfiguration);
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm Craft", Arrays.asList("&aCraft Item Upgrade Orbs!")), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmScrapSlot", 35);
    }

}
