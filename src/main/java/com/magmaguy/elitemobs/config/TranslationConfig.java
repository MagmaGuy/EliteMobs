package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by MagmaGuy on 12/05/2017.
 */
public class TranslationConfig {

    /*
    Translation for the economy messages
     */
    public static final String ECONOMY_PAY_MESSAGE = "Economy pay message v2";
    public static final String ECONOMY_CURRENCY_LEFT_MESSAGE = "Economy currency left message";
    public static final String ECONOMY_PAYMENT_RECEIVED_MESSAGE = "Economy money from payment message";
    public static final String ECONOMY_PAYMENT_INSUFICIENT_CURRENCY = "Economy payment insuficient currency";
    public static final String ECONOMY_WALLET_COMMAND = "Wallet command message";
    public static final String ECONOMY_TAX_MESSAGE = "Tax confirmation message";

    /*
    Translation for the shop messages
     */
    public static final String SHOP_BUY_MESSAGE = "Shop buy message";
    public static final String SHOP_SELL_MESSAGE = "Shop sell message";
    public static final String SHOP_INSUFFICIENT_FUNDS_MESSAGE = "Shop insufficient funds message";
    public static final String SHOP_SALE_INSTRUCTIONS = "Shop sale instructions";
    public static final String SHOP_SALE_OTHERS_ITEMS = "Shop sale player items warning";
    public static final String SHOP_CURRENT_BALANCE = "Shop current balance message";
    public static final String SHOP_ITEM_PRICE = "Shop item cost message";
    /*
    Translation for the teleport messages
     */
    public static final String TELEPORT_TIME_LEFT = "Teleport time left";
    public static final String TELEPORT_CANCELLED = "Teleport cancelled";
    public static final String NO_PENDING_COMMANDS = "noPendingCommands";
    public static final String CONFIG_NAME = "translation.yml";
    public static String shopBatchSellMessage;
    public static String TRACK_MESSAGE;
    public static String CHEST_LOW_RANK_MESSAGE;
    public static String CHEST_COOLDOWN_MESSAGE;
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    FileConfiguration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        //economy commands
        configuration.addDefault(ECONOMY_PAY_MESSAGE, "&2You have paid &2$amount_sent $currency_name &2to $receiver&2, who got $amount_received after taxes!");
        configuration.addDefault(ECONOMY_CURRENCY_LEFT_MESSAGE, "You now have &2$amount_left $currency_name");
        configuration.addDefault(ECONOMY_PAYMENT_RECEIVED_MESSAGE, "You have received &2$amount_received $currency_name &ffrom $sender");
        configuration.addDefault(ECONOMY_PAYMENT_INSUFICIENT_CURRENCY, "&cYou don't have enough $currency_name to do that!");
        configuration.addDefault(ECONOMY_WALLET_COMMAND, "You have &2$balance $currency_name");
        configuration.addDefault(ECONOMY_TAX_MESSAGE, "&cSending a payment will cost $percentage% in taxes. &aDo &9$command &ato proceed!");
        shopBatchSellMessage = ConfigurationEngine.setString(configuration, "shopBatchSellItem", "&aYou have sold your items &afor $currency_amount $currency_name!");
        //shop messages
        configuration.addDefault(SHOP_BUY_MESSAGE, "&aYou have bought $item_name &afor $item_value $currency_name!");
        configuration.addDefault(SHOP_CURRENT_BALANCE, "&aYou have $currency_amount $currency_name.");
        configuration.addDefault(SHOP_INSUFFICIENT_FUNDS_MESSAGE, "&cYou don't have enough $currency_name!");
        configuration.addDefault(SHOP_ITEM_PRICE, "That item costs &c$item_value $currency_name.");
        configuration.addDefault(SHOP_SELL_MESSAGE, "&aYou have sold $item_name &afor $currency_amount $currency_name!");

        configuration.addDefault(SHOP_SALE_OTHERS_ITEMS, "&cYou can't sell items that are not currently soulbound to you! This includes items from other prestige tiers!");
        configuration.addDefault(SHOP_SALE_INSTRUCTIONS, "&cYou can only sell EliteMobs loot here! (Armor / weapons dropped from elites showing a value on their lore)");

        configuration.addDefault(TELEPORT_TIME_LEFT, "&7[EM] Teleporting in &a$time &7seconds...");
        configuration.addDefault(TELEPORT_CANCELLED, "&7[EM] &cTeleport interrupted!");

        configuration.addDefault(NO_PENDING_COMMANDS, "&cYou don't currently have any pending commands!");

        TRACK_MESSAGE = ConfigurationEngine.setString(configuration, "trackMessage", "Track the $name");

        CHEST_LOW_RANK_MESSAGE = ConfigurationEngine.setString(configuration, "chestLowRankMessage", "&7[EM] &cYour guild rank needs to be at least $rank &cin order to open this chest!");
        CHEST_COOLDOWN_MESSAGE = ConfigurationEngine.setString(configuration, "chestCooldownMessage", "&7[EM] &cYou've already opened this chest recently! Wait $time!");

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
