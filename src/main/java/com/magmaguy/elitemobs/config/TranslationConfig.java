package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

/**
 * Created by MagmaGuy on 12/05/2017.
 */
public class TranslationConfig {

    /*
    Translation for commands
     */
    public static final String MISSING_PERMISSION_TITLE = "Missing permission message title";
    public static final String MISSING_PERMISSION_SUBTITLE = "Missing permission message subtitle";
    public static final String MISSING_PERMISSION_MESSAGE = "Missing permission message";
    public static final String VALID_COMMANDS = "Valid commands message";
    public static final String INVALID_COMMAND = "Invalid command message";

    /*
    Translation for the economy messages
     */
    public static final String ECONOMY_PAY_MESSAGE = "Economy pay message";
    public static final String ECONOMY_CURRENCY_LEFT_MESSAGE = "Economy currency left message";
    public static final String ECONOMY_PAYMENT_RECIEVED_MESSAGE = "Economy money from payment message";
    public static final String ECONOMY_NEGATIVE_VALUE_MESSAGE = "Economy payment for negative value";
    public static final String ECONOMY_PAYMENT_INSUFICIENT_CURRENCY = "Economy payment insuficient currency";
    public static final String ECONOMY_INVALID_PAY_COMMAND_SYNTAX = "Economy invalid pay command syntax";
    public static final String ECONOMY_WALLET_COMMAND = "Wallet command message";

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

    public static final String NPC_SLEEPING_MESSAGE = "npcSleepingMessage";


    public static final String CONFIG_NAME = "translation.yml";
    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        //commands
        configuration.addDefault(MISSING_PERMISSION_TITLE, "I'm afraid I can't let you do that, $username.");
        configuration.addDefault(MISSING_PERMISSION_SUBTITLE, "You need the following permission: $permission");
        configuration.addDefault(MISSING_PERMISSION_MESSAGE, "[EliteMobs] You may not run this command.");
        configuration.addDefault(VALID_COMMANDS, "Valid commands:");
        configuration.addDefault(INVALID_COMMAND, "Command not recognized. Valid commands:");

        //economy commands
        configuration.addDefault(ECONOMY_PAY_MESSAGE, "You have paid &2$amount_sent $currency_name &fto $receiver");
        configuration.addDefault(ECONOMY_CURRENCY_LEFT_MESSAGE, "You now have &2$amount_left $currency_name");
        configuration.addDefault(ECONOMY_PAYMENT_RECIEVED_MESSAGE, "You have received &2$amount_sent $currency_name &ffrom $sender");
        configuration.addDefault(ECONOMY_NEGATIVE_VALUE_MESSAGE, "&cNice try. This plugin doesn't make the same mistake as some banks have in the past.");
        configuration.addDefault(ECONOMY_PAYMENT_INSUFICIENT_CURRENCY, "&cYou don't have enough $currency_name to do that!");
        configuration.addDefault(ECONOMY_INVALID_PAY_COMMAND_SYNTAX, "&cInput not valid. Command format: &e/em pay [playerName] [amount]");
        configuration.addDefault(ECONOMY_WALLET_COMMAND, "You have &2$balance $currency_name");

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

        configuration.addDefault(NPC_SLEEPING_MESSAGE, "&7<Sleeping>");

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
