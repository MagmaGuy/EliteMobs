package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EnhancementMenuConfig extends MenusConfigFields {
    public static String shopName;
    public static ItemStack infoButton;
    public static ItemStack eliteItemInputInfoButton;
    public static ItemStack eliteUpgradeOrbInputInfoButton;
    public static ItemStack outputInfoButton;
    public static int infoSlot;
    public static int eliteItemInputInformationSlot;
    public static int eliteUpgradeOrbInputInformationSlot;
    public static int outputInformationSlot;
    public static int eliteItemInputSlot;
    public static int eliteUpgradeOrbInputSlot;
    public static int outputSlot;
    public static ItemStack cancelButton;
    public static int cancelSlot;
    public static ItemStack confirmButton;
    public static int confirmSlot;

    public EnhancementMenuConfig() {
        super("enhancement_menu");
    }

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        shopName = ConfigurationEngine.setString(fileConfiguration, "shopName", "[EM] Enhancement menu!");
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoButtonSlot", 4);
        eliteItemInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputInformationSlot", 20);
        eliteItemInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputSlot", 29);
        eliteUpgradeOrbInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteUpgradeOrbInputInformationSlot", 22);
        eliteUpgradeOrbInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteUpgradeOrbInputSlot", 31);
        outputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationOutputButtonSlot", 24);
        outputSlot = ConfigurationEngine.setInt(fileConfiguration, "outputSlot", 33);
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        Arrays.asList("&8Support the plugins you enjoy!",
                                "&aUse Item Upgrade Orbs one level higher", " than your Elite Item in order to", "upgrade Elite Items!")),
                fileConfiguration);
        ItemStackSerializer.serialize(
                "eliteItemInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Item Input slots",
                        Arrays.asList("&aThis slot is for your elite item!")),
                fileConfiguration);
        ItemStackSerializer.serialize(
                "eliteUpgradeOrbInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Upgrade Orb Input slot",
                        Arrays.asList("&aThis slot is for your Elite Item Upgrade Orb!")),
                fileConfiguration);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", Arrays.asList("&cCancel upgrade!")), fileConfiguration);
        ItemStackSerializer.serialize(
                "outputInformationButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER,
                        "&2Output slots",
                        Arrays.asList("&aThis slot previews the result of your upgrade!")),
                fileConfiguration);
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        eliteUpgradeOrbInputInfoButton = ItemStackSerializer.deserialize("eliteUpgradeOrbInputInformationButton", fileConfiguration);
        eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm!", Arrays.asList("&aUpgrade items!")), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmRepairSlot", 35);
    }
}
