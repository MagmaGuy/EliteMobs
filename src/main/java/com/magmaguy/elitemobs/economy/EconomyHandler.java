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

import com.magmaguy.elitemobs.config.PlayerMoneyData;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomyHandler {

    private static PlayerMoneyData playerMoneyData = new PlayerMoneyData();

    public static void addCurrency(UUID user, double amount) {

        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.addVaultCurrency(user, amount);
            return;
        }

        if (!checkUserExists(user)) createUser(user);

        PlayerData.playerCurrency.put(user, roundDecimals(checkCurrency(user) + amount));
        PlayerData.playerCurrencyChanged = true;

    }


    public static void subtractCurrency(UUID user, double amount) {

        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.subtractCurrency(user, amount);
            return;
        }

        if (!checkUserExists(user)) createUser(user);

        PlayerData.playerCurrency.put(user, roundDecimals(checkCurrency(user) - amount));
        PlayerData.playerCurrencyChanged = true;

    }

    public static void setCurrency(UUID user, double amount) {

        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.setCurrency(user, amount);
            return;
        }

        if (!checkUserExists(user)) createUser(user);

        PlayerData.playerCurrency.put(user, roundDecimals(amount));
        PlayerData.playerCurrencyChanged = true;

    }

    public static double checkCurrency(UUID user) {

        if (VaultCompatibility.VAULT_ENABLED) {
            return VaultCompatibility.checkCurrency(user);
        }

        if (!checkUserExists(user))
            createUser(user);

        return PlayerData.playerCurrency.get(user);

    }

    private static void createUser(UUID uuid) {

        PlayerData.playerCurrency.put(uuid, 0.0);
        PlayerData.playerCurrencyChanged = true;

    }

    private static boolean checkUserExists(UUID uuid) {

        checkUserIsCached(uuid);
        return PlayerData.playerCurrency.containsKey(uuid);

    }

    private static void checkUserIsCached(UUID uuid) {

        boolean playerIsOnline = false;

        for (Player player : Bukkit.getOnlinePlayers())
            if(player.getUniqueId().equals(uuid)){
                playerIsOnline = true;
                break;
            }


        if (!PlayerData.playerDisplayName.containsKey(uuid) || playerIsOnline &&
                !PlayerData.playerDisplayName.get(uuid).equals(Bukkit.getPlayer(uuid).getDisplayName())) {

            PlayerData.playerDisplayName.put(uuid, Bukkit.getPlayer(uuid).getDisplayName());
            PlayerData.playerCacheChanged = true;

        }

    }

    private static double roundDecimals(double rawValue) {

        return (double) Math.round(rawValue * 100) / 100;

    }

}
