package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.magmacore.util.Round;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class CurrencyCommandsHandler {

    public static void payCommand(Player sender, String recipientName, double amount) {

        //CASE: Negative amount
        if (amount <= 0) {
            sender.sendMessage(CommandMessagesConfig.getPayNiceTryMessage());
            return;
        }

        //CASE: Offline player
        Player recipient = Bukkit.getPlayer(recipientName);
        if (recipient == null) {
            sender.sendMessage(CommandMessagesConfig.getPayPlayerNotOnlineMessage().replace("$player", recipientName));
            return;
        }

        //CASE: Insufficient funds
        double senderCurrency = EconomyHandler.checkCurrency(sender.getUniqueId());
        if (senderCurrency - amount < 0) {
            sender.sendMessage(
                            EconomySettingsConfig.getEconomyPaymentInsufficientCurrency()
                                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
            return;
        }

        double actuallyReceivedAmount = Round.twoDecimalPlaces(amount - amount * EconomySettingsConfig.getPlayerToPlayerTaxes());
        double taxes = Round.twoDecimalPlaces(amount * EconomySettingsConfig.getPlayerToPlayerTaxes());

        //CASE: Successful payment
        EconomyHandler.addCurrency(recipient.getUniqueId(), actuallyReceivedAmount);
        EconomyHandler.subtractCurrency(sender.getUniqueId(), amount);

        sender.sendMessage(
                        EconomySettingsConfig.getEconomyPayMessage()
                                .replace("$amount_sent", amount + "")
                                .replace("$amount_received", actuallyReceivedAmount + "")
                                .replace("$taxes", taxes + "")
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                .replace("$receiver", recipient.getDisplayName()));

        sender.sendMessage(
                        EconomySettingsConfig.getEconomyCurrencyLeftMessage()
                                .replace("$amount_left", String.valueOf(EconomyHandler.checkCurrency(sender.getUniqueId())))
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

        recipient.sendMessage(
                        EconomySettingsConfig.getEconomyPaymentReceivedMessage()
                                .replace("$amount_sent", amount + "")
                                .replace("$amount_received", actuallyReceivedAmount + "")
                                .replace("$taxes", taxes + "")
                                .replace("$sender", sender.getDisplayName())
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

        recipient.sendMessage(
                        EconomySettingsConfig.getEconomyCurrencyLeftMessage()
                                .replace("$amount_left", String.valueOf(EconomyHandler.checkCurrency(recipient.getUniqueId())))
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

    }

    public static void addCommand(Player player, double amount) {
        EconomyHandler.addCurrency(player.getUniqueId(), amount);
    }

    public static void addCommand(CommandSender commandSender, String onlinePlayer, double amount) {
        Player player = Bukkit.getPlayer(onlinePlayer);
        if (player == null) {
            commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", onlinePlayer));
            return;
        }
        addCommand(player, amount);
        commandSender.sendMessage(CommandMessagesConfig.getCurrencyAddedMessage().replace("$amount", String.valueOf(amount)).replace("$player", onlinePlayer));
        commandSender.sendMessage(CommandMessagesConfig.getCurrencyNowHasMessage().replace("$amount", String.valueOf(EconomyHandler.checkCurrency(player.getUniqueId()))));
    }

    public static void addAllCommand(CommandSender commandSender, double amount) {
        for (Player player : Bukkit.getOnlinePlayers())
            addCommand(player, amount);
        commandSender.sendMessage(CommandMessagesConfig.getCurrencyAddedAllMessage().replace("$amount", String.valueOf(amount)));
    }

    public static void subtractCommand(String playerName, double amount) {
        EconomyHandler.subtractCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);
    }

    public static void subtractCommand(CommandSender commandSender, String onlinePlayer, int amount) {
        Player player = Bukkit.getPlayer(onlinePlayer);
        if (player == null) {
            commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", onlinePlayer));
            return;
        }
        subtractCommand(onlinePlayer, amount);
        commandSender.sendMessage(CommandMessagesConfig.getCurrencySubtractedMessage().replace("$amount", String.valueOf(amount)).replace("$player", onlinePlayer));
        commandSender.sendMessage(CommandMessagesConfig.getCurrencyNowHasMessage().replace("$amount", String.valueOf(EconomyHandler.checkCurrency(player.getUniqueId()))));
    }

    public static void setCommand(CommandSender commandSender, String onlinePlayer, double amount) {
        Player player = Bukkit.getPlayer(onlinePlayer);
        if (player == null) {
            commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", onlinePlayer));
            return;
        }
        EconomyHandler.setCurrency(player.getUniqueId(), amount);
        commandSender.sendMessage(CommandMessagesConfig.getCurrencySetMessage().replace("$player", onlinePlayer).replace("$currencyName", EconomySettingsConfig.getCurrencyName()).replace("$amount", String.valueOf(amount)));
    }


    public static void checkCommand(CommandSender commandSender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            commandSender.sendMessage(CommandMessagesConfig.getCurrencyPlayerNotValidMessage().replace("$player", playerName));
            return;
        }
        double money = EconomyHandler.checkCurrency(player.getUniqueId());
        commandSender.sendMessage(CommandMessagesConfig.getCurrencyCheckMessage().replace("$player", playerName).replace("$amount", String.valueOf(money)).replace("$currencyName", EconomySettingsConfig.getCurrencyName()));
    }

    public static void walletCommand(Player player) {
        player.sendMessage(
                        EconomySettingsConfig.getEconomyWalletCommand()
                                .replace("$balance", String.valueOf(EconomyHandler.checkCurrency(player.getUniqueId())))
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
    }

}
