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

import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;

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

}
