package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Unified betting menu for all gambling games.
 * Players select their bet amount here before the actual game starts.
 */
public class BettingMenu {

    private static final Map<UUID, BetSession> activeSessions = new HashMap<>();

    // Slot constants
    private static final int GAME_INFO_SLOT = 0;
    private static final int BALANCE_SLOT = 8;
    private static final int BET_DISPLAY_SLOT = 13;
    private static final int PLUS_100_SLOT = 10;
    private static final int PLUS_50_SLOT = 11;
    private static final int PLUS_10_SLOT = 12;
    private static final int MINUS_10_SLOT = 14;
    private static final int MINUS_50_SLOT = 15;
    private static final int MINUS_100_SLOT = 16;
    private static final int CANCEL_SLOT = 18;
    private static final int ALL_IN_SLOT = 21;
    private static final int ZERO_SLOT = 25;
    private static final int PLAY_SLOT = 26;

    /**
     * Opens the betting menu for a player.
     *
     * @param player   The player
     * @param gameType The type of gambling game
     */
    public static void openBettingMenu(Player player, GameType gameType) {
        if (!GamblingConfig.isGamblingEnabled()) {
            player.sendMessage(GamblingConfig.getBettingCasinoClosedMessage());
            return;
        }

        int defaultBet = Math.max(GamblingConfig.getMinBet(), 10);
        BetSession session = new BetSession(player.getUniqueId(), gameType, defaultBet);
        activeSessions.put(player.getUniqueId(), session);

        String title = GamblingConfig.getBettingMenuTitle().replace("%game%", gameType.getDisplayName());
        Inventory inventory = Bukkit.createInventory(player, 27, title);

        updateBettingDisplay(inventory, player, session);
        player.openInventory(inventory);
        BettingMenuEvents.menus.add(inventory);
    }

    /**
     * Updates the betting menu display.
     */
    private static void updateBettingDisplay(Inventory inventory, Player player, BetSession session) {
        UUID uuid = player.getUniqueId();

        // Game info item
        ItemStack gameInfo = ItemStackGenerator.generateItemStack(
                Material.BOOK,
                ChatColorConverter.convert("&6&l" + session.gameType.getDisplayName()),
                List.of(
                        "",
                        ChatColorConverter.convert("&7" + session.gameType.getDescription()),
                        "",
                        GamblingConfig.getBettingSelectBetLore()
                ),
                1
        );
        inventory.setItem(GAME_INFO_SLOT, gameInfo);

        // Balance display
        double balance = GamblingEconomyHandler.getBalance(uuid);
        double debt = GamblingEconomyHandler.getDebt(uuid);
        List<String> balanceLore = new ArrayList<>();
        balanceLore.add("");
        balanceLore.add(GamblingConfig.getBettingBalanceLabel() + String.format("%.2f", balance));
        if (debt > 0) {
            balanceLore.add(GamblingConfig.getBettingDebtLabel() + String.format("%.2f", debt));
            balanceLore.add(GamblingConfig.getBettingAvailableCreditLabel() + String.format("%.2f", GamblingEconomyHandler.getAvailableCredit(uuid)));
        }
        ItemStack balanceItem = ItemStackGenerator.generateItemStack(
                Material.GOLD_INGOT,
                GamblingConfig.getBettingYourFinancesTitle(),
                balanceLore,
                1
        );
        inventory.setItem(BALANCE_SLOT, balanceItem);

        // Current bet display
        boolean canAfford = GamblingEconomyHandler.canAffordBet(uuid, session.betAmount);
        ItemStack betDisplay = ItemStackGenerator.generateItemStack(
                canAfford ? Material.EMERALD : Material.REDSTONE,
                ChatColorConverter.convert((canAfford ? GamblingConfig.getBettingAffordableColor() : GamblingConfig.getBettingUnaffordableColor()) + GamblingConfig.getBettingCurrentBetPrefix() + session.betAmount),
                List.of(
                        "",
                        canAfford ? GamblingConfig.getBettingClickPlayLore() : GamblingConfig.getBettingCantAffordLore()
                ),
                1
        );
        inventory.setItem(BET_DISPLAY_SLOT, betDisplay);

        // Calculate increments based on player wealth
        int[] increments = calculateIncrements(uuid);
        int smallIncrement = increments[0];
        int mediumIncrement = increments[1];
        int largeIncrement = increments[2];

        // Plus buttons
        inventory.setItem(PLUS_10_SLOT, createModifierButton("+" + smallIncrement, Material.LIME_STAINED_GLASS_PANE, smallIncrement));
        inventory.setItem(PLUS_50_SLOT, createModifierButton("+" + mediumIncrement, Material.LIME_STAINED_GLASS_PANE, mediumIncrement));
        inventory.setItem(PLUS_100_SLOT, createModifierButton("+" + largeIncrement, Material.LIME_STAINED_GLASS_PANE, largeIncrement));

        // Minus buttons
        inventory.setItem(MINUS_10_SLOT, createModifierButton("-" + smallIncrement, Material.RED_STAINED_GLASS_PANE, smallIncrement));
        inventory.setItem(MINUS_50_SLOT, createModifierButton("-" + mediumIncrement, Material.RED_STAINED_GLASS_PANE, mediumIncrement));
        inventory.setItem(MINUS_100_SLOT, createModifierButton("-" + largeIncrement, Material.RED_STAINED_GLASS_PANE, largeIncrement));

        // All In button (under +100)
        double maxAvailable = GamblingEconomyHandler.getMaxBet(uuid);
        ItemStack allInButton = ItemStackGenerator.generateItemStack(
                Material.DIAMOND,
                GamblingConfig.getBettingAllInButtonText(),
                List.of(
                        "",
                        GamblingConfig.getBettingAllInLore(),
                        GamblingConfig.getBettingMaxAvailableLabel() + (int) maxAvailable
                ),
                1
        );
        inventory.setItem(ALL_IN_SLOT, allInButton);

        // Zero button (under -100)
        ItemStack zeroButton = ItemStackGenerator.generateItemStack(
                Material.COAL,
                GamblingConfig.getBettingResetButtonText(),
                List.of(
                        "",
                        GamblingConfig.getBettingResetLore() + GamblingConfig.getMinBet()
                ),
                1
        );
        inventory.setItem(ZERO_SLOT, zeroButton);

        // Cancel button
        ItemStack cancelButton = ItemStackGenerator.generateItemStack(
                Material.BARRIER,
                GamblingConfig.getCancelButtonText(),
                List.of(GamblingConfig.getBettingCancelLore()),
                1
        );
        inventory.setItem(CANCEL_SLOT, cancelButton);

        // Play button
        ItemStack playButton = ItemStackGenerator.generateItemStack(
                canAfford ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
                canAfford ? GamblingConfig.getPlayButtonText() : GamblingConfig.getBettingCantAffordButtonText(),
                List.of(
                        "",
                        canAfford
                                ? GamblingConfig.getBettingStartGameLore()
                                : GamblingConfig.getBettingReduceBetLore()
                ),
                1
        );
        inventory.setItem(PLAY_SLOT, playButton);
    }

    /**
     * Creates a bet modifier button.
     */
    private static ItemStack createModifierButton(String text, Material material, int amount) {
        return ItemStackGenerator.generateItemStack(
                material,
                ChatColorConverter.convert("&f" + text),
                List.of(GamblingConfig.getBettingAdjustBetLore() + text),
                1
        );
    }

    /**
     * Calculates the bet increments based on player wealth.
     * Uses percentages (5%, 10%, 25%) if player has >= 100 coins and no debt.
     * Falls back to fixed amounts (10, 50, 100) otherwise.
     *
     * @return array of 3 increments [small, medium, large]
     */
    private static int[] calculateIncrements(UUID uuid) {
        double balance = GamblingEconomyHandler.getBalance(uuid);
        double debt = GamblingEconomyHandler.getDebt(uuid);

        // Fall back to fixed amounts if player has less than 100 coins or is in debt
        if (balance < 100 || debt > 0) {
            return new int[]{10, 50, 100};
        }

        // Use percentage-based increments
        int small = Math.max(1, (int) (balance * 0.05));   // 5%
        int medium = Math.max(1, (int) (balance * 0.10));  // 10%
        int large = Math.max(1, (int) (balance * 0.25));   // 25%

        return new int[]{small, medium, large};
    }

    /**
     * Starts the selected game with the current bet.
     */
    private static void startGame(Player player, BetSession session) {
        // SAFETY-FIRST: Process the bet BEFORE any game logic
        if (!GamblingEconomyHandler.placeBet(player.getUniqueId(), session.betAmount)) {
            player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
            return;
        }

        // Check if player went into debt
        if (GamblingEconomyHandler.isInDebt(player.getUniqueId())) {
            player.sendMessage(
                    GamblingConfig.getDebtWarningMessage()
                            .replace("%debt%", String.format("%.2f", GamblingEconomyHandler.getDebt(player.getUniqueId())))
            );
        }

        player.sendMessage(
                GamblingConfig.getBetPlacedMessage()
                        .replace("%amount%", String.valueOf(session.betAmount))
        );

        // Close the betting menu
        player.closeInventory();

        // Open the appropriate game
        switch (session.gameType) {
            case COIN_FLIP -> CoinFlipGame.startGame(player, session.betAmount);
            case HIGHER_LOWER -> HigherLowerGame.startGame(player, session.betAmount);
            case BLACKJACK -> BlackjackGame.startGame(player, session.betAmount);
            case SLOTS -> SlotMachineGame.startGame(player, session.betAmount);
        }
    }

    public static void shutdown() {
        activeSessions.clear();
        BettingMenuEvents.menus.clear();
    }

    /**
     * Types of gambling games available.
     */
    public enum GameType {
        BLACKJACK,
        COIN_FLIP,
        SLOTS,
        HIGHER_LOWER;

        public String getDisplayName() {
            return switch (this) {
                case BLACKJACK -> GamblingConfig.getBettingBlackjackName();
                case COIN_FLIP -> GamblingConfig.getBettingCoinFlipName();
                case SLOTS -> GamblingConfig.getBettingSlotsName();
                case HIGHER_LOWER -> GamblingConfig.getBettingHigherLowerName();
            };
        }

        public String getDescription() {
            return switch (this) {
                case BLACKJACK -> GamblingConfig.getBettingBlackjackDescription();
                case COIN_FLIP -> GamblingConfig.getBettingCoinFlipDescription();
                case SLOTS -> GamblingConfig.getBettingSlotsDescription();
                case HIGHER_LOWER -> GamblingConfig.getBettingHigherLowerDescription();
            };
        }
    }

    /**
     * Stores betting session data for a player.
     */
    private static class BetSession {
        final UUID playerUUID;
        final GameType gameType;
        int betAmount;

        BetSession(UUID playerUUID, GameType gameType, int betAmount) {
            this.playerUUID = playerUUID;
            this.gameType = gameType;
            this.betAmount = betAmount;
        }
    }

    /**
     * Event handler for the betting menu.
     */
    public static class BettingMenuEvents implements Listener {
        static final Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            Player player = GamblingDisplay.validateClick(event, menus);
            if (player == null) return;

            BetSession session = activeSessions.get(player.getUniqueId());
            if (session == null) {
                player.closeInventory();
                return;
            }

            int slot = event.getSlot();
            int minBet = GamblingConfig.getMinBet();

            // Calculate increments based on player wealth
            int[] increments = calculateIncrements(player.getUniqueId());
            int smallIncrement = increments[0];
            int mediumIncrement = increments[1];
            int largeIncrement = increments[2];

            switch (slot) {
                case PLUS_10_SLOT -> session.betAmount = session.betAmount + smallIncrement;
                case PLUS_50_SLOT -> session.betAmount = session.betAmount + mediumIncrement;
                case PLUS_100_SLOT -> session.betAmount = session.betAmount + largeIncrement;
                case MINUS_10_SLOT -> session.betAmount = Math.max(minBet, session.betAmount - smallIncrement);
                case MINUS_50_SLOT -> session.betAmount = Math.max(minBet, session.betAmount - mediumIncrement);
                case MINUS_100_SLOT -> session.betAmount = Math.max(minBet, session.betAmount - largeIncrement);
                case ALL_IN_SLOT -> session.betAmount = (int) GamblingEconomyHandler.getMaxBet(player.getUniqueId());
                case ZERO_SLOT -> session.betAmount = minBet;
                case CANCEL_SLOT -> {
                    player.closeInventory();
                    return;
                }
                case PLAY_SLOT -> {
                    if (GamblingEconomyHandler.canAffordBet(player.getUniqueId(), session.betAmount)) {
                        startGame(player, session);
                    }
                    return;
                }
                default -> {
                    return;
                }
            }

            // Update display after bet change
            updateBettingDisplay(event.getInventory(), player, session);
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            menus.remove(event.getInventory());
            activeSessions.remove(event.getPlayer().getUniqueId());
        }
    }
}
