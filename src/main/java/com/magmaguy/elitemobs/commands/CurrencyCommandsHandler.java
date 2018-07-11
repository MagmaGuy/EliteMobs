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

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class CurrencyCommandsHandler {

    public static void payCommand(String playerName, double amount) {

        if (amount > 0) {

            EconomyHandler.addCurrency(UUIDFilter.guessUUI(playerName), amount);

        }

    }

    public static void addCommand(String playerName, double amount) {

        EconomyHandler.addCurrency(UUIDFilter.guessUUI(playerName), amount);

    }

    public static void subtractCommand(String playerName, double amount) {

        EconomyHandler.subtractCurrency(UUIDFilter.guessUUI(playerName), amount);

    }

    public static void setCommand(String playerName, double amount) {

        EconomyHandler.setCurrency(UUIDFilter.guessUUI(playerName), amount);

    }

    public static Double checkCommand(String playerName) {

        return EconomyHandler.checkCurrency(UUIDFilter.guessUUI(playerName));

    }

    public static Double walletCommand(String playerName) {

        return EconomyHandler.checkCurrency(UUIDFilter.guessUUI(playerName));

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
