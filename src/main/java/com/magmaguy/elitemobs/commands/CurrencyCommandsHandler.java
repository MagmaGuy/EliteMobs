/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class CurrencyCommandsHandler {

    public static void payCommand(String playerName, double amount) {

        if (amount > 0)
            EconomyHandler.addCurrency(UUIDFilter.guessUUI(playerName), amount);


    }

    public static void payCommand(Player commandSender, String[] args) {

        try {

            if (Double.parseDouble(args[2]) > 0 && Integer.parseInt(args[2]) <= EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName()))) {

                payCommand(args[1], Integer.parseInt(args[2]));
                subtractCommand(commandSender.getName(), Integer.parseInt(args[2]));

                commandSender.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_PAY_MESSAGE)
                                        .replace("$amount_sent", args[2])
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))
                                        .replace("$receiver", args[1])));
                commandSender.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_CURRENCY_LEFT_MESSAGE)
                                        .replace("$amount_left", String.valueOf(EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName()))))
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

                Player recipient = getServer().getPlayer(args[1]);
                recipient.sendMessage(
                        ChatColorConverter.convert(
                                ConfigValues.translationConfig
                                        .getString(TranslationConfig.ECONOMY_PAYMENT_RECIEVED_MESSAGE)
                                        .replace("$amount_sent", args[2])
                                        .replace("$sender", commandSender.getDisplayName())
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

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
                                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));


        } catch (Exception e) {

            commandSender.sendMessage(
                    ChatColorConverter.convert(
                            ConfigValues.translationConfig
                                    .getString(TranslationConfig.ECONOMY_INVALID_PAY_COMMAND_SYNTAX)));

        }

    }

    public static void addCommand(String playerName, double amount) {

        EconomyHandler.addCurrency(UUIDFilter.guessUUI(playerName), amount);

    }

    public static void addCommand(CommandSender commandSender, String[] args) {

        try {

            addCommand(args[1], Integer.parseInt(args[2]));

            commandSender.sendMessage("You have added " + args[2] + " to " + args[1]);
            commandSender.sendMessage("They now have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName())));

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em add [playerName] [amount]");

        }

    }

    public static void subtractCommand(String playerName, double amount) {

        EconomyHandler.subtractCurrency(UUIDFilter.guessUUI(playerName), amount);

    }

    public static void subtractCommand(CommandSender commandSender, String[] args) {

        try {

            subtractCommand(args[1], Integer.parseInt(args[2]));

            commandSender.sendMessage("You have subtracted " + args[2] + " from " + args[1]);
            commandSender.sendMessage("They now have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName())));

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em subtract [playerName] [amount]");

        }

    }

    public static void setCommand(String playerName, double amount) {

        EconomyHandler.setCurrency(UUIDFilter.guessUUI(playerName), amount);

    }

    public static void setCommand(CommandSender commandSender, String[] args) {

        try {

            CurrencyCommandsHandler.setCommand(args[1], Integer.parseInt(args[2]));
            commandSender.sendMessage("You set " + args[1] + "'s " + ConfigValues.economyConfig.get("Currency name") + " to " + args[2]);

        } catch (Exception e) {

            commandSender.sendMessage("Input not valid. Command format: /em set [playerName] [amount]");

        }

    }

    public static Double checkCommand(String playerName) {

        return EconomyHandler.checkCurrency(UUIDFilter.guessUUI(playerName));

    }

    public static void checkCommand(CommandSender commandSender, String[] args) {

        try {
            Double money = CurrencyCommandsHandler.checkCommand(args[1]);
            commandSender.sendMessage(args[1] + " has " + money + " " + ConfigValues.economyConfig.get("Currency name"));
        } catch (Exception e) {
            commandSender.sendMessage("Input not valid. Command format: /em check [playerName]");
        }

    }

    public static Double walletCommand(String playerName) {

        return EconomyHandler.checkCurrency(UUIDFilter.guessUUI(playerName));

    }

    public static void walletCommand(CommandSender commandSender, String[] args) {

        commandSender.sendMessage(
                ChatColorConverter.convert(
                        ConfigValues.translationConfig
                                .getString(TranslationConfig.ECONOMY_WALLET_COMMAND)
                                .replace("$balance", String.valueOf(CurrencyCommandsHandler.walletCommand(commandSender.getName())))
                                .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))));

    }

    public static void coinTop(CommandSender commandSender) {

        ArrayList<UUID> arrayList = new ArrayList();

        for (UUID uuid : PlayerData.playerCurrency.keySet()) {

            if (arrayList.size() == 0) {

                arrayList.add(uuid);

            } else {

                int index = arrayList.size();

                for (UUID inArrayUUID : arrayList) {

                    double arrayEntryValue = PlayerData.playerCurrency.get(inArrayUUID);
                    double newValue = PlayerData.playerCurrency.get(uuid);

                    if (newValue > arrayEntryValue) {

                        index = arrayList.indexOf(inArrayUUID);
                        break;

                    }

                }

                arrayList.add(index, uuid);

            }

        }

        commandSender.sendMessage(ChatColor.RED + "[EliteMobs] " + ChatColor.DARK_GREEN + ConfigValues.economyConfig.get(EconomySettingsConfig.CURRENCY_NAME) + " High Score:");

        int iterationAmount = 10;

        if (arrayList.size() < 10) {

            iterationAmount = arrayList.size();

        }

        for (int i = 0; i < iterationAmount; i++) {

            String name = PlayerData.playerDisplayName.get(arrayList.get(i));
            double amount = PlayerData.playerCurrency.get(arrayList.get(i));

            int place = i + 1;

            commandSender.sendMessage(ChatColor.GREEN + "#" + place + " " + ChatColor.WHITE + name + " with " +
                    ChatColor.DARK_GREEN + amount + " " + ChatColor.GREEN + ConfigValues.economyConfig.get(EconomySettingsConfig.CURRENCY_NAME));

        }

    }

}
