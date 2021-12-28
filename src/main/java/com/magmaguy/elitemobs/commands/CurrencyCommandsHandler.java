package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.utils.Round;
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
            sender.sendMessage(ChatColorConverter.convert("&4[EliteMobs]Nice try."));
            return;
        }

        //CASE: Offline player
        Player recipient = Bukkit.getPlayer(recipientName);
        if (recipient == null) {
            sender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Player " + recipientName + " is not online and can therefore not get a payment."));
            return;
        }

        //CASE: Insufficient funds
        double senderCurrency = EconomyHandler.checkCurrency(sender.getUniqueId());
        if (senderCurrency - amount < 0) {
            sender.sendMessage(
                    ChatColorConverter.convert(
                            TranslationConfig.getEconomyPaymentInsufficientCurrency()
                                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));
            return;
        }

        double actuallyReceivedAmount = Round.twoDecimalPlaces(amount - amount * EconomySettingsConfig.getPlayerToPlayerTaxes());
        double taxes = Round.twoDecimalPlaces(amount * EconomySettingsConfig.getPlayerToPlayerTaxes());

        //CASE: Successful payment
        EconomyHandler.addCurrency(recipient.getUniqueId(), actuallyReceivedAmount);
        EconomyHandler.subtractCurrency(sender.getUniqueId(), amount);

        sender.sendMessage(
                ChatColorConverter.convert(
                        TranslationConfig.getEconomyPayMessage()
                                .replace("$amount_sent", amount + "")
                                .replace("$amount_received", actuallyReceivedAmount + "")
                                .replace("$taxes", taxes + "")
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                .replace("$receiver", recipient.getDisplayName())));

        sender.sendMessage(
                ChatColorConverter.convert(
                        TranslationConfig.getEconomyCurrencyLeftMessage()
                                .replace("$amount_left", String.valueOf(EconomyHandler.checkCurrency(sender.getUniqueId())))
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

        recipient.sendMessage(
                ChatColorConverter.convert(
                        TranslationConfig.getEconomyPaymentReceivedMessage()
                                .replace("$amount_sent", amount + "")
                                .replace("$amount_received", actuallyReceivedAmount + "")
                                .replace("$taxes", taxes + "")
                                .replace("$sender", sender.getDisplayName())
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

        recipient.sendMessage(
                ChatColorConverter.convert(
                        TranslationConfig.getEconomyCurrencyLeftMessage()
                                .replace("$amount_left", String.valueOf(EconomyHandler.checkCurrency(recipient.getUniqueId())))
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

    }

    public static void addCommand(Player player, double amount) {
        EconomyHandler.addCurrency(player.getUniqueId(), amount);
    }

    public static void addCommand(CommandSender commandSender, String onlinePlayer, int amount) {
        Player player = Bukkit.getPlayer(onlinePlayer);
        if (player == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Player " + onlinePlayer + " &4is not valid!"));
            return;
        }
        addCommand(player, amount);
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2You have added " + amount + " to " + onlinePlayer));
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2They now have " + EconomyHandler.checkCurrency(player.getUniqueId())));
    }

    public static void addAllCommand(CommandSender commandSender, int amount) {
        for (Player player : Bukkit.getOnlinePlayers())
            addCommand(player, amount);
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2You have added " + amount + " to all online players."));
    }

    public static void subtractCommand(String playerName, double amount) {
        EconomyHandler.subtractCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);
    }

    public static void subtractCommand(CommandSender commandSender, String onlinePlayer, int amount) {
        Player player = Bukkit.getPlayer(onlinePlayer);
        if (player == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Player " + onlinePlayer + " &4is not valid!"));
            return;
        }
        subtractCommand(onlinePlayer, amount);
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2You have subtracted " + amount + " from " + onlinePlayer));
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]They now have " + EconomyHandler.checkCurrency(player.getUniqueId())));
    }

    public static void setCommand(CommandSender commandSender, String onlinePlayer, double amount) {
        Player player = Bukkit.getPlayer(onlinePlayer);
        if (player == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Player " + onlinePlayer + " &4is not valid!"));
            return;
        }
        EconomyHandler.setCurrency(player.getUniqueId(), amount);
        commandSender.sendMessage("You set " + onlinePlayer + "'s " + EconomySettingsConfig.getCurrencyName() + " to " + amount);
    }


    public static void checkCommand(CommandSender commandSender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Player " + playerName + " &4is not valid!"));
            return;
        }
        double money = EconomyHandler.checkCurrency(player.getUniqueId());
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs]&f " + playerName + " &2has " + money + " " + EconomySettingsConfig.getCurrencyName()));
    }

    public static void walletCommand(Player player) {
        player.sendMessage(
                ChatColorConverter.convert(
                        TranslationConfig.getEconomyWalletCommand()
                                .replace("$balance", String.valueOf(EconomyHandler.checkCurrency(player.getUniqueId())))
                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));
    }

}
