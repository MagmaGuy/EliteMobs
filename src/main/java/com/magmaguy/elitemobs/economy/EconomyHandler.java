package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.utils.Round;

import java.util.UUID;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomyHandler {

    public static void addCurrency(UUID user, double amount) {
        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.addVaultCurrency(user, amount);
            return;
        }
        PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) + amount));
    }


    public static void subtractCurrency(UUID user, double amount) {
        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.subtractCurrency(user, amount);
            return;
        }
        PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) - amount));
    }

    public static void setCurrency(UUID user, double amount) {

        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.setCurrency(user, amount);
            return;
        }

        PlayerData.setCurrency(user, Round.twoDecimalPlaces(amount));

    }

    public static double checkCurrency(UUID user) {
        if (VaultCompatibility.VAULT_ENABLED)
            return VaultCompatibility.checkCurrency(user);

        return PlayerData.getCurrency(user);
    }

    public static double checkCurrency(UUID user, boolean databaseAccess) {
        if (VaultCompatibility.VAULT_ENABLED)
            return VaultCompatibility.checkCurrency(user);

        return PlayerData.getCurrency(user, databaseAccess);
    }

}
