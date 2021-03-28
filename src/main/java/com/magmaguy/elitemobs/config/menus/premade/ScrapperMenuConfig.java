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

public class ScrapperMenuConfig extends MenusConfigFields {

    public ScrapperMenuConfig() {
        super("scrapper_menu");
    }

    public static String shopName;
    public static ItemStack infoButton;
    public static int infoSlot;
    public static List<Integer> storeSlots;
    public static ItemStack cancelButton;
    public static int cancelSlot;
    public static ItemStack confirmButton;
    public static int confirmSlot;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        shopName = ConfigurationEngine.setString(fileConfiguration, "shopName", "[EM] Scrapper");
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        Arrays.asList("&8Support the plugins you enjoy!",
                                "&4Warning!",
                                "&cItems scrapped here are lost!",
                                "&cThere is a 50% chance to get",
                                "&cscrap when scrapping items!",
                                "&aUse scrap at the smelter,",
                                "&arepairman and refiner!")),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoButtonSlot", 4);
        storeSlots = ConfigurationEngine.setList(fileConfiguration, "scrapSlots", Arrays.asList(19, 20, 21, 22, 23,
                24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", Arrays.asList("&cCancel scrap!")), fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm Scrap", Arrays.asList("&aScrap items!", "&a50% chance of success!")), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmScrapSlot", 35);
    }

}
