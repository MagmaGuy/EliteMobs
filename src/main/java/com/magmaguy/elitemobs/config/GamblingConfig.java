package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

/**
 * Configuration for the gambling system including minigames, debt, and the Debt Collector boss.
 */
public class GamblingConfig extends ConfigurationFile {

    // General settings
    @Getter
    private static boolean gamblingEnabled;
    @Getter
    private static double maxDebt;
    @Getter
    private static int minBet;
    @Getter
    private static int maxBet;

    // Debt Collector settings
    @Getter
    private static double debtCollectorSpawnChance;
    @Getter
    private static int debtCollectorCheckIntervalMinutes;
    @Getter
    private static int debtCollectorTimeoutSeconds;
    @Getter
    private static double debtCollectorHealthMultiplier;
    @Getter
    private static double debtCollectorDamageMultiplier;
    @Getter
    private static double debtReductionOnPlayerDeath;

    // Payout multipliers
    @Getter
    private static double blackjackPayoutNormal;
    @Getter
    private static double blackjackPayoutBlackjack;
    @Getter
    private static double coinFlipPayout;
    @Getter
    private static double slotsPayoutCherry;
    @Getter
    private static double slotsPayoutLemon;
    @Getter
    private static double slotsPayoutBell;
    @Getter
    private static double slotsPayoutBar;
    @Getter
    private static double slotsPayoutSeven;
    @Getter
    private static double slotsPayoutTwoMatch;
    @Getter
    private static double higherLowerMultiplier;

    // Messages
    @Getter
    private static String insufficientFundsMessage;
    @Getter
    private static String maxDebtReachedMessage;
    @Getter
    private static String winMessage;
    @Getter
    private static String loseMessage;
    @Getter
    private static String debtWarningMessage;
    @Getter
    private static String betPlacedMessage;
    @Getter
    private static String balanceDisplayMessage;

    // Betting menu
    @Getter
    private static String bettingMenuTitle;
    @Getter
    private static String playButtonText;
    @Getter
    private static String cancelButtonText;
    @Getter
    private static String maxBetButtonText;

    // Game-specific menu titles
    @Getter
    private static String blackjackMenuTitle;
    @Getter
    private static String coinFlipMenuTitle;
    @Getter
    private static String slotsMenuTitle;
    @Getter
    private static String higherLowerMenuTitle;

    public GamblingConfig() {
        super("GamblingSettings.yml");
    }

    @Override
    public void initializeValues() {
        // General settings
        gamblingEnabled = ConfigurationEngine.setBoolean(
                List.of("Enables or disables the gambling system entirely.",
                        "When disabled, gambling NPCs will not work and the Debt Collector will not spawn."),
                fileConfiguration, "gamblingEnabled", true);

        maxDebt = ConfigurationEngine.setDouble(
                List.of("Maximum amount of debt a player can accumulate from gambling.",
                        "Players cannot place bets that would put them over this limit."),
                fileConfiguration, "maxDebt", 500.0);

        minBet = ConfigurationEngine.setInt(
                List.of("Minimum bet amount for all gambling games."),
                fileConfiguration, "minBet", 10);

        maxBet = ConfigurationEngine.setInt(
                List.of("Maximum bet amount for all gambling games."),
                fileConfiguration, "maxBet", 1000);

        // Debt Collector settings
        debtCollectorSpawnChance = ConfigurationEngine.setDouble(
                List.of("Chance (0.0 to 1.0) that the Debt Collector will spawn during each check.",
                        "0.5 = 50% chance each check."),
                fileConfiguration, "debtCollector.spawnChance", 0.5);

        debtCollectorCheckIntervalMinutes = ConfigurationEngine.setInt(
                List.of("How often (in minutes) to check if the Debt Collector should spawn for players in debt."),
                fileConfiguration, "debtCollector.checkIntervalMinutes", 60);

        debtCollectorTimeoutSeconds = ConfigurationEngine.setInt(
                List.of("How long (in seconds) before the Debt Collector despawns if not killed."),
                fileConfiguration, "debtCollector.timeoutSeconds", 600);

        debtCollectorHealthMultiplier = ConfigurationEngine.setDouble(
                List.of("Health multiplier for the Debt Collector boss."),
                fileConfiguration, "debtCollector.healthMultiplier", 5.0);

        debtCollectorDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Damage multiplier for the Debt Collector boss."),
                fileConfiguration, "debtCollector.damageMultiplier", 1.0);

        debtReductionOnPlayerDeath = ConfigurationEngine.setDouble(
                List.of("Amount of debt reduced when the Debt Collector kills a player.",
                        "The player 'paid' with their life."),
                fileConfiguration, "debtCollector.debtReductionOnPlayerDeath", 50.0);

        // Payout multipliers
        blackjackPayoutNormal = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for a normal blackjack win (not a blackjack)."),
                fileConfiguration, "payouts.blackjack.normal", 2.0);

        blackjackPayoutBlackjack = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for getting a blackjack (21 with first two cards)."),
                fileConfiguration, "payouts.blackjack.blackjack", 2.5);

        coinFlipPayout = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for winning a coin flip.",
                        "Set below 2.0 for house edge (1.9 = 5% house edge)."),
                fileConfiguration, "payouts.coinFlip", 1.9);

        slotsPayoutCherry = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for matching 3 cherries on the slot machine."),
                fileConfiguration, "payouts.slots.cherry", 2.0);

        slotsPayoutLemon = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for matching 3 lemons on the slot machine."),
                fileConfiguration, "payouts.slots.lemon", 3.0);

        slotsPayoutBell = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for matching 3 bells on the slot machine."),
                fileConfiguration, "payouts.slots.bell", 5.0);

        slotsPayoutBar = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for matching 3 bars on the slot machine."),
                fileConfiguration, "payouts.slots.bar", 10.0);

        slotsPayoutSeven = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for matching 3 sevens on the slot machine (jackpot)."),
                fileConfiguration, "payouts.slots.seven", 25.0);

        slotsPayoutTwoMatch = ConfigurationEngine.setDouble(
                List.of("Payout multiplier for matching only 2 symbols on the slot machine."),
                fileConfiguration, "payouts.slots.twoMatch", 0.5);

        higherLowerMultiplier = ConfigurationEngine.setDouble(
                List.of("Multiplier applied to the bet for each correct guess in Higher/Lower.",
                        "This stacks: 1st correct = 1.5x, 2nd = 2.25x, 3rd = 3.375x, etc."),
                fileConfiguration, "payouts.higherLower.multiplier", 1.5);

        // Messages
        insufficientFundsMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player tries to bet more than they can afford."),
                file, fileConfiguration, "messages.insufficientFunds", "&c[Casino] You can't afford that bet!", true);

        maxDebtReachedMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player has reached maximum debt."),
                file, fileConfiguration, "messages.maxDebtReached", "&c[Casino] You've reached your credit limit! Pay off some debt first.", true);

        winMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player wins a gambling game.",
                        "Use %amount% for the win amount."),
                file, fileConfiguration, "messages.win", "&a[Casino] Congratulations! You won &6%amount% coins&a!", true);

        loseMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player loses a gambling game.",
                        "Use %amount% for the lost amount."),
                file, fileConfiguration, "messages.lose", "&c[Casino] Better luck next time. You lost &6%amount% coins&c.", true);

        debtWarningMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player goes into debt.",
                        "Use %debt% for the current debt amount."),
                file, fileConfiguration, "messages.debtWarning", "&c[Casino] Warning: You are now &6%debt% coins &cin debt! The Debt Collector is watching...", true);

        betPlacedMessage = ConfigurationEngine.setString(
                List.of("Message shown when a bet is placed.",
                        "Use %amount% for the bet amount."),
                file, fileConfiguration, "messages.betPlaced", "&7[Casino] Bet placed: &6%amount% coins", true);

        balanceDisplayMessage = ConfigurationEngine.setString(
                List.of("Message format for displaying balance and debt.",
                        "Use %balance% and %debt%."),
                file, fileConfiguration, "messages.balanceDisplay", "&7Balance: &a%balance% &7| Debt: &c%debt%", true);

        // Menu titles
        bettingMenuTitle = ConfigurationEngine.setString(
                List.of("Title for the betting amount selection menu.",
                        "Use %game% for the game name."),
                file, fileConfiguration, "menus.bettingTitle", "&6&lCasino - %game%", true);

        playButtonText = ConfigurationEngine.setString(
                List.of("Text for the play button in the betting menu."),
                file, fileConfiguration, "menus.playButton", "&a&lPLAY!", true);

        cancelButtonText = ConfigurationEngine.setString(
                List.of("Text for the cancel button in the betting menu."),
                file, fileConfiguration, "menus.cancelButton", "&c&lCancel", true);

        maxBetButtonText = ConfigurationEngine.setString(
                List.of("Text for the max bet button in the betting menu."),
                file, fileConfiguration, "menus.maxBetButton", "&e&lMax Bet", true);

        // Game menu titles
        blackjackMenuTitle = ConfigurationEngine.setString(
                List.of("Title for the blackjack game menu."),
                file, fileConfiguration, "menus.blackjackTitle", "&6&lBlackjack", true);

        coinFlipMenuTitle = ConfigurationEngine.setString(
                List.of("Title for the coin flip game menu."),
                file, fileConfiguration, "menus.coinFlipTitle", "&e&lCoin Flip", true);

        slotsMenuTitle = ConfigurationEngine.setString(
                List.of("Title for the slot machine game menu."),
                file, fileConfiguration, "menus.slotsTitle", "&d&lSlot Machine", true);

        higherLowerMenuTitle = ConfigurationEngine.setString(
                List.of("Title for the higher/lower game menu."),
                file, fileConfiguration, "menus.higherLowerTitle", "&b&lHigher or Lower", true);
    }
}
