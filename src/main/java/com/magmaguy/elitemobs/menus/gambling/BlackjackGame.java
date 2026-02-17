package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Blackjack gambling game.
 * Standard rules: Get as close to 21 as possible without going over.
 * Dealer stands on 17.
 * SAFETY: All outcomes are processed BEFORE visual animations.
 */
public class BlackjackGame {

    private static final Map<UUID, BlackjackSession> activeSessions = new HashMap<>();

    // Slot constants (54 slot inventory - 6 rows)
    private static final int[] DEALER_CARD_SLOTS = {1, 2, 3, 4, 5};
    private static final int[] PLAYER_CARD_SLOTS = {28, 29, 30, 31, 32};
    private static final int DEALER_TOTAL_SLOT = 7;
    private static final int PLAYER_TOTAL_SLOT = 34;
    private static final int HIT_SLOT = 45;
    private static final int STAND_SLOT = 47;
    private static final int DOUBLE_DOWN_SLOT = 49;
    private static final int BET_INFO_SLOT = 51;
    private static final int RESULT_SLOT = 22;

    // Card values (1-13 representing A-K)
    private static final String[] CARD_NAMES = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUIT_COLORS = {"&8", "&c", "&c", "&8"};

    /**
     * Starts a blackjack game for a player.
     * NOTE: The bet has already been processed in BettingMenu.startGame()
     *
     * @param player    The player
     * @param betAmount The amount bet
     */
    public static void startGame(Player player, int betAmount) {
        BlackjackSession session = new BlackjackSession(player.getUniqueId(), betAmount);

        // Deal initial cards
        session.playerHand.add(drawCard(session));
        session.dealerHand.add(drawCard(session));
        session.playerHand.add(drawCard(session));
        session.dealerHand.add(drawCard(session));

        activeSessions.put(player.getUniqueId(), session);

        String title = GamblingConfig.getBlackjackMenuTitle();
        Inventory inventory = Bukkit.createInventory(player, 54, title);

        updateDisplay(inventory, session, false);

        // Check for natural blackjack
        if (calculateHandValue(session.playerHand) == 21) {
            // Player got blackjack on first deal
            session.gameState = GameState.PLAYER_BLACKJACK;
            processEndGame(player, session, inventory);
        }

        player.openInventory(inventory);
        BlackjackMenuEvents.menus.add(inventory);
    }

    /**
     * Draws a card from the deck.
     */
    private static Card drawCard(BlackjackSession session) {
        int value = ThreadLocalRandom.current().nextInt(1, 14);
        int suit = ThreadLocalRandom.current().nextInt(0, 4);
        return new Card(value, suit);
    }

    /**
     * Calculates the value of a hand.
     * Handles Aces (can be 1 or 11).
     */
    private static int calculateHandValue(List<Card> hand) {
        int total = 0;
        int aces = 0;

        for (Card card : hand) {
            if (card.value == 1) {
                aces++;
                total += 11;
            } else {
                total += card.getBlackjackValue();
            }
        }

        // Convert Aces from 11 to 1 if needed
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        return total;
    }

    /**
     * Updates the game display.
     */
    private static void updateDisplay(Inventory inventory, BlackjackSession session, boolean showDealerHand) {
        // Clear previous cards
        for (int slot : DEALER_CARD_SLOTS) inventory.setItem(slot, null);
        for (int slot : PLAYER_CARD_SLOTS) inventory.setItem(slot, null);

        // Display dealer cards
        for (int i = 0; i < session.dealerHand.size(); i++) {
            if (i >= DEALER_CARD_SLOTS.length) break;

            if (i == 1 && !showDealerHand && session.gameState == GameState.PLAYING) {
                // Hide second dealer card
                inventory.setItem(DEALER_CARD_SLOTS[i], createHiddenCardItem());
            } else {
                inventory.setItem(DEALER_CARD_SLOTS[i], createCardItem(session.dealerHand.get(i)));
            }
        }

        // Display player cards
        for (int i = 0; i < session.playerHand.size(); i++) {
            if (i >= PLAYER_CARD_SLOTS.length) break;
            inventory.setItem(PLAYER_CARD_SLOTS[i], createCardItem(session.playerHand.get(i)));
        }

        // Dealer total (hidden during play)
        int dealerVisible = showDealerHand ? calculateHandValue(session.dealerHand) : session.dealerHand.get(0).getBlackjackValue();
        String dealerTotalText = showDealerHand ? String.valueOf(calculateHandValue(session.dealerHand)) : dealerVisible + " + ?";
        ItemStack dealerTotal = ItemStackGenerator.generateItemStack(
                Material.RED_BANNER,
                GamblingConfig.getBlackjackDealerLabel() + dealerTotalText,
                List.of(),
                1
        );
        inventory.setItem(DEALER_TOTAL_SLOT, dealerTotal);

        // Player total
        int playerTotal = calculateHandValue(session.playerHand);
        Material totalMaterial = playerTotal > 21 ? Material.REDSTONE_BLOCK : (playerTotal == 21 ? Material.EMERALD_BLOCK : Material.BLUE_BANNER);
        ItemStack playerTotalItem = ItemStackGenerator.generateItemStack(
                totalMaterial,
                GamblingConfig.getBlackjackPlayerLabel() + playerTotal,
                List.of(),
                1
        );
        inventory.setItem(PLAYER_TOTAL_SLOT, playerTotalItem);

        // Action buttons (only if game is still active)
        if (session.gameState == GameState.PLAYING) {
            // Hit button
            ItemStack hitButton = ItemStackGenerator.generateItemStack(
                    Material.LIME_CONCRETE,
                    GamblingConfig.getBlackjackHitButtonText(),
                    List.of(
                            "",
                            GamblingConfig.getBlackjackHitButtonLore()
                    ),
                    1
            );
            inventory.setItem(HIT_SLOT, hitButton);

            // Stand button
            ItemStack standButton = ItemStackGenerator.generateItemStack(
                    Material.YELLOW_CONCRETE,
                    GamblingConfig.getBlackjackStandButtonText(),
                    List.of(
                            "",
                            GamblingConfig.getBlackjackStandButtonLore()
                    ),
                    1
            );
            inventory.setItem(STAND_SLOT, standButton);

            // Double Down button (only on first action)
            if (session.playerHand.size() == 2 && session.canDoubleDown) {
                boolean canAffordDouble = GamblingEconomyHandler.canAffordBet(session.playerUUID, session.betAmount);
                ItemStack doubleButton = ItemStackGenerator.generateItemStack(
                        canAffordDouble ? Material.GOLD_BLOCK : Material.GRAY_CONCRETE,
                        canAffordDouble ? GamblingConfig.getBlackjackDoubleDownButtonText() : GamblingConfig.getBlackjackDoubleDownDisabledText(),
                        List.of(
                                "",
                                canAffordDouble
                                        ? GamblingConfig.getBlackjackDoubleDownButtonLore()
                                        : GamblingConfig.getBlackjackDoubleDownCantAffordLore()
                        ),
                        1
                );
                inventory.setItem(DOUBLE_DOWN_SLOT, doubleButton);
            } else {
                inventory.setItem(DOUBLE_DOWN_SLOT, null);
            }
        } else {
            // Game over - hide action buttons
            inventory.setItem(HIT_SLOT, null);
            inventory.setItem(STAND_SLOT, null);
            inventory.setItem(DOUBLE_DOWN_SLOT, null);
        }

        // Bet info
        ItemStack betInfo = ItemStackGenerator.generateItemStack(
                Material.EMERALD,
                GamblingConfig.getBlackjackBetLabel() + session.betAmount,
                List.of(
                        "",
                        GamblingConfig.getBlackjackWinPayoutLabel() + String.format("%.2f", session.betAmount * GamblingConfig.getBlackjackPayoutNormal()),
                        GamblingConfig.getBlackjackBlackjackPayoutLabel() + String.format("%.2f", session.betAmount * GamblingConfig.getBlackjackPayoutBlackjack())
                ),
                1
        );
        inventory.setItem(BET_INFO_SLOT, betInfo);
    }

    /**
     * Creates a card display item.
     */
    private static ItemStack createCardItem(Card card) {
        Material material = card.suit < 2 ? Material.BLACK_BANNER : Material.RED_BANNER;
        return ItemStackGenerator.generateItemStack(
                material,
                ChatColorConverter.convert(card.getName()),
                List.of(GamblingConfig.getBlackjackCardValueLabel() + card.getBlackjackValue()),
                1
        );
    }

    /**
     * Creates a hidden card item.
     */
    private static ItemStack createHiddenCardItem() {
        return ItemStackGenerator.generateItemStack(
                Material.GRAY_BANNER,
                GamblingConfig.getBlackjackHiddenCardText(),
                List.of(GamblingConfig.getBlackjackHiddenCardLore()),
                1
        );
    }

    /**
     * Handles player hitting.
     */
    private static void onHit(Player player, BlackjackSession session, Inventory inventory) {
        if (session.gameState != GameState.PLAYING) return;

        // Draw card
        session.playerHand.add(drawCard(session));
        session.canDoubleDown = false;

        int total = calculateHandValue(session.playerHand);

        // BACKEND FIRST: Check for bust
        if (total > 21) {
            session.gameState = GameState.PLAYER_BUST;
            processEndGame(player, session, inventory);
        } else if (total == 21) {
            // Auto-stand on 21
            onStand(player, session, inventory);
        } else {
            updateDisplay(inventory, session, false);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        }
    }

    /**
     * Handles player standing.
     */
    private static void onStand(Player player, BlackjackSession session, Inventory inventory) {
        if (session.gameState != GameState.PLAYING) return;

        session.gameState = GameState.DEALER_TURN;

        // Dealer draws cards
        playDealerTurn(player, session, inventory);
    }

    /**
     * Handles double down.
     */
    private static void onDoubleDown(Player player, BlackjackSession session, Inventory inventory) {
        if (session.gameState != GameState.PLAYING || !session.canDoubleDown) return;
        if (session.playerHand.size() != 2) return;

        // SAFETY-FIRST: Process the additional bet
        if (!GamblingEconomyHandler.placeBet(player.getUniqueId(), session.betAmount)) {
            player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
            return;
        }

        session.betAmount *= 2;
        session.canDoubleDown = false;

        // Draw exactly one card
        session.playerHand.add(drawCard(session));

        int total = calculateHandValue(session.playerHand);

        if (total > 21) {
            session.gameState = GameState.PLAYER_BUST;
            processEndGame(player, session, inventory);
        } else {
            // Force stand after double
            onStand(player, session, inventory);
        }
    }

    /**
     * Plays the dealer's turn with animation.
     */
    private static void playDealerTurn(Player player, BlackjackSession session, Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !BlackjackMenuEvents.menus.contains(inventory)) {
                    cancel();
                    return;
                }

                int dealerTotal = calculateHandValue(session.dealerHand);

                // Dealer draws until 17
                if (dealerTotal < 17) {
                    session.dealerHand.add(drawCard(session));
                    updateDisplay(inventory, session, true);
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                } else {
                    // Dealer is done
                    if (dealerTotal > 21) {
                        session.gameState = GameState.DEALER_BUST;
                    } else {
                        int playerTotal = calculateHandValue(session.playerHand);
                        if (playerTotal > dealerTotal) {
                            session.gameState = GameState.PLAYER_WINS;
                        } else if (dealerTotal > playerTotal) {
                            session.gameState = GameState.DEALER_WINS;
                        } else {
                            session.gameState = GameState.PUSH;
                        }
                    }
                    processEndGame(player, session, inventory);
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
    }

    /**
     * Processes the end of the game.
     */
    private static void processEndGame(Player player, BlackjackSession session, Inventory inventory) {
        double payout = 0;
        String resultMessage;
        Material resultMaterial;
        Sound resultSound;

        switch (session.gameState) {
            case PLAYER_BLACKJACK -> {
                payout = session.betAmount * GamblingConfig.getBlackjackPayoutBlackjack();
                resultMessage = GamblingConfig.getBlackjackResultBlackjack();
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
            }
            case PLAYER_WINS -> {
                payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
                resultMessage = GamblingConfig.getBlackjackResultYouWin();
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            }
            case DEALER_BUST -> {
                payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
                resultMessage = GamblingConfig.getBlackjackResultDealerBusts();
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            }
            case PUSH -> {
                payout = session.betAmount; // Return bet
                resultMessage = GamblingConfig.getBlackjackResultPush();
                resultMaterial = Material.YELLOW_CONCRETE;
                resultSound = Sound.BLOCK_NOTE_BLOCK_PLING;
            }
            case PLAYER_BUST -> {
                resultMessage = GamblingConfig.getBlackjackResultBust();
                resultMaterial = Material.REDSTONE_BLOCK;
                resultSound = Sound.ENTITY_VILLAGER_NO;
            }
            case DEALER_WINS -> {
                resultMessage = GamblingConfig.getBlackjackResultDealerWins();
                resultMaterial = Material.REDSTONE_BLOCK;
                resultSound = Sound.ENTITY_VILLAGER_NO;
            }
            default -> {
                resultMessage = GamblingConfig.getBlackjackResultGameOver();
                resultMaterial = Material.GRAY_CONCRETE;
                resultSound = Sound.BLOCK_NOTE_BLOCK_BASS;
            }
        }

        // SAFETY-FIRST: Resolve outcome BEFORE showing result
        payout = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);

        // Update display to show dealer's full hand
        updateDisplay(inventory, session, true);

        // Show result
        List<String> resultLore = new ArrayList<>();
        resultLore.add("");
        resultLore.add(GamblingConfig.getBlackjackResultPlayerLabel() + calculateHandValue(session.playerHand));
        resultLore.add(GamblingConfig.getBlackjackResultDealerLabel() + calculateHandValue(session.dealerHand));
        resultLore.add("");
        if (payout > 0) {
            resultLore.add(payout > session.betAmount
                    ? GamblingConfig.getBlackjackResultWonPrefix() + String.format("%.2f", payout - session.betAmount) + GamblingConfig.getBlackjackResultWonSuffix()
                    : GamblingConfig.getBlackjackResultBetReturned());
        } else {
            resultLore.add(GamblingConfig.getBlackjackResultLostPrefix() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord() + ".");
        }
        resultLore.add("");
        resultLore.add(GamblingConfig.getBlackjackResultCloseToContinue());

        ItemStack resultItem = ItemStackGenerator.generateItemStack(
                resultMaterial,
                resultMessage,
                resultLore,
                1
        );
        inventory.setItem(RESULT_SLOT, resultItem);

        player.playSound(player.getLocation(), resultSound, 1.0f, 1.0f);

        if (payout > session.betAmount) {
            GamblingDisplay.sendWinMessage(player, payout - session.betAmount, GamblingConfig.getBettingBlackjackName());
        } else if (payout == 0) {
            GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingBlackjackName());
        }
    }

    public static void shutdown() {
        activeSessions.clear();
        BlackjackMenuEvents.menus.clear();
    }

    /**
     * Game states.
     */
    private enum GameState {
        PLAYING,
        DEALER_TURN,
        PLAYER_BUST,
        DEALER_BUST,
        PLAYER_WINS,
        DEALER_WINS,
        PLAYER_BLACKJACK,
        PUSH
    }

    /**
     * Represents a playing card.
     */
    private static class Card {
        final int value; // 1-13 (A=1, J=11, Q=12, K=13)
        final int suit; // 0-3

        Card(int value, int suit) {
            this.value = value;
            this.suit = suit;
        }

        String getName() {
            String[] suitNames = {
                    GamblingConfig.getBlackjackCardSuitSpades(),
                    GamblingConfig.getBlackjackCardSuitHearts(),
                    GamblingConfig.getBlackjackCardSuitDiamonds(),
                    GamblingConfig.getBlackjackCardSuitClubs()
            };
            return SUIT_COLORS[suit] + CARD_NAMES[value - 1] + GamblingConfig.getBlackjackCardOfText() + suitNames[suit];
        }

        int getBlackjackValue() {
            if (value == 1) return 11; // Ace (handled specially)
            if (value >= 10) return 10; // Face cards
            return value;
        }
    }

    /**
     * Stores game session data.
     */
    private static class BlackjackSession extends GamblingSession {
        final List<Card> playerHand = new ArrayList<>();
        final List<Card> dealerHand = new ArrayList<>();
        GameState gameState = GameState.PLAYING;
        boolean canDoubleDown = true;

        BlackjackSession(UUID playerUUID, int betAmount) {
            super(playerUUID, betAmount);
        }
    }

    /**
     * Event handler for the blackjack menu.
     */
    public static class BlackjackMenuEvents implements Listener {
        static final Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            Player player = GamblingDisplay.validateClick(event, menus);
            if (player == null) return;

            BlackjackSession session = activeSessions.get(player.getUniqueId());
            if (session == null || session.gameState != GameState.PLAYING) return;

            int slot = event.getSlot();

            if (slot == HIT_SLOT) {
                onHit(player, session, event.getInventory());
            } else if (slot == STAND_SLOT) {
                onStand(player, session, event.getInventory());
            } else if (slot == DOUBLE_DOWN_SLOT) {
                onDoubleDown(player, session, event.getInventory());
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (!menus.remove(event.getInventory())) return;
            BlackjackSession session = activeSessions.remove(event.getPlayer().getUniqueId());
            if (session == null) return;

            // If closed during dealer's turn, resolve the hand
            if (session.gameState == GameState.DEALER_TURN) {
                // Play out dealer's hand
                while (calculateHandValue(session.dealerHand) < 17) {
                    session.dealerHand.add(drawCard(session));
                }
                int dealerTotal = calculateHandValue(session.dealerHand);
                int playerTotal = calculateHandValue(session.playerHand);

                double payout = 0;
                if (dealerTotal > 21 || playerTotal > dealerTotal) {
                    payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
                } else if (playerTotal == dealerTotal) {
                    payout = session.betAmount; // Push - return bet
                }

                double awarded = GamblingEconomyHandler.resolveOutcome(event.getPlayer().getUniqueId(), payout);
                if (awarded > 0 && event.getPlayer() instanceof Player player) {
                    GamblingDisplay.sendWinMessage(player, awarded - session.betAmount, GamblingConfig.getBettingBlackjackName());
                }
            } else if (session.gameState == GameState.PLAYING) {
                // Player closed mid-game without standing — forfeit
                GamblingEconomyHandler.resolveOutcome(event.getPlayer().getUniqueId(), 0);
            }
        }
    }
}
