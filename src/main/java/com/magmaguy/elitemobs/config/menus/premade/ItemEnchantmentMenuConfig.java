package com.magmaguy.elitemobs.config.menus.premade;

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
    private static List<Integer> cancelSlots;
    @Getter
    private static ItemStack cancelButton;
    @Getter
    private static List<Integer> confirmSlots;
    @Getter
    private static ItemStack confirmButton;
    @Getter
    private static int itemSlot;
    @Getter
    private static int enchantedBookSlot;

    @Getter
    private static int luckyTicketSlot;
    @Getter
    private static String enchantmentLimitMessage;


    public ItemEnchantmentMenuConfig() {
        super("item_enchantment_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        menuName = ConfigurationEngine.setString(
                List.of("Sets the display name of the menu"),
                file, fileConfiguration, "menuName", ":offset_-8::elitemob_enchant:", true);
        cancelSlots = ConfigurationEngine.setInt(
                fileConfiguration, "cancelSlot", Arrays.asList(29, 30, 38, 39));
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.PAPER,
                "&4Cancel", List.of(
                        "&cCancel repair!"
                ),
                2001), fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        confirmSlots = ConfigurationEngine.setInt(
                fileConfiguration, "confirmSlots", Arrays.asList(32, 33, 41, 42));
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.PAPER,
                "&2Enchant!", List.of(
                        "&aPrice: $price $currencyName",
                        "&2Chance of success: $successChance%",
                        "&cChance of failure: $failureChance%",
                        "&6Challenge chance: $challengeChance%",
                        "&4Chance of critical failure: $criticalFailureChance%"),
                2001), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        itemSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the elite item in the chest menu."),
                fileConfiguration, "itemSlot", 1);

        enchantedBookSlot = ConfigurationEngine.setInt(
                List.of("Sets the item slot number of the enchanted book in the chest menu."),
                fileConfiguration, "enchantedBookSlot", 3);

        luckyTicketSlot = ConfigurationEngine.setInt(fileConfiguration, "luckyTicketSlot", 5);

        enchantmentLimitMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a player attempts to enchant an item beyond the configured maximum enchantment level of any enchantment."),
                file,
                fileConfiguration,
                "enchantmentLimitMessage",
                "&c[EliteRabbit] Can't enchant item beyond enchantment limit!",
                true);
    }
}
