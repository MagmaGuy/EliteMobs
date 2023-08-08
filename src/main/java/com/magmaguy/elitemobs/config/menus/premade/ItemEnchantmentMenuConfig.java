package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ItemEnchantmentMenuConfig extends MenusConfigFields {
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
    private static int itemSlot;
    @Getter
    private static int itemInfoSlot;
    @Getter
    private static ItemStack itemInfoButton;
    @Getter
    private static int enchantedBookSlot;
    @Getter
    private static int enchantedBookInfoSlot;
    @Getter
    private static ItemStack enchantedBookInfoButton;

    @Getter
    private static int luckyTicketSlot;
    @Getter
    private static int luckyTicketInfoSlot;
    @Getter
    private static ItemStack luckyTicketInfoButton;
    @Getter
    private static String enchantmentLimitMessage;


    public ItemEnchantmentMenuConfig() {
        super("item_enchantment_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        menuName = ConfigurationEngine.setString(
                List.of("Sets the display name of the menu"),
                file, fileConfiguration, "menuName", "&6Enchantment Menu", true);
        infoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the information button in the chest menu."),
                fileConfiguration, "infoSlot", 4);
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&2Enchantment info:",
                        Arrays.asList(
                                "&2This menu can add enchants to your elite items!",
                                "&aIn order to enchant your elite item, add your",
                                "&aelite item and your elite enchanted books!",
                                "&6Items that already have a lot of enchantments",
                                "&6(high quality items) have a high chance of failing!",
                                "&8Normal failure makes you fail to enchant the item.",
                                "&8Challenge makes you fight a boss for the enchant.",
                                "&8Critical failures make you lose the item!"), MetadataHandler.signatureID),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the cancel button in the chest menu."),
                fileConfiguration, "cancelSlot", 27);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel repair!"), MetadataHandler.signatureID), fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        confirmSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the confirm button in the chest menu."),
                fileConfiguration, "confirmSlot", 35);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Enchant!", List.of(
                        "&aPrice: $price $currencyName",
                        "&2Chance of success: $successChance%",
                        "&cChance of failure: $failureChance%",
                        "&6Challenge chance: $challengeChance%",
                        "&4Chance of critical failure: $criticalFailureChance%"),
                31174), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        itemSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the elite item in the chest menu."),
                fileConfiguration, "itemSlot", 29);
        itemInfoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the elite item info button in the chest menu."),
                fileConfiguration, "itemInfoSlot", 20);
        ItemStackSerializer.serialize(
                "itemInfoButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Item Input slot",
                        List.of("&aPut your elite item here!"), MetadataHandler.signatureID),
                fileConfiguration);
        itemInfoButton = ItemStackSerializer.deserialize("itemInfoButton", fileConfiguration);
        enchantedBookInfoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the enchanted book info button in the chest menu."),
                fileConfiguration, "enchantedBookInfoSlot", 22);
        ItemStackSerializer.serialize(
                "enchantedBookInfoButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Enchanted Book Input slot",
                        List.of("&aPut your elite enchanted books here!"), MetadataHandler.signatureID),
                fileConfiguration);
        enchantedBookInfoButton = ItemStackSerializer.deserialize("enchantedBookInfoButton", fileConfiguration);
        enchantedBookSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the enchanted book in the chest menu."),
                fileConfiguration, "enchantedBookSlot", 31);
        luckyTicketSlot = ConfigurationEngine.setInt(fileConfiguration, "luckyTicketSlot", 33);
        luckyTicketInfoSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the lucky ticket info button in the chest menu."),
                fileConfiguration, "luckyTicketInfoSlot", 24);
        ItemStackSerializer.serialize(
                "luckyTicketInfoButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Lucky Ticket Input slot",
                        List.of("&a(Optional) Put your elite lucky tickets here!",
                                "&2Increase the odds of the enchantment succeeding",
                                "&2by adding an elite lucky ticket!"), MetadataHandler.signatureID),
                fileConfiguration);
        luckyTicketInfoButton = ItemStackSerializer.deserialize("luckyTicketInfoButton", fileConfiguration);
        enchantmentLimitMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a player attempts to enchant an item beyond the configured maximum enchantment level of any enchantment."),
                file,
                fileConfiguration,
                "enchantmentLimitMessage",
                "&c[EliteMobs] Can't enchant item beyond enchantment limit!",
                true);
    }
}
