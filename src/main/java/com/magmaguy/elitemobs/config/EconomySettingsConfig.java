package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomySettingsConfig extends ConfigurationFile {
    @Getter
    private static boolean enableEconomy;
    @Getter
    private static double resaleValue;
    @Getter
    private static String currencyName;
    @Getter
    private static boolean useVault;
    @Getter
    private static boolean enableCurrencyShower;
    @Getter
    private static double currencyShowerMultiplier;
    @Getter
    private static String chatCurrencyShowerMessage;
    @Getter
    private static String actionBarCurrencyShowerMessage;
    @Getter
    private static String lootShowerMaterial1;
    @Getter
    private static String lootShowerMaterial5;
    @Getter
    private static String lootShowerMaterial10;
    @Getter
    private static String lootShowerMaterial20;
    @Getter
    private static String lootShowerMaterial50;
    @Getter
    private static String lootShowerMaterial100;
    @Getter
    private static String lootShowerMaterial500;
    @Getter
    private static String lootShowerMaterial1000;
    @Getter
    private static String adventurersGuildNotificationMessage;
    @Getter
    private static double defaultMaterialWorth;
    @Getter
    private static FileConfiguration thisConfiguration;
    @Getter
    private static double playerToPlayerTaxes;
    @Getter
    private static String economyPayMessage;
    @Getter
    private static String economyCurrencyLeftMessage;
    @Getter
    private static String economyPaymentReceivedMessage;
    @Getter
    private static String economyPaymentInsufficientCurrency;
    @Getter
    private static String economyWalletCommand;
    @Getter
    private static String economyTaxMessage;
    @Getter
    private static String shopBuyMessage;
    @Getter
    private static String shopSellMessage;
    @Getter
    private static String shopInsufficientFundsMessage;
    @Getter
    private static String shopSaleInstructions;
    @Getter
    private static String shopSaleOthersItems;
    @Getter
    private static String shopCurrentBalance;
    @Getter
    private static String shopItemPrice;
    @Getter
    private static String shopBatchSellMessage;


    public EconomySettingsConfig() {
        super("EconomySettings.yml");
    }

    private static void addMaterial(FileConfiguration fileConfiguration, Material material, double value) {
        ConfigurationEngine.setDouble(
                List.of("Sets the worth of this material for the elitemobs currency system."),
                fileConfiguration, "materialWorth." + material.name(), value);
    }

    public static double getMaterialWorth(Material material) {
        try {
            return thisConfiguration.getDouble("materialWorth." + material.name());
        } catch (Exception ex) {
            return defaultMaterialWorth;
        }
    }

    @Override
    public void initializeValues() {
        thisConfiguration = fileConfiguration;
        double netheriteLevel = CombatSystem.NETHERITE_TIER_LEVEL + 10D;
        double tridentLevel = CombatSystem.DIAMOND_TIER_LEVEL + 10D;
        double diamondLevel = CombatSystem.DIAMOND_TIER_LEVEL + 10D;
        double ironLevel = CombatSystem.IRON_TIER_LEVEL + 10D;
        double stoneChainLevel = CombatSystem.STONE_CHAIN_TIER_LEVEL + 10D;
        double goldWoodLeatherLevel = CombatSystem.GOLD_WOOD_LEATHER_TIER_LEVEL + 10D;

        enableEconomy = ConfigurationEngine.setBoolean(
                List.of("Sets if the EliteMobs economy is enabled. This means elite coins, the ability to buy and sell gear and the ability to upgrade guild ranks", "If disabled, players will not be able to progress in the plugin!"),
                fileConfiguration, "enableEconomy", true);
        resaleValue = ConfigurationEngine.setDouble(
                List.of("Sets the resale item of items, as a % of the original price. 5 is 5%"),
                fileConfiguration, "itemResaleValue", 5);
        currencyName = ConfigurationEngine.setString(
                List.of("Sets the in-game name of the currency used."),
                file, fileConfiguration, "currencyName", "Elite Coins", true);
        useVault = ConfigurationEngine.setBoolean(
                List.of("Sets the plugin to use Vault. THIS IS NOT RECOMMENDED! Read why here: https://github.com/MagmaGuy/EliteMobs/wiki/%5BThird-party-support%5D-Vault"),
                fileConfiguration, "useVault - not recommended", false);
        enableCurrencyShower = ConfigurationEngine.setBoolean(
                List.of("Sets if elites will drop coins based on their level."),
                fileConfiguration, "enableCurrencyShower", true);
        currencyShowerMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier for the currency dropped by elites."),
                fileConfiguration, "currencyShowerTierMultiplier", 1D);
        chatCurrencyShowerMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when players pick up elite currency."),
                file, fileConfiguration, "chatCurrencyShowerMessage", "&7[EM] You've picked up &a$amount $currency_name!", true);
        actionBarCurrencyShowerMessage = ConfigurationEngine.setString(
                List.of("Sets the action bar message sent when players pick up elite currency."),
                file, fileConfiguration, "actionbarCurrencyShowerMessage", "&7[EM] You've picked up &a$amount $currency_name!", true);
        lootShowerMaterial1 = ConfigurationEngine.setString(
                List.of("Sets the material type of 1 dropped elite coin."),
                file, fileConfiguration, "lootShowerMaterial.1", Material.GOLD_NUGGET.name(), false);
        ConfigurationEngine.setInt(
                List.of("Sets the custom model ID for 1 dropped elite coin. Used by the resource pack."),
                fileConfiguration, "lootShowerData.1", 1);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.1", "elitemobs:coins/coin1");
        lootShowerMaterial5 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.5", Material.GOLD_INGOT.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.5", "elitemobs:coins/coin1");
        lootShowerMaterial10 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.10", Material.GOLD_BLOCK.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.10", "elitemobs:coins/coin2");
        lootShowerMaterial20 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.20", Material.EMERALD.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.20", "elitemobs:coins/coin3");
        lootShowerMaterial50 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.50", Material.EMERALD_BLOCK.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.50", "elitemobs:coins/coin4");
        lootShowerMaterial100 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.100", Material.DIAMOND.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.100", "elitemobs:coins/coin4");
        lootShowerMaterial500 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.500", Material.DIAMOND_BLOCK.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.500", "elitemobs:coins/coin4");
        lootShowerMaterial1000 = ConfigurationEngine.setString(file, fileConfiguration, "lootShowerMaterial.1000", Material.NETHER_STAR.name(), false);
        ConfigurationEngine.setString(fileConfiguration, "lootShowerDataV2.1000", "elitemobs:coins/coin4");
        adventurersGuildNotificationMessage = ConfigurationEngine.setString(
                List.of("Send the message players get after looting currency. Useful for tutorial purposes."),
                file, fileConfiguration, "adventurersGuildNotificationMessages", "&7[EM] Extra spending money? Try &a/ag !", true);

        addMaterial(fileConfiguration, Material.DIAMOND_AXE, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_BOOTS, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_CHESTPLATE, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_LEGGINGS, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_HELMET, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_PICKAXE, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_SHOVEL, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_SWORD, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_HOE, diamondLevel);

        addMaterial(fileConfiguration, Material.IRON_AXE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_BOOTS, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_LEGGINGS, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_CHESTPLATE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_HELMET, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_PICKAXE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_SHOVEL, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_HOE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_SWORD, ironLevel);
        addMaterial(fileConfiguration, Material.SHIELD, ironLevel);
        addMaterial(fileConfiguration, Material.BOW, ironLevel);

        addMaterial(fileConfiguration, Material.CHAINMAIL_BOOTS, stoneChainLevel);
        addMaterial(fileConfiguration, Material.CHAINMAIL_LEGGINGS, stoneChainLevel);
        addMaterial(fileConfiguration, Material.CHAINMAIL_CHESTPLATE, stoneChainLevel);
        addMaterial(fileConfiguration, Material.CHAINMAIL_HELMET, stoneChainLevel);

        addMaterial(fileConfiguration, Material.STONE_SWORD, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_AXE, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_PICKAXE, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_SHOVEL, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_HOE, stoneChainLevel);

        addMaterial(fileConfiguration, Material.GOLDEN_AXE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_BOOTS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_LEGGINGS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_CHESTPLATE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_HELMET, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_SWORD, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_SHOVEL, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_PICKAXE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_HOE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_APPLE, diamondLevel);
        addMaterial(fileConfiguration, Material.ENCHANTED_GOLDEN_APPLE, diamondLevel);

        addMaterial(fileConfiguration, Material.LEATHER_BOOTS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.LEATHER_LEGGINGS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.LEATHER_CHESTPLATE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.LEATHER_HELMET, goldWoodLeatherLevel);

        addMaterial(fileConfiguration, Material.WOODEN_SWORD, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.WOODEN_AXE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.WOODEN_HOE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.WOODEN_PICKAXE, goldWoodLeatherLevel);

        addMaterial(fileConfiguration, Material.TRIDENT, tridentLevel);
        addMaterial(fileConfiguration, Material.ELYTRA, diamondLevel);
        addMaterial(fileConfiguration, Material.TURTLE_HELMET, goldWoodLeatherLevel);

        addMaterial(fileConfiguration, Material.NETHERITE_AXE, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_PICKAXE, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_SHOVEL, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_HOE, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_SWORD, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_HELMET, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_CHESTPLATE, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_LEGGINGS, netheriteLevel);
        addMaterial(fileConfiguration, Material.NETHERITE_BOOTS, netheriteLevel);

        defaultMaterialWorth = ConfigurationEngine.setDouble(
                List.of("Sets the default material worth for items not specifically defined."),
                fileConfiguration, "materialWorth.defaultMaterialWorth", 1);
        playerToPlayerTaxes = ConfigurationEngine.setDouble(
                List.of("Sets the tax rate for transactions between players.", "Strongly recommended for balance reasons, as high level players can gain up to 6x more currency and try to use other players to bypass prestige currency resets."),
                fileConfiguration, "playerToPlayerPaymentTaxes", 0.2);

        economyPayMessage = ConfigurationEngine.setString(
                List.of("Message sent when sending elite currency to other players."),
                file, fileConfiguration, "Economy pay message v2", "&2You have paid &2$amount_sent $currency_name &2to $receiver&2, who got $amount_received after taxes!", true);
        economyCurrencyLeftMessage = ConfigurationEngine.setString(
                List.of("Message sent after players send currency."),
                file, fileConfiguration, "Economy currency left message", "You now have &2$amount_left $currency_name", true);
        economyPaymentReceivedMessage = ConfigurationEngine.setString(
                List.of("Message received when receiving currency."),
                file, fileConfiguration, "Economy money from payment message", "You have received &2$amount_received $currency_name &ffrom $sender", true);
        economyPaymentInsufficientCurrency = ConfigurationEngine.setString(
                List.of("Message sent when players try to send an amount of coins they do not have."),
                file, fileConfiguration, "Economy payment insufficient currency", "&cYou don't have enough $currency_name to do that!", true);
        economyWalletCommand = ConfigurationEngine.setString(
                List.of("/em balance message"),
                file, fileConfiguration, "Wallet command message", "You have &2$balance $currency_name", true);
        economyTaxMessage = ConfigurationEngine.setString(
                List.of("Confirmation message sent when players try to send currency to another player."),
                file, fileConfiguration, "Tax confirmation message", "&cSending a payment will cost $percentage% in taxes. &aDo &9$command &ato proceed!", true);
        shopBuyMessage = ConfigurationEngine.setString(
                List.of("Message sent when a player buys from a shop."),
                file, fileConfiguration, "Shop buy message", "&aYou have bought $item_name &afor $item_value $currency_name!", true);
        shopCurrentBalance = ConfigurationEngine.setString(
                List.of("Message sent when a player interacts with a shop."),
                file, fileConfiguration, "Shop current balance message", "&aYou have $currency_amount $currency_name.", true);
        shopInsufficientFundsMessage = ConfigurationEngine.setString(
                List.of("Message sent when players don't have enough currency to purchase an item"),
                file, fileConfiguration, "Shop insufficient funds message", "&cYou don't have enough $currency_name!", true);
        shopItemPrice = ConfigurationEngine.setString(
                List.of("Second part of message sent when players try to purchase an item they can not afford."),
                file, fileConfiguration, "Shop item cost message", "That item costs &c$item_value $currency_name.", true);
        shopSellMessage = ConfigurationEngine.setString(
                List.of("Message sent upon selling an item to a shop."),
                file, fileConfiguration, "Shop sell message", "&aYou have sold $item_name &afor $currency_amount $currency_name!", true);
        shopSaleOthersItems = ConfigurationEngine.setString(
                List.of("Message sent upon trying to sell an item that does not belong to that player."),
                file, fileConfiguration, "Shop sale player items warning", "&cYou can't sell items that are not currently soulbound to you! This includes items from other prestige tiers!", true);
        shopSaleInstructions = ConfigurationEngine.setString(
                List.of("Message sent upon trying to sell a non-EliteMobs item"),
                file, fileConfiguration, "Shop sale instructions", "&cYou can only sell EliteMobs loot here! (Armor / weapons dropped from elites showing a value on their lore)", true);
        shopBatchSellMessage = ConfigurationEngine.setString(
                List.of("Message sent upon selling a batch of elite items."),
                file, fileConfiguration, "shopBatchSellItem", "&aYou have sold your items &afor $currency_amount $currency_name!", true);

    }
}
