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
                List.of("Currency value of this material when sold to shops."),
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
                List.of("Whether the EliteMobs economy is enabled (elite coins, shops, guild ranks).",
                        "If disabled, players will not be able to progress in the plugin!"),
                fileConfiguration, "enableEconomy", true);
        resaleValue = ConfigurationEngine.setDouble(
                List.of("Percentage of the original price players get when reselling items. 5 = 5%."),
                fileConfiguration, "itemResaleValue", 5);
        currencyName = ConfigurationEngine.setString(
                List.of("The display name of the currency used throughout the plugin."),
                file, fileConfiguration, "currencyName", "Elite Coins", true);
        useVault = ConfigurationEngine.setBoolean(
                List.of("Whether to use Vault for the economy instead of the built-in system.",
                        "NOT RECOMMENDED! See: https://wiki.nightbreak.io/EliteMobs/vault"),
                fileConfiguration, "useVault", false);
        enableCurrencyShower = ConfigurationEngine.setBoolean(
                List.of("Whether elites drop currency (coins) on death."),
                fileConfiguration, "enableCurrencyShower", true);
        currencyShowerMultiplier = ConfigurationEngine.setDouble(
                List.of("Multiplier for currency dropped by elites. 1.0 = normal, 2.0 = double drops."),
                fileConfiguration, "currencyShowerTierMultiplier", 1D);
        chatCurrencyShowerMessage = ConfigurationEngine.setString(
                List.of("Chat message shown when a player picks up elite currency.",
                        "Placeholders: $amount, $currency_name"),
                file, fileConfiguration, "chatCurrencyShowerMessage", "&7[EM] You've picked up &a$amount $currency_name!", true);
        actionBarCurrencyShowerMessage = ConfigurationEngine.setString(
                List.of("Action bar message shown when a player picks up elite currency.",
                        "Placeholders: $amount, $currency_name"),
                file, fileConfiguration, "actionbarCurrencyShowerMessage", "&7[EM] You've picked up &a$amount $currency_name!", true);
        lootShowerMaterial1 = ConfigurationEngine.setString(
                List.of("Material used for dropped coins worth 1 currency."),
                file, fileConfiguration, "lootShowerMaterial.1", Material.GOLD_NUGGET.name(), false);
        ConfigurationEngine.setInt(
                List.of("Custom model data ID for dropped coins worth 1 currency. Used by the resource pack."),
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
                List.of("Hint message shown to players after picking up currency, pointing them to the Adventurer's Guild."),
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
                List.of("Default currency value for materials not listed above."),
                fileConfiguration, "materialWorth.defaultMaterialWorth", 1);
        playerToPlayerTaxes = ConfigurationEngine.setDouble(
                List.of("Tax rate (0.0-1.0) on player-to-player currency transfers. 0.2 = 20% tax.",
                        "Recommended to prevent high-level players from funneling currency through alts to avoid taxes."),
                fileConfiguration, "playerToPlayerPaymentTaxes", 0.2);

        economyPayMessage = ConfigurationEngine.setString(
                List.of("Message shown to the sender when paying another player.",
                        "Placeholders: $amount_sent, $currency_name, $receiver, $amount_received"),
                file, fileConfiguration, "economyPayMessageV2", "&2You have paid &2$amount_sent $currency_name &2to $receiver&2, who got $amount_received after taxes!", true);
        economyCurrencyLeftMessage = ConfigurationEngine.setString(
                List.of("Message showing remaining balance after paying another player.",
                        "Placeholders: $amount_left, $currency_name"),
                file, fileConfiguration, "economyCurrencyLeftMessage", "You now have &2$amount_left $currency_name", true);
        economyPaymentReceivedMessage = ConfigurationEngine.setString(
                List.of("Message shown to the recipient when receiving currency from another player.",
                        "Placeholders: $amount_received, $currency_name, $sender"),
                file, fileConfiguration, "economyPaymentReceivedMessage", "You have received &2$amount_received $currency_name &ffrom $sender", true);
        economyPaymentInsufficientCurrency = ConfigurationEngine.setString(
                List.of("Message shown when a player tries to pay another player more currency than they have.",
                        "Placeholder: $currency_name"),
                file, fileConfiguration, "economyPaymentInsufficientCurrency", "&cYou don't have enough $currency_name to do that!", true);
        economyWalletCommand = ConfigurationEngine.setString(
                List.of("Message shown when a player checks their balance with /em wallet.",
                        "Placeholders: $balance, $currency_name"),
                file, fileConfiguration, "walletCommandMessage", "You have &2$balance $currency_name", true);
        shopBuyMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player buys an item from the shop.",
                        "Placeholders: $item_name, $item_value, $currency_name"),
                file, fileConfiguration, "shopBuyMessage", "&aYou have bought $item_name &afor $item_value $currency_name!", true);
        shopCurrentBalance = ConfigurationEngine.setString(
                List.of("Message showing the player's current balance when interacting with a shop.",
                        "Placeholders: $currency_amount, $currency_name"),
                file, fileConfiguration, "shopCurrentBalanceMessage", "&aYou have $currency_amount $currency_name.", true);
        shopInsufficientFundsMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player cannot afford to buy an item from the shop.",
                        "Placeholder: $currency_name"),
                file, fileConfiguration, "shopInsufficientFundsMessage", "&cYou don't have enough $currency_name!", true);
        shopItemPrice = ConfigurationEngine.setString(
                List.of("Message showing the price of an item the player cannot afford.",
                        "Placeholders: $item_value, $currency_name"),
                file, fileConfiguration, "shopItemCostMessage", "That item costs &c$item_value $currency_name.", true);
        shopSellMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player sells an item to the shop.",
                        "Placeholders: $item_name, $currency_amount, $currency_name"),
                file, fileConfiguration, "shopSellMessage", "&aYou have sold $item_name &afor $currency_amount $currency_name!", true);
        shopSaleOthersItems = ConfigurationEngine.setString(
                List.of("Message shown when a player tries to sell an item that is not soulbound to them."),
                file, fileConfiguration, "shopSalePlayerItemsWarning", "&cYou can't sell items that are not currently soulbound to you!", true);
        shopSaleInstructions = ConfigurationEngine.setString(
                List.of("Message shown when a player tries to sell a non-EliteMobs item to the shop."),
                file, fileConfiguration, "shopSaleInstructions", "&cYou can only sell EliteMobs loot here! (Armor / weapons dropped from elites showing a value on their lore)", true);
        shopBatchSellMessage = ConfigurationEngine.setString(
                List.of("Message sent upon selling a batch of elite items."),
                file, fileConfiguration, "shopBatchSellItem", "&aYou have sold your items &afor $currency_amount $currency_name!", true);

    }
}
