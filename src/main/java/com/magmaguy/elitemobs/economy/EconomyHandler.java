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

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.PlayerMoneyDataConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomyHandler {

    private static PlayerMoneyDataConfig playerMoneyDataConfig = new PlayerMoneyDataConfig();
    private static FileConfiguration configuration = playerMoneyDataConfig.getEconomySettingsConfig();

    public static double addCurrency(UUID user, double amount) {

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        double currentAmount = configuration.getDouble(user.toString());
        double newAmount = currentAmount + amount;

        configuration.set(user.toString(), newAmount);
        playerMoneyDataConfig.saveCustomConfig();

        ConfigValues.initializeConfigValues();

        return newAmount;

    }


    public static double subtractCurrency(UUID user, double amount) {

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        double currentAmount = configuration.getDouble(user.toString());
        double newAmount = currentAmount - amount;

        configuration.set(user.toString(), newAmount);
        playerMoneyDataConfig.saveCustomConfig();

        ConfigValues.initializeConfigValues();

        return newAmount;

    }

    public static void setCurrency(UUID user, double amount) {

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        configuration.set(user.toString(), amount);
        playerMoneyDataConfig.saveCustomConfig();

        ConfigValues.initializeConfigValues();

    }

    public static double checkCurrency(UUID user) {

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        return configuration.getDouble(user.toString());

    }

    public static void createUser(UUID user) {

        configuration.set(user.toString(), 0);
        playerMoneyDataConfig.saveCustomConfig();

        ConfigValues.initializeConfigValues();

    }

    public static boolean checkUserExists(String name) {

        for (String string : configuration.getKeys(false)) {

            if (string.equals(name)) {

                return true;

            }

        }

        return false;

    }

}
