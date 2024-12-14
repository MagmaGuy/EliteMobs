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

public class ScrapperMenuConfig extends MenusConfigFields {

    public static String shopName;
    public static ItemStack infoButton;
    public static int infoSlot;
    public static List<Integer> storeSlots;
    public static ItemStack cancelButton;
    public static int cancelSlot;
    public static ItemStack confirmButton;
    public static int confirmSlot;
    public static double scrapChance;

    public ScrapperMenuConfig() {
        super("scrapper_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "shopName", "[EM] Scrapper", true);
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        new ArrayList<>(List.of("&8Support the plugins you enjoy!",
                                "&4Warning!",
                                "&cItems scrapped here are lost!",
                                "&cThere is a $chance% chance to get",
                                "&cscrap when scrapping items!",
                                "&aUse scrap at the smelter,",
                                "&arepairman and refiner!"))),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoButtonSlot", 4);
        storeSlots = ConfigurationEngine.setList(file, fileConfiguration, "scrapSlots", new ArrayList<>(List.of(19, 20, 21, 22, 23,
                24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)), false);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel scrap!"), MetadataHandler.signatureID), fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm Scrap", new ArrayList<>(List.of("&aScrap items!", "&a$chance% chance of success!")) ), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmScrapSlot", 35);
        scrapChance = ConfigurationEngine.setDouble(fileConfiguration, "scrapChance", 0.75);
    }

}
