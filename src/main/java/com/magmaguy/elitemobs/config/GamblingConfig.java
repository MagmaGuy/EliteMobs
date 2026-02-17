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
    private static int minBet;

    // Debt Collector settings
    @Getter
    private static double debtCollectorSpawnChance;
    @Getter
    private static int debtCollectorCheckIntervalMinutes;
    @Getter
    private static int debtCollectorTimeoutSeconds;
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
    private static double coinFlipEdgeChance;
    @Getter
    private static double coinFlipEdgePayout;
    @Getter
    private static double higherLowerMultiplier;

    // Messages
    @Getter
    private static String insufficientFundsMessage;
    @Getter
    private static String winMessage;
    @Getter
    private static String loseMessage;
    @Getter
    private static String debtWarningMessage;
    @Getter
    private static String betPlacedMessage;

    // Betting menu
    @Getter
    private static String bettingMenuTitle;
    @Getter
    private static String playButtonText;
    @Getter
    private static String cancelButtonText;

    // Game-specific menu titles
    @Getter
    private static String blackjackMenuTitle;
    @Getter
    private static String coinFlipMenuTitle;
    @Getter
    private static String slotsMenuTitle;
    @Getter
    private static String higherLowerMenuTitle;

    // ---- Blackjack strings ----
    @Getter
    private static String blackjackHitButtonText;
    @Getter
    private static String blackjackHitButtonLore;
    @Getter
    private static String blackjackStandButtonText;
    @Getter
    private static String blackjackStandButtonLore;
    @Getter
    private static String blackjackDoubleDownButtonText;
    @Getter
    private static String blackjackDoubleDownButtonLore;
    @Getter
    private static String blackjackDoubleDownDisabledText;
    @Getter
    private static String blackjackDoubleDownCantAffordLore;
    @Getter
    private static String blackjackDealerLabel;
    @Getter
    private static String blackjackPlayerLabel;
    @Getter
    private static String blackjackBetLabel;
    @Getter
    private static String blackjackWinPayoutLabel;
    @Getter
    private static String blackjackBlackjackPayoutLabel;
    @Getter
    private static String blackjackCardValueLabel;
    @Getter
    private static String blackjackHiddenCardText;
    @Getter
    private static String blackjackHiddenCardLore;
    @Getter
    private static String blackjackResultBlackjack;
    @Getter
    private static String blackjackResultYouWin;
    @Getter
    private static String blackjackResultDealerBusts;
    @Getter
    private static String blackjackResultPush;
    @Getter
    private static String blackjackResultBust;
    @Getter
    private static String blackjackResultDealerWins;
    @Getter
    private static String blackjackResultGameOver;
    @Getter
    private static String blackjackResultPlayerLabel;
    @Getter
    private static String blackjackResultDealerLabel;
    @Getter
    private static String blackjackResultWonPrefix;
    @Getter
    private static String blackjackResultWonSuffix;
    @Getter
    private static String blackjackResultBetReturned;
    @Getter
    private static String blackjackResultLostPrefix;
    @Getter
    private static String blackjackResultCloseToContinue;
    @Getter
    private static String blackjackCardSuitSpades;
    @Getter
    private static String blackjackCardSuitHearts;
    @Getter
    private static String blackjackCardSuitDiamonds;
    @Getter
    private static String blackjackCardSuitClubs;
    @Getter
    private static String blackjackCardOfText;

    // ---- CoinFlip strings ----
    @Getter
    private static String coinFlipCoinTitle;
    @Getter
    private static String coinFlipChooseLore;
    @Getter
    private static String coinFlipWillFlipLore;
    @Getter
    private static String coinFlipHeadsButtonText;
    @Getter
    private static String coinFlipHeadsBetLore;
    @Getter
    private static String coinFlipTailsButtonText;
    @Getter
    private static String coinFlipTailsBetLore;
    @Getter
    private static String coinFlipMakeYourChoice;
    @Getter
    private static String coinFlipBetLabel;
    @Getter
    private static String coinFlipNormalWinLabel;
    @Getter
    private static String coinFlipPayoutLabel;
    @Getter
    private static String coinFlipEdgeChanceLore;
    @Getter
    private static String coinFlipEdgePayoutLore;
    @Getter
    private static String coinFlipFlipping;
    @Getter
    private static String coinFlipHeadsText;
    @Getter
    private static String coinFlipTailsText;
    @Getter
    private static String coinFlipEdgeCoinTitle;
    @Getter
    private static String coinFlipEdgeCoinLore;
    @Getter
    private static String coinFlipEdgeResultTitle;
    @Getter
    private static String coinFlipEdgeLandedLore;
    @Getter
    private static String coinFlipEdgeRareLore;
    @Getter
    private static String coinFlipEdgeWonPrefix;
    @Getter
    private static String coinFlipEdgePayoutMultiplier;
    @Getter
    private static String coinFlipEdgeChatMessage;
    @Getter
    private static String coinFlipLandedOnLore;
    @Getter
    private static String coinFlipHeadsWord;
    @Getter
    private static String coinFlipTailsWord;
    @Getter
    private static String coinFlipYouWin;
    @Getter
    private static String coinFlipWonPrefix;
    @Getter
    private static String coinFlipCloseToContinue;
    @Getter
    private static String coinFlipYouLose;
    @Getter
    private static String coinFlipLostPrefix;

    // ---- SlotMachine strings ----
    @Getter
    private static String slotsPaylineLabel;
    @Getter
    private static String slotsSpinButtonText;
    @Getter
    private static String slotsSpinClickLore;
    @Getter
    private static String slotsSpinBetLore;
    @Getter
    private static String slotsBetLabel;
    @Getter
    private static String slotsMatch3Lore;
    @Getter
    private static String slotsMatch2Lore;
    @Getter
    private static String slotsPayoutsTitle;
    @Getter
    private static String slotsPayoutTableTitle;
    @Getter
    private static String slotsChanceRarityNote;
    @Getter
    private static String slotsSpinning;
    @Getter
    private static String slotsJackpotTitle;
    @Getter
    private static String slotsBigWinTitle;
    @Getter
    private static String slotsSmallWinTitle;
    @Getter
    private static String slotsWonPrefix;
    @Getter
    private static String slotsCloseToContinue;
    @Getter
    private static String slotsNoMatchTitle;
    @Getter
    private static String slotsLostPrefix;

    // ---- HigherLower strings ----
    @Getter
    private static String higherLowerCardValueLabel;
    @Getter
    private static String higherLowerNextCardPrompt;
    @Getter
    private static String higherLowerHiddenCardText;
    @Getter
    private static String higherLowerMakeGuessLore;
    @Getter
    private static String higherLowerHigherButtonText;
    @Getter
    private static String higherLowerHigherLore1;
    @Getter
    private static String higherLowerHigherLore2;
    @Getter
    private static String higherLowerCashOutButtonText;
    @Getter
    private static String higherLowerCurrentWinningsLabel;
    @Getter
    private static String higherLowerCashOutClickLore;
    @Getter
    private static String higherLowerCashOutDisabledText;
    @Getter
    private static String higherLowerCashOutWinOnceLore;
    @Getter
    private static String higherLowerLowerButtonText;
    @Getter
    private static String higherLowerLowerLore1;
    @Getter
    private static String higherLowerLowerLore2;
    @Getter
    private static String higherLowerStreakLabel;
    @Getter
    private static String higherLowerMultiplierLabel;
    @Getter
    private static String higherLowerPotentialWinLabel;
    @Getter
    private static String higherLowerMaxStreakLabel;
    @Getter
    private static String higherLowerBetLabel;
    @Getter
    private static String higherLowerEachGuessLore;
    @Getter
    private static String higherLowerWait;
    @Getter
    private static String higherLowerRevealing;
    @Getter
    private static String higherLowerMaxStreakChat;
    @Getter
    private static String higherLowerCorrectChat;
    @Getter
    private static String higherLowerGameOverTitle;
    @Getter
    private static String higherLowerLostCardLore;
    @Getter
    private static String higherLowerLostCoinsLore;
    @Getter
    private static String higherLowerCloseToContinue;
    @Getter
    private static String higherLowerCashedOutTitle;
    @Getter
    private static String higherLowerCashedOutLore;
    @Getter
    private static String higherLowerCashedOutStreakLore;
    @Getter
    private static String higherLowerAutoCashOutChat;

    // ---- Debt Collector messages ----
    @Getter
    private static String debtCollectorSpawnMessage;
    @Getter
    private static String debtCollectorTimeoutMessage;
    @Getter
    private static String debtCollectorDeathMessage;
    @Getter
    private static String debtCollectorKillMessage;
    @Getter
    private static String debtPaidMessage;
    @Getter
    private static String debtClearedMessage;

    // ---- BettingMenu strings ----
    @Getter
    private static String bettingCasinoClosedMessage;
    @Getter
    private static String bettingSelectBetLore;
    @Getter
    private static String bettingYourFinancesTitle;
    @Getter
    private static String bettingBalanceLabel;
    @Getter
    private static String bettingDebtLabel;
    @Getter
    private static String bettingAvailableCreditLabel;
    @Getter
    private static String bettingCurrentBetPrefix;
    @Getter
    private static String bettingClickPlayLore;
    @Getter
    private static String bettingCantAffordLore;
    @Getter
    private static String bettingAdjustBetLore;
    @Getter
    private static String bettingAllInButtonText;
    @Getter
    private static String bettingAllInLore;
    @Getter
    private static String bettingMaxAvailableLabel;
    @Getter
    private static String bettingResetButtonText;
    @Getter
    private static String bettingResetLore;
    @Getter
    private static String bettingCancelLore;
    @Getter
    private static String bettingCantAffordButtonText;
    @Getter
    private static String bettingStartGameLore;
    @Getter
    private static String bettingReduceBetLore;
    @Getter
    private static String bettingBlackjackName;
    @Getter
    private static String bettingBlackjackDescription;
    @Getter
    private static String bettingCoinFlipName;
    @Getter
    private static String bettingCoinFlipDescription;
    @Getter
    private static String bettingSlotsName;
    @Getter
    private static String bettingSlotsDescription;
    @Getter
    private static String bettingHigherLowerName;
    @Getter
    private static String bettingHigherLowerDescription;

    // ---- General display strings ----
    @Getter
    private static String gamblingCurrencyWord;
    @Getter
    private static String coinFlipWinLore;
    @Getter
    private static String slotsPayoutSymbolFormat;
    @Getter
    private static String higherLowerAutoCashoutSuffix;
    @Getter
    private static String bettingAffordableColor;
    @Getter
    private static String bettingUnaffordableColor;
    @Getter
    private static String houseEarningsLabel;

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

        minBet = ConfigurationEngine.setInt(
                List.of("Minimum bet amount for all gambling games."),
                fileConfiguration, "minBet", 10);

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

        coinFlipEdgeChance = ConfigurationEngine.setDouble(
                List.of("Chance (0.0 to 1.0) for the coin to land on its edge (bonus win).",
                        "Default 0.01 = 1% chance. Set to 0.0 to disable the edge mechanic."),
                fileConfiguration, "payouts.coinFlip.edgeChance", 0.01);

        coinFlipEdgePayout = ConfigurationEngine.setDouble(
                List.of("Payout multiplier when the coin lands on its edge.",
                        "Combined with the edge chance and normal payout, this determines the overall house edge.",
                        "With 1% edge chance and 1.9x normal payout: 5.0x edge = ~1% house edge, 10.0x edge = ~4% player edge."),
                fileConfiguration, "payouts.coinFlip.edgePayout", 5.0);

        higherLowerMultiplier = ConfigurationEngine.setDouble(
                List.of("Multiplier applied to the bet for each correct guess in Higher/Lower.",
                        "This stacks: 1st correct = 1.3x, 2nd = 1.69x, 3rd = 2.197x, etc.",
                        "With ~71% average win rate per guess: 1.3x = ~7.7% house edge, 1.5x = ~6.5% player edge."),
                fileConfiguration, "payouts.higherLower.multiplierV2", 1.3);

        // Messages
        insufficientFundsMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player tries to bet more than they can afford."),
                file, fileConfiguration, "messages.insufficientFunds", "&c[Casino] You can't afford that bet!", true);

        winMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player wins a gambling game.",
                        "Use %amount% for the win amount, %game% for the game name."),
                file, fileConfiguration, "messages.win", "&a[%game%] Congratulations! You won &6%amount% coins&a!", true);

        loseMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player loses a gambling game.",
                        "Use %amount% for the lost amount, %game% for the game name."),
                file, fileConfiguration, "messages.lose", "&c[%game%] Better luck next time. You lost &6%amount% coins&c.", true);

        debtWarningMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player goes into debt.",
                        "Use %debt% for the current debt amount."),
                file, fileConfiguration, "messages.debtWarning", "&c[Casino] Warning: You are now &6%debt% coins &cin debt! The Debt Collector is watching...", true);

        betPlacedMessage = ConfigurationEngine.setString(
                List.of("Message shown when a bet is placed.",
                        "Use %amount% for the bet amount."),
                file, fileConfiguration, "messages.betPlaced", "&7[Casino] Bet placed: &6%amount% coins", true);

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

        // ---- Blackjack strings ----
        blackjackHitButtonText = ConfigurationEngine.setString(
                List.of("Text for the Hit button in Blackjack."),
                file, fileConfiguration, "blackjack.hitButton", "&a&lHIT", true);

        blackjackHitButtonLore = ConfigurationEngine.setString(
                List.of("Lore for the Hit button in Blackjack."),
                file, fileConfiguration, "blackjack.hitButtonLore", "&7Draw another card", true);

        blackjackStandButtonText = ConfigurationEngine.setString(
                List.of("Text for the Stand button in Blackjack."),
                file, fileConfiguration, "blackjack.standButton", "&e&lSTAND", true);

        blackjackStandButtonLore = ConfigurationEngine.setString(
                List.of("Lore for the Stand button in Blackjack."),
                file, fileConfiguration, "blackjack.standButtonLore", "&7Keep your current hand", true);

        blackjackDoubleDownButtonText = ConfigurationEngine.setString(
                List.of("Text for the Double Down button in Blackjack when the player can afford it."),
                file, fileConfiguration, "blackjack.doubleDownButton", "&6&lDOUBLE DOWN", true);

        blackjackDoubleDownButtonLore = ConfigurationEngine.setString(
                List.of("Lore for the Double Down button in Blackjack."),
                file, fileConfiguration, "blackjack.doubleDownButtonLore", "&7Double your bet, take one card, then stand", true);

        blackjackDoubleDownDisabledText = ConfigurationEngine.setString(
                List.of("Text for the Double Down button when the player cannot afford it."),
                file, fileConfiguration, "blackjack.doubleDownDisabled", "&7Double Down", true);

        blackjackDoubleDownCantAffordLore = ConfigurationEngine.setString(
                List.of("Lore shown when the player can't afford to double down."),
                file, fileConfiguration, "blackjack.doubleDownCantAffordLore", "&cYou can't afford to double!", true);

        blackjackDealerLabel = ConfigurationEngine.setString(
                List.of("Label for the dealer's total display. The value is appended after this."),
                file, fileConfiguration, "blackjack.dealerLabel", "&cDealer: ", true);

        blackjackPlayerLabel = ConfigurationEngine.setString(
                List.of("Label for the player's total display. The value is appended after this."),
                file, fileConfiguration, "blackjack.playerLabel", "&aYou: ", true);

        blackjackBetLabel = ConfigurationEngine.setString(
                List.of("Label for the bet info display. The bet amount is appended after this."),
                file, fileConfiguration, "blackjack.betLabel", "&aBet: ", true);

        blackjackWinPayoutLabel = ConfigurationEngine.setString(
                List.of("Label for the normal win payout in the bet info display."),
                file, fileConfiguration, "blackjack.winPayoutLabel", "&7Win: ", true);

        blackjackBlackjackPayoutLabel = ConfigurationEngine.setString(
                List.of("Label for the blackjack payout in the bet info display."),
                file, fileConfiguration, "blackjack.blackjackPayoutLabel", "&7Blackjack: ", true);

        blackjackCardValueLabel = ConfigurationEngine.setString(
                List.of("Label for the card value in the card display lore."),
                file, fileConfiguration, "blackjack.cardValueLabel", "&7Value: ", true);

        blackjackHiddenCardText = ConfigurationEngine.setString(
                List.of("Text for the hidden dealer card."),
                file, fileConfiguration, "blackjack.hiddenCardText", "&8???", true);

        blackjackHiddenCardLore = ConfigurationEngine.setString(
                List.of("Lore for the hidden dealer card."),
                file, fileConfiguration, "blackjack.hiddenCardLore", "&7Hidden", true);

        blackjackResultBlackjack = ConfigurationEngine.setString(
                List.of("Result text when the player gets a blackjack."),
                file, fileConfiguration, "blackjack.resultBlackjack", "&a&lBLACKJACK!", true);

        blackjackResultYouWin = ConfigurationEngine.setString(
                List.of("Result text when the player wins normally."),
                file, fileConfiguration, "blackjack.resultYouWin", "&a&lYOU WIN!", true);

        blackjackResultDealerBusts = ConfigurationEngine.setString(
                List.of("Result text when the dealer busts."),
                file, fileConfiguration, "blackjack.resultDealerBusts", "&a&lDEALER BUSTS!", true);

        blackjackResultPush = ConfigurationEngine.setString(
                List.of("Result text when the game is a push (tie)."),
                file, fileConfiguration, "blackjack.resultPush", "&e&lPUSH - TIE", true);

        blackjackResultBust = ConfigurationEngine.setString(
                List.of("Result text when the player busts."),
                file, fileConfiguration, "blackjack.resultBust", "&c&lBUST!", true);

        blackjackResultDealerWins = ConfigurationEngine.setString(
                List.of("Result text when the dealer wins."),
                file, fileConfiguration, "blackjack.resultDealerWins", "&c&lDEALER WINS", true);

        blackjackResultGameOver = ConfigurationEngine.setString(
                List.of("Default game over text."),
                file, fileConfiguration, "blackjack.resultGameOver", "&7Game Over", true);

        blackjackResultPlayerLabel = ConfigurationEngine.setString(
                List.of("Label for the player's hand value in the result lore."),
                file, fileConfiguration, "blackjack.resultPlayerLabel", "&7Player: ", true);

        blackjackResultDealerLabel = ConfigurationEngine.setString(
                List.of("Label for the dealer's hand value in the result lore."),
                file, fileConfiguration, "blackjack.resultDealerLabel", "&7Dealer: ", true);

        blackjackResultWonPrefix = ConfigurationEngine.setString(
                List.of("Prefix shown before the win amount in the result lore."),
                file, fileConfiguration, "blackjack.resultWonPrefix", "&aYou won ", true);

        blackjackResultWonSuffix = ConfigurationEngine.setString(
                List.of("Suffix shown after the win amount in the result lore."),
                file, fileConfiguration, "blackjack.resultWonSuffix", " coins!", true);

        blackjackResultBetReturned = ConfigurationEngine.setString(
                List.of("Text shown in the result lore when the bet is returned (push)."),
                file, fileConfiguration, "blackjack.resultBetReturned", "&7Your bet was returned.", true);

        blackjackResultLostPrefix = ConfigurationEngine.setString(
                List.of("Prefix shown before the lost amount in the result lore."),
                file, fileConfiguration, "blackjack.resultLostPrefix", "&cYou lost ", true);

        blackjackResultCloseToContinue = ConfigurationEngine.setString(
                List.of("Text telling the player to close the menu to continue."),
                file, fileConfiguration, "blackjack.resultCloseToContinue", "&eClose to continue", true);

        blackjackCardSuitSpades = ConfigurationEngine.setString(
                List.of("Display name for the Spades suit."),
                file, fileConfiguration, "blackjack.cardSuitSpades", "Spades", true);

        blackjackCardSuitHearts = ConfigurationEngine.setString(
                List.of("Display name for the Hearts suit."),
                file, fileConfiguration, "blackjack.cardSuitHearts", "Hearts", true);

        blackjackCardSuitDiamonds = ConfigurationEngine.setString(
                List.of("Display name for the Diamonds suit."),
                file, fileConfiguration, "blackjack.cardSuitDiamonds", "Diamonds", true);

        blackjackCardSuitClubs = ConfigurationEngine.setString(
                List.of("Display name for the Clubs suit."),
                file, fileConfiguration, "blackjack.cardSuitClubs", "Clubs", true);

        blackjackCardOfText = ConfigurationEngine.setString(
                List.of("The word 'of' between the rank and suit of a card (e.g. 'Ace of Spades')."),
                file, fileConfiguration, "blackjack.cardOfText", " &7of ", true);

        // ---- CoinFlip strings ----
        coinFlipCoinTitle = ConfigurationEngine.setString(
                List.of("Title for the coin display before the flip."),
                file, fileConfiguration, "coinFlip.coinTitle", "&6&lThe Coin", true);

        coinFlipChooseLore = ConfigurationEngine.setString(
                List.of("Lore prompting the player to choose Heads or Tails."),
                file, fileConfiguration, "coinFlip.chooseLore", "&7Choose Heads or Tails!", true);

        coinFlipWillFlipLore = ConfigurationEngine.setString(
                List.of("Lore telling the player the coin will flip."),
                file, fileConfiguration, "coinFlip.willFlipLore", "&7The coin will flip...", true);

        coinFlipHeadsButtonText = ConfigurationEngine.setString(
                List.of("Text for the Heads button."),
                file, fileConfiguration, "coinFlip.headsButton", "&e&lHEADS", true);

        coinFlipHeadsBetLore = ConfigurationEngine.setString(
                List.of("Lore prompting the player to bet on Heads."),
                file, fileConfiguration, "coinFlip.headsBetLore", "&7Click to bet on Heads!", true);

        coinFlipTailsButtonText = ConfigurationEngine.setString(
                List.of("Text for the Tails button."),
                file, fileConfiguration, "coinFlip.tailsButton", "&6&lTAILS", true);

        coinFlipTailsBetLore = ConfigurationEngine.setString(
                List.of("Lore prompting the player to bet on Tails."),
                file, fileConfiguration, "coinFlip.tailsBetLore", "&7Click to bet on Tails!", true);

        coinFlipMakeYourChoice = ConfigurationEngine.setString(
                List.of("Text shown in the result slot before a choice is made."),
                file, fileConfiguration, "coinFlip.makeYourChoice", "&7Make your choice!", true);

        coinFlipBetLabel = ConfigurationEngine.setString(
                List.of("Label for the bet info display. Bet amount and 'coins' are appended."),
                file, fileConfiguration, "coinFlip.betLabel", "&aBet: ", true);

        coinFlipNormalWinLabel = ConfigurationEngine.setString(
                List.of("Label for the normal win amount in the bet info."),
                file, fileConfiguration, "coinFlip.normalWinLabel", "&7Normal Win: &a", true);

        coinFlipPayoutLabel = ConfigurationEngine.setString(
                List.of("Label for the payout multiplier in the bet info."),
                file, fileConfiguration, "coinFlip.payoutLabel", "&7Payout: &e", true);

        coinFlipEdgeChanceLore = ConfigurationEngine.setString(
                List.of("Lore describing the edge chance."),
                file, fileConfiguration, "coinFlip.edgeChanceLore", "&b✦ 1% chance to land on EDGE!", true);

        coinFlipEdgePayoutLore = ConfigurationEngine.setString(
                List.of("Lore describing the edge payout. Amount is inserted dynamically."),
                file, fileConfiguration, "coinFlip.edgePayoutLoreV2", "&b✦ Edge Payout: &a5x &7(&a", true);

        coinFlipFlipping = ConfigurationEngine.setString(
                List.of("Text shown while the coin is flipping."),
                file, fileConfiguration, "coinFlip.flipping", "&7Flipping...", true);

        coinFlipHeadsText = ConfigurationEngine.setString(
                List.of("Text shown for Heads during the animation."),
                file, fileConfiguration, "coinFlip.headsText", "&e&lHEADS", true);

        coinFlipTailsText = ConfigurationEngine.setString(
                List.of("Text shown for Tails during the animation."),
                file, fileConfiguration, "coinFlip.tailsText", "&6&lTAILS", true);

        coinFlipEdgeCoinTitle = ConfigurationEngine.setString(
                List.of("Title for the coin when it lands on edge."),
                file, fileConfiguration, "coinFlip.edgeCoinTitle", "&b&l✦ EDGE! ✦", true);

        coinFlipEdgeCoinLore = ConfigurationEngine.setString(
                List.of("Lore for the coin when it lands on edge."),
                file, fileConfiguration, "coinFlip.edgeCoinLore", "&7The coin landed on its &bEDGE&7!", true);

        coinFlipEdgeResultTitle = ConfigurationEngine.setString(
                List.of("Title for the edge result display."),
                file, fileConfiguration, "coinFlip.edgeResultTitle", "&b&l✦ INCREDIBLE! ✦", true);

        coinFlipEdgeLandedLore = ConfigurationEngine.setString(
                List.of("Lore explaining the coin landed on its edge."),
                file, fileConfiguration, "coinFlip.edgeLandedLore", "&7The coin landed on its &bEDGE&7!", true);

        coinFlipEdgeRareLore = ConfigurationEngine.setString(
                List.of("Lore explaining the rarity of landing on edge."),
                file, fileConfiguration, "coinFlip.edgeRareLore", "&7This is incredibly rare! (1% chance)", true);

        coinFlipEdgeWonPrefix = ConfigurationEngine.setString(
                List.of("Prefix before the won amount in the edge result. Amount and 'coins' are appended."),
                file, fileConfiguration, "coinFlip.edgeWonPrefix", "&7You won &a", true);

        coinFlipEdgePayoutMultiplier = ConfigurationEngine.setString(
                List.of("Text showing the payout multiplier in the edge result."),
                file, fileConfiguration, "coinFlip.edgePayoutMultiplierV2", "&6&l5x PAYOUT!", true);

        coinFlipEdgeChatMessage = ConfigurationEngine.setString(
                List.of("Chat message sent when the coin lands on edge.",
                        "Use %amount% for the win amount."),
                file, fileConfiguration, "coinFlip.edgeChatMessageV2", "&b&l✦ EDGE! ✦ &7The coin landed on its edge! &a5x PAYOUT! &7You won &a%amount% coins&7!", true);

        coinFlipLandedOnLore = ConfigurationEngine.setString(
                List.of("Lore prefix for the final coin result. The side name is appended."),
                file, fileConfiguration, "coinFlip.landedOnLore", "&7The coin landed on ", true);

        coinFlipHeadsWord = ConfigurationEngine.setString(
                List.of("The word 'Heads' used in the landed-on lore."),
                file, fileConfiguration, "coinFlip.headsWord", "Heads", true);

        coinFlipTailsWord = ConfigurationEngine.setString(
                List.of("The word 'Tails' used in the landed-on lore."),
                file, fileConfiguration, "coinFlip.tailsWord", "Tails", true);

        coinFlipYouWin = ConfigurationEngine.setString(
                List.of("Title shown when the player wins the coin flip."),
                file, fileConfiguration, "coinFlip.youWin", "&a&lYOU WIN!", true);

        coinFlipWonPrefix = ConfigurationEngine.setString(
                List.of("Prefix before the won amount in the win result. Amount and 'coins' are appended."),
                file, fileConfiguration, "coinFlip.wonPrefix", "&7You won &a", true);

        coinFlipCloseToContinue = ConfigurationEngine.setString(
                List.of("Text telling the player to close the menu to continue."),
                file, fileConfiguration, "coinFlip.closeToContinue", "&eClose to continue", true);

        coinFlipYouLose = ConfigurationEngine.setString(
                List.of("Title shown when the player loses the coin flip."),
                file, fileConfiguration, "coinFlip.youLose", "&c&lYOU LOSE", true);

        coinFlipLostPrefix = ConfigurationEngine.setString(
                List.of("Prefix before the lost amount in the lose result. Amount and 'coins' are appended."),
                file, fileConfiguration, "coinFlip.lostPrefix", "&7You lost &c", true);

        // ---- SlotMachine strings ----
        slotsPaylineLabel = ConfigurationEngine.setString(
                List.of("Label for the payline indicators."),
                file, fileConfiguration, "slots.paylineLabel", "&e>>> PAYLINE >>>", true);

        slotsSpinButtonText = ConfigurationEngine.setString(
                List.of("Text for the Spin button."),
                file, fileConfiguration, "slots.spinButton", "&a&lSPIN!", true);

        slotsSpinClickLore = ConfigurationEngine.setString(
                List.of("Lore for the Spin button prompting the player to click."),
                file, fileConfiguration, "slots.spinClickLore", "&7Click to spin the reels!", true);

        slotsSpinBetLore = ConfigurationEngine.setString(
                List.of("Lore showing the bet amount on the Spin button. Amount is appended."),
                file, fileConfiguration, "slots.spinBetLore", "&7Bet: &6", true);

        slotsBetLabel = ConfigurationEngine.setString(
                List.of("Label for the bet info display. Amount and 'coins' are appended."),
                file, fileConfiguration, "slots.betLabel", "&aBet: ", true);

        slotsMatch3Lore = ConfigurationEngine.setString(
                List.of("Lore explaining 3-symbol match wins."),
                file, fileConfiguration, "slots.match3Lore", "&7Match 3 symbols to win!", true);

        slotsMatch2Lore = ConfigurationEngine.setString(
                List.of("Lore explaining 2-symbol match returns."),
                file, fileConfiguration, "slots.match2Lore", "&7Match 2 symbols for a small return", true);

        slotsPayoutsTitle = ConfigurationEngine.setString(
                List.of("Title header in the payout table lore."),
                file, fileConfiguration, "slots.payoutsTitle", "&6&lPAYOUTS &7(Rarer = Higher!)", true);

        slotsPayoutTableTitle = ConfigurationEngine.setString(
                List.of("Title for the payout table item."),
                file, fileConfiguration, "slots.payoutTableTitle", "&6Payout Table", true);

        slotsChanceRarityNote = ConfigurationEngine.setString(
                List.of("Note at the bottom of the payout table."),
                file, fileConfiguration, "slots.chanceRarityNote", "&8Chance shown as rarity colors", true);

        slotsSpinning = ConfigurationEngine.setString(
                List.of("Text shown while the reels are spinning."),
                file, fileConfiguration, "slots.spinning", "&7Spinning...", true);

        slotsJackpotTitle = ConfigurationEngine.setString(
                List.of("Title shown when the player hits the jackpot (3 sevens)."),
                file, fileConfiguration, "slots.jackpotTitle", "&6&l✦ JACKPOT! ✦", true);

        slotsBigWinTitle = ConfigurationEngine.setString(
                List.of("Title shown when the player matches 3 symbols (non-jackpot)."),
                file, fileConfiguration, "slots.bigWinTitle", "&a&lBIG WIN!", true);

        slotsSmallWinTitle = ConfigurationEngine.setString(
                List.of("Title shown when the player matches 2 symbols."),
                file, fileConfiguration, "slots.smallWinTitle", "&e&lSmall Win!", true);

        slotsWonPrefix = ConfigurationEngine.setString(
                List.of("Prefix before the won amount. Amount and 'coins' are appended."),
                file, fileConfiguration, "slots.wonPrefix", "&7You won &a", true);

        slotsCloseToContinue = ConfigurationEngine.setString(
                List.of("Text telling the player to close the menu to continue."),
                file, fileConfiguration, "slots.closeToContinue", "&eClose to continue", true);

        slotsNoMatchTitle = ConfigurationEngine.setString(
                List.of("Title shown when no symbols match."),
                file, fileConfiguration, "slots.noMatchTitle", "&c&lNo Match", true);

        slotsLostPrefix = ConfigurationEngine.setString(
                List.of("Prefix before the lost amount. Amount and 'coins' are appended."),
                file, fileConfiguration, "slots.lostPrefix", "&7You lost &c", true);

        // ---- HigherLower strings ----
        higherLowerCardValueLabel = ConfigurationEngine.setString(
                List.of("Label for displaying a card's numeric value."),
                file, fileConfiguration, "higherLower.cardValueLabel", "&7Value: &f", true);

        higherLowerNextCardPrompt = ConfigurationEngine.setString(
                List.of("Lore prompting the player about the next card."),
                file, fileConfiguration, "higherLower.nextCardPrompt", "&7Is the next card higher or lower?", true);

        higherLowerHiddenCardText = ConfigurationEngine.setString(
                List.of("Text for the hidden next card."),
                file, fileConfiguration, "higherLower.hiddenCardText", "&8&l???", true);

        higherLowerMakeGuessLore = ConfigurationEngine.setString(
                List.of("Lore prompting the player to make a guess."),
                file, fileConfiguration, "higherLower.makeGuessLore", "&7Make your guess!", true);

        higherLowerHigherButtonText = ConfigurationEngine.setString(
                List.of("Text for the Higher button."),
                file, fileConfiguration, "higherLower.higherButton", "&a&lHIGHER", true);

        higherLowerHigherLore1 = ConfigurationEngine.setString(
                List.of("First line of lore for the Higher button."),
                file, fileConfiguration, "higherLower.higherLore1", "&7Click if you think the next", true);

        higherLowerHigherLore2 = ConfigurationEngine.setString(
                List.of("Second line of lore for the Higher button. The card name is appended."),
                file, fileConfiguration, "higherLower.higherLore2", "&7card is &ahigher &7than ", true);

        higherLowerCashOutButtonText = ConfigurationEngine.setString(
                List.of("Text for the Cash Out button when the player has a streak."),
                file, fileConfiguration, "higherLower.cashOutButton", "&e&lCASH OUT", true);

        higherLowerCurrentWinningsLabel = ConfigurationEngine.setString(
                List.of("Label for current winnings in the Cash Out button lore."),
                file, fileConfiguration, "higherLower.currentWinningsLabel", "&7Current Winnings: &a", true);

        higherLowerCashOutClickLore = ConfigurationEngine.setString(
                List.of("Lore prompting the player to click to take winnings."),
                file, fileConfiguration, "higherLower.cashOutClickLore", "&7Click to take your winnings!", true);

        higherLowerCashOutDisabledText = ConfigurationEngine.setString(
                List.of("Text for the Cash Out button when disabled (no streak)."),
                file, fileConfiguration, "higherLower.cashOutDisabled", "&7Cash Out", true);

        higherLowerCashOutWinOnceLore = ConfigurationEngine.setString(
                List.of("Lore shown when the player needs to win once before cashing out."),
                file, fileConfiguration, "higherLower.cashOutWinOnceLore", "&7Win at least once to cash out!", true);

        higherLowerLowerButtonText = ConfigurationEngine.setString(
                List.of("Text for the Lower button."),
                file, fileConfiguration, "higherLower.lowerButton", "&c&lLOWER", true);

        higherLowerLowerLore1 = ConfigurationEngine.setString(
                List.of("First line of lore for the Lower button."),
                file, fileConfiguration, "higherLower.lowerLore1", "&7Click if you think the next", true);

        higherLowerLowerLore2 = ConfigurationEngine.setString(
                List.of("Second line of lore for the Lower button. The card name is appended."),
                file, fileConfiguration, "higherLower.lowerLore2", "&7card is &clower &7than ", true);

        higherLowerStreakLabel = ConfigurationEngine.setString(
                List.of("Label for the streak display. Streak count and max are appended."),
                file, fileConfiguration, "higherLower.streakLabel", "&6Streak: ", true);

        higherLowerMultiplierLabel = ConfigurationEngine.setString(
                List.of("Label for the multiplier in the streak display."),
                file, fileConfiguration, "higherLower.multiplierLabel", "&7Multiplier: &e", true);

        higherLowerPotentialWinLabel = ConfigurationEngine.setString(
                List.of("Label for the potential win in the streak display."),
                file, fileConfiguration, "higherLower.potentialWinLabel", "&7Potential Win: &a", true);

        higherLowerMaxStreakLabel = ConfigurationEngine.setString(
                List.of("Label for the max streak in the streak display."),
                file, fileConfiguration, "higherLower.maxStreakLabel", "&7Max streak: &e", true);

        higherLowerBetLabel = ConfigurationEngine.setString(
                List.of("Label for the bet amount display."),
                file, fileConfiguration, "higherLower.betLabel", "&aBet: ", true);

        higherLowerEachGuessLore = ConfigurationEngine.setString(
                List.of("Lore explaining the multiplier per correct guess. The multiplier value is appended."),
                file, fileConfiguration, "higherLower.eachGuessLore", "&7Each correct guess multiplies by &e", true);

        higherLowerWait = ConfigurationEngine.setString(
                List.of("Text shown when buttons are disabled during processing."),
                file, fileConfiguration, "higherLower.wait", "&7Wait...", true);

        higherLowerRevealing = ConfigurationEngine.setString(
                List.of("Text shown during the card reveal animation."),
                file, fileConfiguration, "higherLower.revealing", "&7Revealing...", true);

        higherLowerMaxStreakChat = ConfigurationEngine.setString(
                List.of("Chat message sent when the player hits max streak and is auto-cashed out.",
                        "Use %streak% for the streak count and %amount% for the win amount."),
                file, fileConfiguration, "higherLower.maxStreakChat", "&a[Casino] &6MAX STREAK! &aYou hit %streak% correct guesses! Auto-cashing out: &6%amount% coins!", true);

        higherLowerCorrectChat = ConfigurationEngine.setString(
                List.of("Chat message sent when the player guesses correctly.",
                        "Use %streak% for the streak count and %multiplier% for the multiplier."),
                file, fileConfiguration, "higherLower.correctChat", "&a[Casino] Correct! Streak: %streak% | Multiplier: %multiplier%x", true);

        higherLowerGameOverTitle = ConfigurationEngine.setString(
                List.of("Title shown when the player loses in Higher/Lower."),
                file, fileConfiguration, "higherLower.gameOverTitle", "&c&lGAME OVER", true);

        higherLowerLostCardLore = ConfigurationEngine.setString(
                List.of("Lore prefix showing what the card was. The card name is appended."),
                file, fileConfiguration, "higherLower.lostCardLore", "&7You lost! The card was ", true);

        higherLowerLostCoinsLore = ConfigurationEngine.setString(
                List.of("Lore prefix showing how many coins were lost. Amount and 'coins' are appended."),
                file, fileConfiguration, "higherLower.lostCoinsLore", "&7You lost &c", true);

        higherLowerCloseToContinue = ConfigurationEngine.setString(
                List.of("Text telling the player to close the menu to continue."),
                file, fileConfiguration, "higherLower.closeToContinue", "&eClose to continue", true);

        higherLowerCashedOutTitle = ConfigurationEngine.setString(
                List.of("Title shown when the player cashes out."),
                file, fileConfiguration, "higherLower.cashedOutTitle", "&a&lCASHED OUT!", true);

        higherLowerCashedOutLore = ConfigurationEngine.setString(
                List.of("Lore prefix showing cash out amount. Amount and 'coins!' are appended."),
                file, fileConfiguration, "higherLower.cashedOutLore", "&7You cashed out with &a", true);

        higherLowerCashedOutStreakLore = ConfigurationEngine.setString(
                List.of("Lore prefix showing the streak at cash out. Streak count is appended."),
                file, fileConfiguration, "higherLower.cashedOutStreakLore", "&7Streak: &e", true);

        higherLowerAutoCashOutChat = ConfigurationEngine.setString(
                List.of("Chat message sent when auto-cashing out on menu close.",
                        "Use %amount% for the awarded amount and %streak% for the streak."),
                file, fileConfiguration, "higherLower.autoCashOutChat", "&a[Casino] Auto-cashed out: &6%amount% coins &a(Streak: %streak%)", true);

        // ---- General display strings ----
        gamblingCurrencyWord = ConfigurationEngine.setString(
                List.of("The currency word displayed in gambling UIs (e.g. 'coins')."),
                file, fileConfiguration, "gambling.currencyWord", "coins", true);

        coinFlipWinLore = ConfigurationEngine.setString(
                List.of("Label for the win amount shown on Heads/Tails buttons. Amount is appended."),
                file, fileConfiguration, "coinFlip.winLore", "&7Win: &a", true);

        slotsPayoutSymbolFormat = ConfigurationEngine.setString(
                List.of("Format for each line in the slot machine payout table.",
                        "Use $symbol for the symbol name, $multiplier for 3-match payout, $twoMatch for 2-match payout."),
                file, fileConfiguration, "slots.payoutSymbolFormat", "$symbol&7: &a$multiplierx &8(2x: $twoMatchx)", true);

        higherLowerAutoCashoutSuffix = ConfigurationEngine.setString(
                List.of("Suffix shown after the max streak value in Higher/Lower."),
                file, fileConfiguration, "higherLower.autoCashoutSuffix", " &7(auto-cashout)", true);

        bettingAffordableColor = ConfigurationEngine.setString(
                List.of("Color code applied to the current bet display when the player can afford it."),
                file, fileConfiguration, "betting.affordableColor", "&a", true);

        bettingUnaffordableColor = ConfigurationEngine.setString(
                List.of("Color code applied to the current bet display when the player cannot afford it."),
                file, fileConfiguration, "betting.unaffordableColor", "&c", true);

        houseEarningsLabel = ConfigurationEngine.setString(
                List.of("Label displayed above the Gambling Den Owner NPC showing house earnings."),
                file, fileConfiguration, "gambling.houseEarningsLabel", "&6House Earnings: ", true);

        // ---- Debt Collector messages ----
        debtCollectorSpawnMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when the Debt Collector spawns for a player.",
                        "$player is the placeholder for the player name.",
                        "$amount is the placeholder for the debt amount."),
                file, fileConfiguration, "debtCollector.spawnMessage", "&c[Debt Collector] &7Well well well... &e$player&7. You owe us &6$amount coins&7. Time to pay up!", true);
        debtCollectorTimeoutMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when the Debt Collector times out.",
                        "$player is the placeholder for the player name."),
                file, fileConfiguration, "debtCollector.timeoutMessage", "&c[Debt Collector] &7Running won't save you, &e$player&7. I'll be back for what you owe!", true);
        debtCollectorDeathMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when the Debt Collector is killed.",
                        "$player is the placeholder for the player name."),
                file, fileConfiguration, "debtCollector.deathMessage", "&c[Debt Collector] &7Ugh... I'll be back, &e$player&7. And I'll bring friends!", true);
        debtCollectorKillMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when the Debt Collector kills a player.",
                        "$player is the placeholder for the player name."),
                file, fileConfiguration, "debtCollector.killMessage", "&c[Debt Collector] &7Pleasure doing business, &e$player&7. Let that be a lesson about debts!", true);
        debtPaidMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when debt is partially paid.",
                        "$amount is the placeholder for the paid amount.",
                        "$remaining is the placeholder for the remaining debt."),
                file, fileConfiguration, "debtCollector.debtPaidMessage", "&7[Casino] &e$amount coins &7of debt paid. Remaining debt: &c$remaining coins", true);
        debtClearedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player's debt is fully cleared."),
                file, fileConfiguration, "debtCollector.debtClearedMessage", "&a[Casino] &7Your debt has been cleared! You're free... for now.", true);

        // ---- BettingMenu strings ----
        bettingCasinoClosedMessage = ConfigurationEngine.setString(
                List.of("Message shown when the casino is disabled."),
                file, fileConfiguration, "betting.casinoClosedMessage", "&c[Casino] The casino is currently closed!", true);

        bettingSelectBetLore = ConfigurationEngine.setString(
                List.of("Lore prompting the player to select their bet."),
                file, fileConfiguration, "betting.selectBetLore", "&eSelect your bet amount below!", true);

        bettingYourFinancesTitle = ConfigurationEngine.setString(
                List.of("Title for the finances display item."),
                file, fileConfiguration, "betting.yourFinancesTitle", "&6Your Finances", true);

        bettingBalanceLabel = ConfigurationEngine.setString(
                List.of("Label for the balance in the finances display."),
                file, fileConfiguration, "betting.balanceLabel", "&7Balance: &a", true);

        bettingDebtLabel = ConfigurationEngine.setString(
                List.of("Label for the debt in the finances display."),
                file, fileConfiguration, "betting.debtLabel", "&7Debt: &c", true);

        bettingAvailableCreditLabel = ConfigurationEngine.setString(
                List.of("Label for the available credit in the finances display."),
                file, fileConfiguration, "betting.availableCreditLabel", "&7Available Credit: &e", true);

        bettingCurrentBetPrefix = ConfigurationEngine.setString(
                List.of("Prefix for the current bet display. Amount is appended."),
                file, fileConfiguration, "betting.currentBetPrefix", "Current Bet: ", true);

        bettingClickPlayLore = ConfigurationEngine.setString(
                List.of("Lore shown when the player can afford the bet."),
                file, fileConfiguration, "betting.clickPlayLore", "&7Click Play to start!", true);

        bettingCantAffordLore = ConfigurationEngine.setString(
                List.of("Lore shown when the player can't afford the bet."),
                file, fileConfiguration, "betting.cantAffordLore", "&cYou can't afford this bet!", true);

        bettingAdjustBetLore = ConfigurationEngine.setString(
                List.of("Lore prefix for the bet adjustment buttons. The increment value is appended."),
                file, fileConfiguration, "betting.adjustBetLore", "&7Click to adjust bet by ", true);

        bettingAllInButtonText = ConfigurationEngine.setString(
                List.of("Text for the All In button."),
                file, fileConfiguration, "betting.allInButton", "&b&lALL IN", true);

        bettingAllInLore = ConfigurationEngine.setString(
                List.of("Lore for the All In button."),
                file, fileConfiguration, "betting.allInLore", "&7Bet everything you have!", true);

        bettingMaxAvailableLabel = ConfigurationEngine.setString(
                List.of("Label showing the max available bet. Amount is appended."),
                file, fileConfiguration, "betting.maxAvailableLabel", "&7Max available: &6", true);

        bettingResetButtonText = ConfigurationEngine.setString(
                List.of("Text for the Reset button."),
                file, fileConfiguration, "betting.resetButton", "&8&lRESET", true);

        bettingResetLore = ConfigurationEngine.setString(
                List.of("Lore for the Reset button. The min bet amount is appended."),
                file, fileConfiguration, "betting.resetLore", "&7Reset to minimum bet: &6", true);

        bettingCancelLore = ConfigurationEngine.setString(
                List.of("Lore for the Cancel button."),
                file, fileConfiguration, "betting.cancelLore", "&7Click to leave the casino", true);

        bettingCantAffordButtonText = ConfigurationEngine.setString(
                List.of("Text for the Play button when the player can't afford the bet."),
                file, fileConfiguration, "betting.cantAffordButton", "&c&lCan't Afford", true);

        bettingStartGameLore = ConfigurationEngine.setString(
                List.of("Lore for the Play button when the player can afford the bet."),
                file, fileConfiguration, "betting.startGameLore", "&7Start the game with your current bet!", true);

        bettingReduceBetLore = ConfigurationEngine.setString(
                List.of("Lore for the Play button when the player can't afford the bet."),
                file, fileConfiguration, "betting.reduceBetLore", "&7Reduce your bet or pay off debt", true);

        bettingBlackjackName = ConfigurationEngine.setString(
                List.of("Display name for the Blackjack game type."),
                file, fileConfiguration, "betting.blackjackName", "Blackjack", true);

        bettingBlackjackDescription = ConfigurationEngine.setString(
                List.of("Description for the Blackjack game type."),
                file, fileConfiguration, "betting.blackjackDescription", "Get as close to 21 as possible without going over!", true);

        bettingCoinFlipName = ConfigurationEngine.setString(
                List.of("Display name for the Coin Flip game type."),
                file, fileConfiguration, "betting.coinFlipName", "Coin Flip", true);

        bettingCoinFlipDescription = ConfigurationEngine.setString(
                List.of("Description for the Coin Flip game type."),
                file, fileConfiguration, "betting.coinFlipDescription", "Heads or Tails? Simple 50/50 chance!", true);

        bettingSlotsName = ConfigurationEngine.setString(
                List.of("Display name for the Slot Machine game type."),
                file, fileConfiguration, "betting.slotsName", "Slot Machine", true);

        bettingSlotsDescription = ConfigurationEngine.setString(
                List.of("Description for the Slot Machine game type."),
                file, fileConfiguration, "betting.slotsDescription", "Match three symbols to win big!", true);

        bettingHigherLowerName = ConfigurationEngine.setString(
                List.of("Display name for the Higher or Lower game type."),
                file, fileConfiguration, "betting.higherLowerName", "Higher or Lower", true);

        bettingHigherLowerDescription = ConfigurationEngine.setString(
                List.of("Description for the Higher or Lower game type."),
                file, fileConfiguration, "betting.higherLowerDescription", "Guess if the next card is higher or lower!", true);
    }
}
