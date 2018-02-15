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
import com.magmaguy.elitemobs.config.CustomConfigLoader;
import com.magmaguy.elitemobs.config.PlayerMoneyDataConfig;
import org.bukkit.configuration.Configuration;

import java.util.UUID;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomyHandler {

    private static PlayerMoneyDataConfig playerMoneyDataConfig = new PlayerMoneyDataConfig();

    public static double addCurrency(UUID user, double amount) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration configuration = customConfigLoader.getCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        double currentAmount = configuration.getDouble(user.toString());
        double newAmount = currentAmount + amount;

        configuration.set(user.toString(), newAmount);
        customConfigLoader.saveCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        return newAmount;

    }


    public static double subtractCurrency(UUID user, double amount) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration configuration = customConfigLoader.getCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        double currentAmount = configuration.getDouble(user.toString());
        double newAmount = currentAmount - amount;

        configuration.set(user.toString(), newAmount);
        customConfigLoader.saveCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        ConfigValues.initializeConfigValues();

        return newAmount;

    }

    public static void setCurrency(UUID user, double amount) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration configuration = customConfigLoader.getCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        configuration.set(user.toString(), amount);
        customConfigLoader.saveCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        ConfigValues.initializeConfigValues();

    }

    public static double checkCurrency(UUID user) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration configuration = customConfigLoader.getCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        if (!checkUserExists(user.toString())) {

            createUser(user);

        }

        return configuration.getDouble(user.toString());

    }

    public static void createUser(UUID user) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration configuration = customConfigLoader.getCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        configuration.set(user.toString(), 0);
        customConfigLoader.saveCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        ConfigValues.initializeConfigValues();

    }

    public static boolean checkUserExists(String name) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration configuration = customConfigLoader.getCustomConfig(PlayerMoneyDataConfig.CONFIG_NAME);

        for (String string : configuration.getKeys(false)) {

            if (string.equals(name)) {

                return true;

            }

        }

        return false;

    }

}
