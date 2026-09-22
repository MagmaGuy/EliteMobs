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

    private static final java.util.Set<UUID> activeMutations = new java.util.HashSet<>();

    public static boolean isReady(UUID user) {
        return user != null && PlayerData.isDataLoaded(user);
    }

    private static void requireReady(UUID user) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Economy mutation off server thread");
        if (!isReady(user)) throw new IllegalStateException("Player economy data is not loaded for " + user);
    }

    public static void addCurrency(UUID user, double amount) {
        if (!tryCredit(user, amount)) throw new IllegalStateException("Economy provider rejected credit for " + user);
    }

    /** Checked ordinary proceeds, including debt collection. False commits neither proceeds nor debt. */
    public static boolean tryCredit(UUID user, double amount) {
        requireReady(user);
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid credit");
        amount = Round.twoDecimalPlaces(amount);
        if (amount == 0) return true;
        if (!activeMutations.add(user)) return false;
        try {
            double debt = PlayerData.getGamblingDebt(user);
            double collection = Math.min(debt, amount);
            double proceeds = Round.twoDecimalPlaces(amount - collection);
            double remainingDebt = Round.twoDecimalPlaces(debt - collection);
            if (VaultCompatibility.VAULT_ENABLED) {
                if (proceeds > 0) {
                    var economy = VaultCompatibility.getEconomy();
                    if (economy == null || !economy.depositPlayer(Bukkit.getOfflinePlayer(user), proceeds).transactionSuccess())
                        return false;
                }
                // Commit local debt only after the external provider accepts the remaining proceeds.
                if (collection > 0) PlayerData.setGamblingDebt(user, remainingDebt);
            } else {
                PlayerData.setCurrencyAndGamblingDebt(user,
                        Round.twoDecimalPlaces(checkCurrency(user) + proceeds), remainingDebt);
            }
            if (collection > 0) notifyDebtCollection(user, collection, remainingDebt);
            return true;
        } finally {
            activeMutations.remove(user);
        }
    }

    private static void notifyDebtCollection(UUID user, double amount, double remainingDebt) {
        try {
            Player player = Bukkit.getPlayer(user);
            if (player == null || !player.isOnline()) return;
            player.sendMessage(ChatColorConverter.convert(remainingDebt > 0
                    ? GamblingConfig.getDebtAutoCollectedMessage().replace("$amount", String.format("%.0f", amount))
                            .replace("$remaining", String.format("%.0f", remainingDebt))
                    : GamblingConfig.getDebtAutoClearedMessage()));
        } catch (RuntimeException failure) {
            com.magmaguy.magmacore.util.Logger.warn("Could not display accepted debt collection for " + user + ": " + failure);
        }
    }

    /** Checked purchase path. Call on the server thread with loaded player data. */
    public static boolean tryWithdraw(UUID user, double amount) {
        requireReady(user);
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid purchase price");
        if (!activeMutations.add(user)) return false;
        try {
            if (VaultCompatibility.VAULT_ENABLED) {
                var economy = VaultCompatibility.getEconomy();
                return economy != null && economy.withdrawPlayer(Bukkit.getOfflinePlayer(user), amount).transactionSuccess();
            }
            double balance = checkCurrency(user);
            if (balance < amount) return false;
            PlayerData.setCurrency(user, Round.twoDecimalPlaces(balance - amount));
            return true;
        } finally {
            activeMutations.remove(user);
        }
    }

    /** Returns an aborted purchase without diverting the refund into gambling debt. */
    public static boolean refundPayment(UUID user, double amount) {
        requireReady(user);
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid refund");
        if (!activeMutations.add(user)) return false;
        try {
            if (VaultCompatibility.VAULT_ENABLED) {
                var economy = VaultCompatibility.getEconomy();
                return economy != null && economy.depositPlayer(Bukkit.getOfflinePlayer(user), amount).transactionSuccess();
            }
            PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) + amount));
            return true;
        } finally {
            activeMutations.remove(user);
        }
    }

    public static void subtractCurrency(UUID user, double amount) {
        requireReady(user);
        if (activeMutations.contains(user)) throw new IllegalStateException("Economy mutation already in progress");
        if (VaultCompatibility.VAULT_ENABLED) {
            if (!tryWithdraw(user, amount)) throw new IllegalStateException("Economy provider rejected withdrawal for " + user);
            return;
        }
        // Storage layer is now cent-precise (PlayerData stores long cents); Round.twoDecimalPlaces is belt-and-suspenders.
        PlayerData.setCurrency(user, Round.twoDecimalPlaces(checkCurrency(user) - amount));
    }

    public static void setCurrency(UUID user, double amount) {
        requireReady(user);
        if (activeMutations.contains(user)) throw new IllegalStateException("Economy mutation already in progress");

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
