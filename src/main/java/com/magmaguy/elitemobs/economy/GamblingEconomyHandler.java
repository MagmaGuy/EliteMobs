package com.magmaguy.elitemobs.economy;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.Round;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
    private static double houseEarnings = 0;

    private static File houseDataFile;
    private static FileConfiguration houseDataConfig;

    /**
     * Initializes the house earnings tracking system.
     * Loads saved earnings from disk.
     */
    public static void initialize() {
        houseDataFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "house_earnings.yml");
        if (!houseDataFile.exists()) {
            try {
                houseDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        houseDataConfig = YamlConfiguration.loadConfiguration(houseDataFile);
        houseEarnings = houseDataConfig.getDouble("houseEarnings", 0);
    }

    /**
     * Saves the house earnings to disk.
     */
    public static void saveHouseEarnings() {
        if (houseDataConfig == null) return;
        houseDataConfig.set("houseEarnings", houseEarnings);
        try {
            houseDataConfig.save(houseDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Records earnings for the house (when a player loses).
     *
     * @param amount The amount the house won
     */
    public static void recordHouseWin(double amount) {
        houseEarnings += amount;
        saveHouseEarnings();
    }

    /**
     * Records a loss for the house (when a player wins).
     *
     * @param amount The amount the house lost
     */
    public static void recordHouseLoss(double amount) {
        houseEarnings -= amount;
        saveHouseEarnings();
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
        saveHouseEarnings();
    }

    /**
     * Checks if a player can afford a bet, including using credit (going into debt).
     *
     * @param uuid      The player's UUID
     * @param betAmount The amount they want to bet
     * @return true if they can afford it (including credit)
     */
    public static boolean canAffordBet(UUID uuid, double betAmount) {
        double balance = EconomyHandler.checkCurrency(uuid);
        double currentDebt = PlayerData.getGamblingDebt(uuid);
        double availableCredit = MAX_DEBT - currentDebt;
        return (balance + availableCredit) >= betAmount;
    }

    /**
     * Gets the maximum bet a player can make based on their balance and available credit.
     *
     * @param uuid The player's UUID
     * @return The maximum amount they can bet
     */
    public static double getMaxBet(UUID uuid) {
        double balance = EconomyHandler.checkCurrency(uuid);
        double currentDebt = PlayerData.getGamblingDebt(uuid);
        double availableCredit = MAX_DEBT - currentDebt;
        return balance + availableCredit;
    }

    /**
     * Gets the player's available credit (how much more debt they can take on).
     *
     * @param uuid The player's UUID
     * @return The available credit amount
     */
    public static double getAvailableCredit(UUID uuid) {
        double currentDebt = PlayerData.getGamblingDebt(uuid);
        return Math.max(0, MAX_DEBT - currentDebt);
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
        if (!canAffordBet(uuid, betAmount)) {
            return false;
        }

        betAmount = Round.twoDecimalPlaces(betAmount);
        double balance = EconomyHandler.checkCurrency(uuid);

        if (balance >= betAmount) {
            // Player has enough balance - deduct from balance
            EconomyHandler.subtractCurrency(uuid, betAmount);
        } else {
            // Player needs to go into debt
            double fromBalance = balance;
            double fromDebt = betAmount - balance;

            // Take all remaining balance
            if (fromBalance > 0) {
                EconomyHandler.setCurrency(uuid, 0);
            }

            // Add the rest to debt
            PlayerData.addGamblingDebt(uuid, fromDebt);
        }

        // House takes the bet
        recordHouseWin(betAmount);

        return true;
    }

    /**
     * Awards winnings to a player. Winnings are first applied to pay off debt.
     * SAFETY: This should be called BEFORE any visual animations.
     *
     * @param uuid   The player's UUID
     * @param amount The amount won (including original bet if applicable)
     */
    public static void awardWinnings(UUID uuid, double amount) {
        amount = Round.twoDecimalPlaces(amount);

        // House pays out the winnings
        recordHouseLoss(amount);

        double currentDebt = PlayerData.getGamblingDebt(uuid);

        if (currentDebt > 0) {
            // Pay off debt first
            double debtPayment = Math.min(currentDebt, amount);
            PlayerData.reduceGamblingDebt(uuid, debtPayment);
            amount -= debtPayment;
        }

        // Add remaining amount to balance
        if (amount > 0) {
            EconomyHandler.addCurrency(uuid, amount);
        }
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
        return Round.twoDecimalPlaces(betAmount * multiplier);
    }
}
