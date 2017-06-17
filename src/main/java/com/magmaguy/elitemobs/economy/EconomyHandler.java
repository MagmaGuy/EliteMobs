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

package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.config.PlayerMoneyDataConfig;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomyHandler {

    private static PlayerMoneyDataConfig playerMoneyDataConfig = new PlayerMoneyDataConfig();
    private static FileConfiguration configuration = playerMoneyDataConfig.getEconomySettingsConfig();

    public static double addCurrency (String name, double amount) {

        if (!checkUserExists(name)) {

            createUser(name);

        }

        double currentAmount = configuration.getDouble(name);
        double newAmount = currentAmount + amount;

        configuration.set(name, newAmount);
        playerMoneyDataConfig.saveCustomConfig();

        return newAmount;

    }


    public static double subtractCurrency (String name, double amount) {

        if (!checkUserExists(name)) {

            createUser(name);

        }

        double currentAmount = configuration.getDouble(name);
        double newAmount = currentAmount - amount;

        configuration.set(name, newAmount);
        playerMoneyDataConfig.saveCustomConfig();

        return newAmount;

    }

    public static void setCurrency (String name, double amount) {

        if (!checkUserExists(name)) {

            createUser(name);

        }

        configuration.set(name, amount);
        playerMoneyDataConfig.saveCustomConfig();

    }

    public static double checkCurrency (String name) {

        if (!checkUserExists(name)) {

            createUser(name);

        }

        return configuration.getDouble(name);

    }

    public static void createUser (String name){

        configuration.set(name, 0);
        playerMoneyDataConfig.saveCustomConfig();

    }

    public static boolean checkUserExists(String name){

        for (String string : configuration.getKeys(false)) {

            if (string.equals(name)) {

                return true;

            }

        }

        return false;

    }

}
