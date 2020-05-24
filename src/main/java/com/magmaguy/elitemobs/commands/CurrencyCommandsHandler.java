package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class CurrencyCommandsHandler {

    public static void payCommand(String playerName, double amount) {
        if (amount > 0)
            EconomyHandler.addCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);
    }

    public static void payCommand(Player commandSender, String[] args) {

        try {

            if (Double.parseDouble(args[2]) > 0 && Integer.parseInt(args[2]) <= EconomyHandler.checkCurrency(Bukkit.getPlayer(commandSender.getName()).getUniqueId())) {

                payCommand(args[1], Integer.parseInt(args[2]));
                subtractCommand(commandSender.getName(), Integer.parseInt(args[2]));

                commandSender.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_PAY_MESSAGE)
                                        .replace("$amount_sent", args[2])
                                        .replace("$currency_name", EconomySettingsConfig.currencyName)
                                        .replace("$receiver", args[1])));
                commandSender.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_CURRENCY_LEFT_MESSAGE)
                                        .replace("$amount_left", String.valueOf(EconomyHandler.checkCurrency(Bukkit.getPlayer(commandSender.getName()).getUniqueId())))
                                        .replace("$currency_name", EconomySettingsConfig.currencyName)));

                Player recipient = getServer().getPlayer(args[1]);
                recipient.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_PAYMENT_RECIEVED_MESSAGE)
                                        .replace("$amount_sent", args[2])
                                        .replace("$sender", commandSender.getDisplayName())
                                        .replace("$currency_name", EconomySettingsConfig.currencyName)));

            } else if (Double.parseDouble(args[2]) < 0)
                commandSender.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_PAYMENT_RECIEVED_MESSAGE)));
            else
                commandSender.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_PAYMENT_INSUFICIENT_CURRENCY)
                                        .replace("$currency_name", EconomySettingsConfig.currencyName)));


        } catch (Exception e) {

            commandSender.sendMessage(
                    ChatColorConverter.convert(
                            ConfigValues.translationConfig
                                    .getString(TranslationConfig.ECONOMY_INVALID_PAY_COMMAND_SYNTAX)));

        }

    }

    public static void addCommand(String playerName, double amount) {

        EconomyHandler.addCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);

    }

    public static void addCommand(CommandSender commandSender, String[] args) {

        try {

            addCommand(args[1], Integer.parseInt(args[2]));

            commandSender.sendMessage("You have added " + args[2] + " to " + args[1]);
            commandSender.sendMessage("They now have " + EconomyHandler.checkCurrency(Bukkit.getPlayer(args[2]).getUniqueId()));

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em add [playerName] [amount]");

        }

    }

    public static void addAllCommand(CommandSender commandSender, String[] args) {

        try {

            for (Player player : Bukkit.getOnlinePlayers())
                addCommand(player.getName(), Integer.parseInt(args[1]));

            commandSender.sendMessage("You have added " + args[2] + " to all online players.");

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em addall [amount]");

        }

    }

    public static void subtractCommand(String playerName, double amount) {

        EconomyHandler.subtractCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);

    }

    public static void subtractCommand(CommandSender commandSender, String[] args) {

        try {

            subtractCommand(args[1], Integer.parseInt(args[2]));

            commandSender.sendMessage("You have subtracted " + args[2] + " from " + args[1]);
            commandSender.sendMessage("They now have " + EconomyHandler.checkCurrency(Bukkit.getPlayer(commandSender.getName()).getUniqueId()));

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em subtract [playerName] [amount]");

        }

    }

    public static void setCommand(String playerName, double amount) {

        EconomyHandler.setCurrency(Bukkit.getPlayer(playerName).getUniqueId(), amount);

    }

    public static void setCommand(CommandSender commandSender, String[] args) {

        try {

            CurrencyCommandsHandler.setCommand(args[1], Integer.parseInt(args[2]));
            commandSender.sendMessage("You set " + args[1] + "'s " + EconomySettingsConfig.currencyName + " to " + args[2]);

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em set [playerName] [amount]");

        }

    }

    public static Double checkCommand(String playerName) {

        return EconomyHandler.checkCurrency(Bukkit.getPlayer(playerName).getUniqueId());

    }

    public static void checkCommand(CommandSender commandSender, String[] args) {

        try {
            Double money = CurrencyCommandsHandler.checkCommand(args[1]);
            commandSender.sendMessage(args[1] + " has " + money + " " + EconomySettingsConfig.currencyName);
        } catch (Exception e) {
            commandSender.sendMessage("Input not valid. Command format: /em check [playerName]");
        }

    }

    public static Double walletCommand(String playerName) {

        return EconomyHandler.checkCurrency(Bukkit.getPlayer(playerName).getUniqueId());

    }

    public static void walletCommand(CommandSender commandSender, String[] args) {

        commandSender.sendMessage(
                ChatColorConverter.convert(
                        ConfigValues.translationConfig
                                .getString(TranslationConfig.ECONOMY_WALLET_COMMAND)
                                .replace("$balance", String.valueOf(CurrencyCommandsHandler.walletCommand(commandSender.getName())))
                                .replace("$currency_name", EconomySettingsConfig.currencyName)));

    }

    //public static void coinTop(CommandSender commandSender) {

    //    ArrayList<UUID> arrayList = new ArrayList();

    //    for (UUID uuid : PlayerData.playerCurrency.keySet()) {

    //        if (arrayList.size() == 0) {

    //            arrayList.add(uuid);

    //        } else {

    //            int index = arrayList.size();

    //            for (UUID inArrayUUID : arrayList) {

    //                double arrayEntryValue = PlayerData.playerCurrency.get(inArrayUUID);
    //                double newValue = PlayerData.playerCurrency.get(uuid);

    //                if (newValue > arrayEntryValue) {

    //                    index = arrayList.indexOf(inArrayUUID);
    //                    break;

    //                }

    //            }

    //            arrayList.add(index, uuid);

    //        }

    //    }

    //    commandSender.sendMessage(ChatColor.RED + "[EliteMobs] " + ChatColor.DARK_GREEN + EconomySettingsConfig.currencyName + " High Score:");

    //    int iterationAmount = 10;

    //    if (arrayList.size() < 10) {

    //        iterationAmount = arrayList.size();

    //    }

    //    for (int i = 0; i < iterationAmount; i++) {

    //        String name = PlayerData.playerDisplayName.get(arrayList.get(i));
    //        double amount = PlayerData.playerCurrency.get(arrayList.get(i));

    //        int place = i + 1;

    //        commandSender.sendMessage(ChatColor.GREEN + "#" + place + " " + ChatColor.WHITE + name + " with " +
    //                ChatColor.DARK_GREEN + amount + " " + ChatColor.GREEN + EconomySettingsConfig.currencyName);

    //    }

    //}

}
