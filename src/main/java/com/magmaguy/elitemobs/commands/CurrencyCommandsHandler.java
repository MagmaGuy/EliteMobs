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

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class CurrencyCommandsHandler {

    public static void payCommand(String playerName, int amount) {

        EconomyHandler.addCurrency(playerName, amount);

    }

    public static void subtractCommand(String playerName, int amount) {

        EconomyHandler.subtractCurrency(playerName, amount);

    }

    public static void setCommand(String playerName, int amount) {

        EconomyHandler.setCurrency(playerName, amount);

    }

    public static Double checkCommand(String playerName) {

        return EconomyHandler.checkCurrency(playerName);

    }

    public static Double walletCommand(String playerName) {

        return EconomyHandler.checkCurrency(playerName);

    }

}
