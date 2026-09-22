package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.Round;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles gambling-specific economy operations including debt management.
 * This class ensures safety-first transaction processing for gambling operations.
 */
public class GamblingEconomyHandler {

    /**
     * Maximum amount of debt a player can accumulate from gambling.
     */
    public static final double MAX_DEBT = 500.0;

    /**
     * Tracks total house earnings (money won by the house from players).
     * Positive = house profit, Negative = house loss (players winning more than losing).
     */
    @Getter
    private static volatile double houseEarnings = 0;

    /**
     * Tracks players with active, unresolved gambling sessions.
     * Added by {@link #placeBet}, removed by {@link #resolveOutcome}.
     * A marker is removed only after a known outcome settles. External provider exceptions
     * remain manual reconciliation cases, with no automatic replay.
     */
    private static final Set<UUID> unresolvedGames = ConcurrentHashMap.newKeySet();
    private static final java.util.Map<UUID, Double> pendingPayouts = new java.util.HashMap<>();
    private static final Set<UUID> uncertainTransactions = new java.util.HashSet<>();
    private static final Set<UUID> transactionsInProgress = new java.util.HashSet<>();

    private static final Object houseStateLock = new Object();
    private static final Object houseWriteLock = new Object();
    private static long houseRevision;
    private static long savedHouseRevision;
    private static volatile long saveGeneration;
    private static org.bukkit.scheduler.BukkitTask saveTask;
    private static File houseDataFile;
    private static FileConfiguration houseDataConfig;

    /**
     * Initializes the house earnings tracking system.
     * Loads saved earnings from disk.
     */
    public static void initialize() {
        if (saveTask != null) saveTask.cancel();
        saveTask = null;
        synchronized (houseWriteLock) {
            ++saveGeneration;
            if (!GamblingConfig.isGamblingEnabled()) return;
            houseDataFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "house_earnings.yml");
            YamlConfiguration loaded = new YamlConfiguration();
            try {
                loaded.load(houseDataFile);
            } catch (java.io.FileNotFoundException missing) {
                // A new installation starts at zero and creates its file on the first change.
            } catch (IOException | org.bukkit.configuration.InvalidConfigurationException failure) {
                houseDataConfig = null;
                Logger.warn("Cannot load house_earnings.yml; preserving the file and unsaved totals: " + failure);
                return;
            }
            houseDataConfig = loaded;
            synchronized (houseStateLock) {
                if (houseRevision == savedHouseRevision) houseEarnings = loaded.getDouble("houseEarnings", 0);
            }
            long generation = saveGeneration;
            saveTask = org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN,
                    () -> saveHouseEarnings(generation), 100L, 100L);
        }
    }

    public static void saveHouseEarnings() {
        saveHouseEarnings(saveGeneration);
    }

    private static void saveHouseEarnings(long generation) {
        synchronized (houseWriteLock) {
            if (generation != saveGeneration || houseDataConfig == null) return;
            final double value;
            final long revision;
            synchronized (houseStateLock) {
                if (houseRevision == savedHouseRevision) return;
                value = houseEarnings;
                revision = houseRevision;
            }
            houseDataConfig.set("houseEarnings", value);
            try {
                com.magmaguy.magmacore.config.ConfigurationEngine.fileSaverSerialized(
                        houseDataConfig.saveToString(), houseDataFile);
                synchronized (houseStateLock) {
                    savedHouseRevision = revision;
                }
            } catch (RuntimeException failure) {
                Logger.warn("Could not save house_earnings.yml; total retained for retry: " + value + "; " + failure);
            }
        }
    }

    public static void recordHouseWin(double amount) {
        if (!isPositiveAmount(amount)) return;
        synchronized (houseStateLock) {
            houseEarnings += amount;
            ++houseRevision;
        }
    }

    public static void recordHouseLoss(double amount) {
        if (!isPositiveAmount(amount)) return;
        synchronized (houseStateLock) {
            houseEarnings -= amount;
            ++houseRevision;
        }
    }

    /**
     * Gets the formatted house earnings string for display.
     *
     * @return Formatted string with color based on profit/loss
     */
    public static String getFormattedHouseEarnings() {
        if (houseEarnings >= 0) {
            return "&a+" + String.format("%.2f", houseEarnings);
        } else {
            return "&c" + String.format("%.2f", houseEarnings);
        }
    }

    /**
     * Shuts down the handler and saves data.
     */
    public static void shutdown() {
        if (saveTask != null) saveTask.cancel();
        saveTask = null;
        synchronized (houseWriteLock) {
            ++saveGeneration;
            saveHouseEarnings(saveGeneration);
        }
        if (!pendingPayouts.isEmpty() || !uncertainTransactions.isEmpty())
            Logger.warn("Unsettled gambling payments retained in memory: payouts=" + pendingPayouts
                    + "; uncertain players=" + uncertainTransactions + ". Reconcile before restarting the server.");
    }

    /**
     * Checks if a player can afford a bet, including using credit (going into debt).
     *
     * @param uuid      The player's UUID
     * @param betAmount The amount they want to bet
     * @return true if they can afford it (including credit)
     */
    public static boolean canAffordBet(UUID uuid, double betAmount) {
        if (!GamblingConfig.isGamblingEnabled() || !EconomyHandler.isReady(uuid)) return false;
        betAmount = Round.twoDecimalPlaces(betAmount);
        if (!isPositiveAmount(betAmount)) return false;
        double balance = EconomyHandler.checkCurrency(uuid);
        double currentDebt = PlayerData.getGamblingDebt(uuid);
        double availableCredit = MAX_DEBT - currentDebt;
        double spendingPower = balance + availableCredit;
        return Double.isFinite(spendingPower) && spendingPower >= betAmount;
    }

    /**
     * Gets the maximum bet a player can make based on their balance and available credit.
     *
     * @param uuid The player's UUID
     * @return The maximum amount they can bet
     */
    public static double getMaxBet(UUID uuid) {
        if (!GamblingConfig.isGamblingEnabled() || uuid == null) return 0;
        double balance = EconomyHandler.checkCurrency(uuid);
        double currentDebt = PlayerData.getGamblingDebt(uuid);
        double availableCredit = MAX_DEBT - currentDebt;
        double maxBet = balance + availableCredit;
        return Double.isFinite(maxBet) ? Math.max(0, maxBet) : 0;
    }

    /**
     * Gets the player's available credit (how much more debt they can take on).
     *
     * @param uuid The player's UUID
     * @return The available credit amount
     */
    public static double getAvailableCredit(UUID uuid) {
        if (!GamblingConfig.isGamblingEnabled() || uuid == null) return 0;
        double currentDebt = PlayerData.getGamblingDebt(uuid);
        double availableCredit = MAX_DEBT - currentDebt;
        return Double.isFinite(availableCredit) ? Math.max(0, availableCredit) : 0;
    }

    /**
     * Places a bet for a player. This should be called BEFORE any visual animations.
     * SAFETY: This method processes the bet immediately to prevent exploit-by-disconnect.
     *
     * @param uuid      The player's UUID
     * @param betAmount The amount to bet
     * @return true if the bet was successfully placed, false if they can't afford it
     */
    public static boolean placeBet(UUID uuid, double betAmount) {
        if (!org.bukkit.Bukkit.isPrimaryThread()) throw new IllegalStateException("Gambling mutation off server thread");
        if (!GamblingConfig.isGamblingEnabled() || !EconomyHandler.isReady(uuid)) return false;
        if (uncertainTransactions.contains(uuid)) {
            transactionFailure(uuid, "An earlier gambling provider result needs manual reconciliation");
            return false;
        }
        if (pendingPayouts.containsKey(uuid)) {
            // Only an explicit rejected credit can reach this retry; ambiguous calls stay blocked.
            try { resolveOutcome(uuid, pendingPayouts.get(uuid)); }
            catch (IllegalStateException rejected) { return false; }
        }
        betAmount = Round.twoDecimalPlaces(betAmount);
        if (!isPositiveAmount(betAmount) || !transactionsInProgress.add(uuid)) return false;
        try {
            double balance = EconomyHandler.checkCurrency(uuid);
            double debt = PlayerData.getGamblingDebt(uuid);
            if (!Double.isFinite(balance) || !Double.isFinite(debt) || balance + MAX_DEBT - debt < betAmount)
                return false;
            double fromBalance = Math.min(Math.max(0, balance), betAmount);
            double fromDebt = Round.twoDecimalPlaces(betAmount - fromBalance);
            try {
                if (VaultCompatibility.VAULT_ENABLED) {
                    if (fromBalance > 0 && !EconomyHandler.tryWithdraw(uuid, fromBalance)) return false;
                    if (fromDebt > 0) PlayerData.setGamblingDebt(uuid, debt + fromDebt);
                } else {
                    PlayerData.setCurrencyAndGamblingDebt(uuid, balance - fromBalance, debt + fromDebt);
                }
            } catch (RuntimeException failure) {
                uncertainTransactions.add(uuid);
                transactionFailure(uuid, "Unconfirmed gambling stake " + betAmount + "; balance debit="
                        + fromBalance + "; debt addition=" + fromDebt + "; " + failure);
                return false;
            }
            unresolvedGames.add(uuid);
            recordHouseWin(betAmount);
            return true;
        } finally {
            transactionsInProgress.remove(uuid);
        }
    }

    /**
     * Central resolution point for all gambling game outcomes.
     * MUST be called before any visual animation begins.
     * Idempotent — calling multiple times for the same session is safe (returns 0 on subsequent calls).
     * All games MUST use this instead of calling {@link #awardWinnings} directly.
     *
     * @param uuid          The player's UUID
     * @param payoutAmount  Total payout (0 for loss, betAmount for push, &gt; betAmount for win)
     * @return The actual amount awarded (0 if already resolved or loss)
     */
    public static double resolveOutcome(UUID uuid, double payoutAmount) {
        if (!org.bukkit.Bukkit.isPrimaryThread()) throw new IllegalStateException("Gambling mutation off server thread");
        if (uuid == null || !unresolvedGames.contains(uuid)) return 0;
        if (uncertainTransactions.contains(uuid))
            throw new IllegalStateException("An earlier gambling provider result needs manual reconciliation for " + uuid);
        if (!Double.isFinite(payoutAmount) || payoutAmount < 0)
            throw new IllegalArgumentException("Invalid gambling payout");
        if (!transactionsInProgress.add(uuid)) throw new IllegalStateException("Gambling transaction already in progress");
        try {
            double owed = pendingPayouts.computeIfAbsent(uuid, ignored -> Round.twoDecimalPlaces(payoutAmount));
            if (owed > 0 && !EconomyHandler.isReady(uuid))
                throw new IllegalStateException("Player data unavailable; gambling payout retained for " + uuid);
            if (owed > 0) {
                boolean accepted;
                try {
                    accepted = EconomyHandler.tryCredit(uuid, owed);
                } catch (RuntimeException failure) {
                    uncertainTransactions.add(uuid);
                    transactionFailure(uuid, "Unconfirmed gambling payout " + owed + "; " + failure);
                    throw failure;
                }
                if (!accepted) {
                    transactionFailure(uuid, "Provider rejected gambling payout " + owed + "; payout retained");
                    throw new IllegalStateException("Gambling payout rejected for " + uuid);
                }
                recordHouseLoss(owed);
            }
            pendingPayouts.remove(uuid);
            unresolvedGames.remove(uuid);
            return owed;
        } finally {
            transactionsInProgress.remove(uuid);
        }
    }

    /** Direct callers receive a failure instead of a false successful payout. */
    public static void awardWinnings(UUID uuid, double amount) {
        if (uuid == null || !isPositiveAmount(amount)) return;
        amount = Round.twoDecimalPlaces(amount);
        if (!EconomyHandler.tryCredit(uuid, amount))
            throw new IllegalStateException("Provider rejected gambling payout for " + uuid);
        recordHouseLoss(amount);
    }

    private static void transactionFailure(UUID uuid, String reason) {
        Logger.warn(reason + "; player=" + uuid);
        var player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline())
            player.sendMessage(com.magmaguy.elitemobs.config.EconomySettingsConfig.getShopTransactionFailedMessage());
    }

    /**
     * Checks if a player is currently in gambling debt.
     *
     * @param uuid The player's UUID
     * @return true if the player has gambling debt
     */
    public static boolean isInDebt(UUID uuid) {
        return PlayerData.hasGamblingDebt(uuid);
    }

    /**
     * Gets the player's current gambling debt.
     *
     * @param uuid The player's UUID
     * @return The amount of debt
     */
    public static double getDebt(UUID uuid) {
        return PlayerData.getGamblingDebt(uuid);
    }

    /**
     * Gets the player's current balance (convenience method).
     *
     * @param uuid The player's UUID
     * @return The player's balance
     */
    public static double getBalance(UUID uuid) {
        return EconomyHandler.checkCurrency(uuid);
    }

    /**
     * Formats the player's financial status as a string for display.
     *
     * @param uuid The player's UUID
     * @return A formatted string showing balance and debt
     */
    public static String getFinancialStatus(UUID uuid) {
        double balance = getBalance(uuid);
        double debt = getDebt(uuid);

        if (debt > 0) {
            return String.format("Balance: %.2f | Debt: %.2f", balance, debt);
        }
        return String.format("Balance: %.2f", balance);
    }

    /**
     * Calculates payout for a win based on bet amount and multiplier.
     *
     * @param betAmount  The original bet
     * @param multiplier The win multiplier
     * @return The total payout amount
     */
    public static double calculatePayout(double betAmount, double multiplier) {
        if (!isPositiveAmount(betAmount) || !Double.isFinite(multiplier) || multiplier < 0) return 0;
        double payout = Round.twoDecimalPlaces(betAmount * multiplier);
        return Double.isFinite(payout) && payout >= 0 ? payout : 0;
    }

    private static boolean isPositiveAmount(double amount) {
        return Double.isFinite(amount) && amount > 0;
    }
}
