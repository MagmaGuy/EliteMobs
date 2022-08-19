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
    private static String dismissEMMessage;
    @Getter
    private static String switchEMStyleMessage;

    private TranslationConfig() {
    }

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("translation.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        economyPayMessage = ConfigurationEngine.setString(file, fileConfiguration, "Economy pay message v2", "&2You have paid &2$amount_sent $currency_name &2to $receiver&2, who got $amount_received after taxes!", true);
        economyCurrencyLeftMessage = ConfigurationEngine.setString(file, fileConfiguration, "Economy currency left message", "You now have &2$amount_left $currency_name", true);
        economyPaymentReceivedMessage = ConfigurationEngine.setString(file, fileConfiguration, "Economy money from payment message", "You have received &2$amount_received $currency_name &ffrom $sender", true);
        economyPaymentInsufficientCurrency = ConfigurationEngine.setString(file, fileConfiguration, "Economy payment insuficient currency", "&cYou don't have enough $currency_name to do that!", true);
        economyWalletCommand = ConfigurationEngine.setString(file, fileConfiguration, "Wallet command message", "You have &2$balance $currency_name", true);
        economyTaxMessage = ConfigurationEngine.setString(file, fileConfiguration, "Tax confirmation message", "&cSending a payment will cost $percentage% in taxes. &aDo &9$command &ato proceed!", true);
        shopBatchSellMessage = ConfigurationEngine.setString(file, fileConfiguration, "shopBatchSellItem", "&aYou have sold your items &afor $currency_amount $currency_name!", true);
        shopBuyMessage = ConfigurationEngine.setString(file, fileConfiguration, "Shop buy message", "&aYou have bought $item_name &afor $item_value $currency_name!", true);
        shopCurrentBalance = ConfigurationEngine.setString(file, fileConfiguration, "Shop current balance message", "&aYou have $currency_amount $currency_name.", true);
        shopInsufficientFundsMessage = ConfigurationEngine.setString(file, fileConfiguration, "Shop insufficient funds message", "&cYou don't have enough $currency_name!", true);
        shopItemPrice = ConfigurationEngine.setString(file, fileConfiguration, "Shop item cost message", "That item costs &c$item_value $currency_name.", true);
        shopSellMessage = ConfigurationEngine.setString(file, fileConfiguration, "Shop sell message", "&aYou have sold $item_name &afor $currency_amount $currency_name!", true);
        shopSaleOthersItems = ConfigurationEngine.setString(file, fileConfiguration, "Shop sale player items warning", "&cYou can't sell items that are not currently soulbound to you! This includes items from other prestige tiers!", true);
        shopSaleInstructions = ConfigurationEngine.setString(file, fileConfiguration, "Shop sale instructions", "&cYou can only sell EliteMobs loot here! (Armor / weapons dropped from elites showing a value on their lore)", true);
        teleportTimeLeft = ConfigurationEngine.setString(file, fileConfiguration, "Teleport time left", "&7[EM] Teleporting in &a$time &7seconds...", true);
        teleportCancelled = ConfigurationEngine.setString(file, fileConfiguration, "Teleport cancelled", "&7[EM] &cTeleport interrupted!", true);
        noPendingCommands = ConfigurationEngine.setString(file, fileConfiguration, "noPendingCommands", "&cYou don't currently have any pending commands!", true);
        trackMessage = ConfigurationEngine.setString(file, fileConfiguration, "trackMessage", "Track the $name", true);
        chestLowRankMessage = ConfigurationEngine.setString(file, fileConfiguration, "chestLowRankMessage", "&7[EM] &cYour guild rank needs to be at least $rank &cin order to open this chest!", true);
        chestCooldownMessage = ConfigurationEngine.setString(file, fileConfiguration, "chestCooldownMessage", "&7[EM] &cYou've already opened this chest recently! Wait $time!", true);
        insufficientCurrencyForWormholeMessage = ConfigurationEngine.setString(file, fileConfiguration, "insufficientCurrencyForWormholeMessage", "&8[EliteMobs] &cInsufficient currency! You need $amount to use this com.magmaguy.elitemobs.wormhole!", true);
        dismissEMMessage = ConfigurationEngine.setString(file, fileConfiguration, "dismissEMMessage", "&8[EliteMobs] &2/elitemobs &fmenu not working for you? Try &2/elitemobs alt &fto see an alternative version of the menu! &cDon't want to see this message again? &4/em dismiss", true);
        switchEMStyleMessage = ConfigurationEngine.setString(file, fileConfiguration, "switchEMStyleMessage", "&8[EliteMobs] &2/elitemobs &fmenu style changed! Check it out!", true);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
