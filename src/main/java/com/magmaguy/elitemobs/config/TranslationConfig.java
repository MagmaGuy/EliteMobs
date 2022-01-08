package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Created by MagmaGuy on 12/05/2017.
 */
public class TranslationConfig {
    /*
    Translation for the economy messages
     */
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
    /*
    Translation for the shop messages
     */
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
    /*
    Translation for the teleport messages
     */
    @Getter
    private static String teleportTimeLeft;
    @Getter
    private static String teleportCancelled;
    @Getter
    private static String noPendingCommands;
    @Getter
    private static String shopBatchSellMessage;
    @Getter
    private static String trackMessage;
    @Getter
    private static String chestLowRankMessage;
    @Getter
    private static String chestCooldownMessage;
    @Getter
    private static String insufficientCurrencyForWormholeMessage;
    @Getter
    private static String missingWormholeDestinationMessage;
    private TranslationConfig() {
    }

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("translation.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        economyPayMessage = ConfigurationEngine.setString(fileConfiguration, "Economy pay message v2", "&2You have paid &2$amount_sent $currency_name &2to $receiver&2, who got $amount_received after taxes!");
        economyCurrencyLeftMessage = ConfigurationEngine.setString(fileConfiguration, "Economy currency left message", "You now have &2$amount_left $currency_name");
        economyPaymentReceivedMessage = ConfigurationEngine.setString(fileConfiguration, "Economy money from payment message", "You have received &2$amount_received $currency_name &ffrom $sender");
        economyPaymentInsufficientCurrency = ConfigurationEngine.setString(fileConfiguration, "Economy payment insuficient currency", "&cYou don't have enough $currency_name to do that!");
        economyWalletCommand = ConfigurationEngine.setString(fileConfiguration, "Wallet command message", "You have &2$balance $currency_name");
        economyTaxMessage = ConfigurationEngine.setString(fileConfiguration, "Tax confirmation message", "&cSending a payment will cost $percentage% in taxes. &aDo &9$command &ato proceed!");
        shopBatchSellMessage = ConfigurationEngine.setString(fileConfiguration, "shopBatchSellItem", "&aYou have sold your items &afor $currency_amount $currency_name!");
        shopBuyMessage = ConfigurationEngine.setString(fileConfiguration, "Shop buy message", "&aYou have bought $item_name &afor $item_value $currency_name!");
        shopCurrentBalance = ConfigurationEngine.setString(fileConfiguration, "Shop current balance message", "&aYou have $currency_amount $currency_name.");
        shopInsufficientFundsMessage = ConfigurationEngine.setString(fileConfiguration, "Shop insufficient funds message", "&cYou don't have enough $currency_name!");
        shopItemPrice = ConfigurationEngine.setString(fileConfiguration, "Shop item cost message", "That item costs &c$item_value $currency_name.");
        shopSellMessage = ConfigurationEngine.setString(fileConfiguration, "Shop sell message", "&aYou have sold $item_name &afor $currency_amount $currency_name!");
        shopSaleOthersItems = ConfigurationEngine.setString(fileConfiguration, "Shop sale player items warning", "&cYou can't sell items that are not currently soulbound to you! This includes items from other prestige tiers!");
        shopSaleInstructions = ConfigurationEngine.setString(fileConfiguration, "Shop sale instructions", "&cYou can only sell EliteMobs loot here! (Armor / weapons dropped from elites showing a value on their lore)");
        teleportTimeLeft = ConfigurationEngine.setString(fileConfiguration, "Teleport time left", "&7[EM] Teleporting in &a$time &7seconds...");
        teleportCancelled = ConfigurationEngine.setString(fileConfiguration, "Teleport cancelled", "&7[EM] &cTeleport interrupted!");
        noPendingCommands = ConfigurationEngine.setString(fileConfiguration, "noPendingCommands", "&cYou don't currently have any pending commands!");
        trackMessage = ConfigurationEngine.setString(fileConfiguration, "trackMessage", "Track the $name");
        chestLowRankMessage = ConfigurationEngine.setString(fileConfiguration, "chestLowRankMessage", "&7[EM] &cYour guild rank needs to be at least $rank &cin order to open this chest!");
        chestCooldownMessage = ConfigurationEngine.setString(fileConfiguration, "chestCooldownMessage", "&7[EM] &cYou've already opened this chest recently! Wait $time!");
        insufficientCurrencyForWormholeMessage = ConfigurationEngine.setString(fileConfiguration, "insufficientCurrencyForWormholeMessage", "&8[EliteMobs] &cInsufficient currency! You need $amount to use this wormhole!");
        missingWormholeDestinationMessage = ConfigurationEngine.setString(fileConfiguration, "missingWormholeDestinationMessage", "&8[EliteMobs] &cDestination not found! Seems like you have not installed this content!");
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
