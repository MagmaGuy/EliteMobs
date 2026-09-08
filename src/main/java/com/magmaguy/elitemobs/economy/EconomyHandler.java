package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Round;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomyHandler {

    public static void addCurrency(UUID user, double amount) {
        double debt = PlayerData.getGamblingDebt(user);
        if (debt > 0) {
            double debtPayment = Math.min(debt, amount);
            PlayerData.reduceGamblingDebt(user, debtPayment);
            amount -= debtPayment;
            double remainingDebt = debt - debtPayment;
            Player player = Bukkit.getPlayer(user);
            if (player != null && player.isOnline()) {
                if (remainingDebt > 0)
                    player.sendMessage(ChatColorConverter.convert(
                            GamblingConfig.getDebtAutoCollectedMessage()
                                    .replace("$amount", String.format("%.0f", debtPayment))
                                    .replace("$remaining", String.format("%.0f", remainingDebt))));
                else
                    player.sendMessage(ChatColorConverter.convert(
                            GamblingConfig.getDebtAutoClearedMessage()));
            }
            if (amount <= 0) return;
        }
        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.addVaultCurrency(user, amount);
            return;
        }
        // Storage layer is now cent-precise (PlayerData stores long cents); Round.twoDecimalPlaces is belt-and-suspenders.
        PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) + amount));
    }


    /** Checked purchase path. Call on the server thread with loaded player data. */
    public static boolean tryWithdraw(UUID user, double amount) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Economy purchase off server thread");
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid purchase price");
        if (VaultCompatibility.VAULT_ENABLED) {
            var economy = VaultCompatibility.getEconomy();
            return economy != null && economy.withdrawPlayer(Bukkit.getOfflinePlayer(user), amount).transactionSuccess();
        }
        double balance = checkCurrency(user);
        if (balance < amount) return false;
        PlayerData.setCurrency(user, Round.twoDecimalPlaces(balance - amount));
        return true;
    }

    /** Returns an aborted purchase without diverting the refund into gambling debt. */
    public static boolean refundPayment(UUID user, double amount) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Economy refund off server thread");
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid refund");
        if (VaultCompatibility.VAULT_ENABLED) {
            var economy = VaultCompatibility.getEconomy();
            return economy != null && economy.depositPlayer(Bukkit.getOfflinePlayer(user), amount).transactionSuccess();
        }
        PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) + amount));
        return true;
    }

    public static void subtractCurrency(UUID user, double amount) {
        if (VaultCompatibility.VAULT_ENABLED) {
            VaultCompatibility.subtractCurrency(user, amount);
            return;
        }
        // Storage layer is now cent-precise (PlayerData stores long cents); Round.twoDecimalPlaces is belt-and-suspenders.
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

    /**
     * Formats a currency amount to 2 decimal places for player-facing display.
     */
    public static String formatCurrency(double amount) {
        return String.format("%.2f", amount);
    }

}
