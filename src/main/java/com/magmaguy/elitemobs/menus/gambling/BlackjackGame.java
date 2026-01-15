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
import org.bukkit.event.inventory.InventoryType;
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
    private static final String[] SUIT_NAMES = {"Spades", "Hearts", "Diamonds", "Clubs"};
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

        String title = ChatColorConverter.convert(GamblingConfig.getBlackjackMenuTitle());
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
                ChatColorConverter.convert("&cDealer: " + dealerTotalText),
                List.of(),
                1
        );
        inventory.setItem(DEALER_TOTAL_SLOT, dealerTotal);

        // Player total
        int playerTotal = calculateHandValue(session.playerHand);
        Material totalMaterial = playerTotal > 21 ? Material.REDSTONE_BLOCK : (playerTotal == 21 ? Material.EMERALD_BLOCK : Material.BLUE_BANNER);
        ItemStack playerTotalItem = ItemStackGenerator.generateItemStack(
                totalMaterial,
                ChatColorConverter.convert("&aYou: " + playerTotal),
                List.of(),
                1
        );
        inventory.setItem(PLAYER_TOTAL_SLOT, playerTotalItem);

        // Action buttons (only if game is still active)
        if (session.gameState == GameState.PLAYING) {
            // Hit button
            ItemStack hitButton = ItemStackGenerator.generateItemStack(
                    Material.LIME_CONCRETE,
                    ChatColorConverter.convert("&a&lHIT"),
                    List.of(
                            "",
                            ChatColorConverter.convert("&7Draw another card")
                    ),
                    1
            );
            inventory.setItem(HIT_SLOT, hitButton);

            // Stand button
            ItemStack standButton = ItemStackGenerator.generateItemStack(
                    Material.YELLOW_CONCRETE,
                    ChatColorConverter.convert("&e&lSTAND"),
                    List.of(
                            "",
                            ChatColorConverter.convert("&7Keep your current hand")
                    ),
                    1
            );
            inventory.setItem(STAND_SLOT, standButton);

            // Double Down button (only on first action)
            if (session.playerHand.size() == 2 && session.canDoubleDown) {
                boolean canAffordDouble = GamblingEconomyHandler.canAffordBet(session.playerUUID, session.betAmount);
                ItemStack doubleButton = ItemStackGenerator.generateItemStack(
                        canAffordDouble ? Material.GOLD_BLOCK : Material.GRAY_CONCRETE,
                        ChatColorConverter.convert(canAffordDouble ? "&6&lDOUBLE DOWN" : "&7Double Down"),
                        List.of(
                                "",
                                canAffordDouble
                                        ? ChatColorConverter.convert("&7Double your bet, take one card, then stand")
                                        : ChatColorConverter.convert("&cYou can't afford to double!")
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
                ChatColorConverter.convert("&aBet: " + session.betAmount),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Win: " + String.format("%.2f", session.betAmount * GamblingConfig.getBlackjackPayoutNormal())),
                        ChatColorConverter.convert("&7Blackjack: " + String.format("%.2f", session.betAmount * GamblingConfig.getBlackjackPayoutBlackjack()))
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
                List.of(ChatColorConverter.convert("&7Value: " + card.getBlackjackValue())),
                1
        );
    }

    /**
     * Creates a hidden card item.
     */
    private static ItemStack createHiddenCardItem() {
        return ItemStackGenerator.generateItemStack(
                Material.GRAY_BANNER,
                ChatColorConverter.convert("&8???"),
                List.of(ChatColorConverter.convert("&7Hidden")),
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
            player.sendMessage(ChatColorConverter.convert(GamblingConfig.getInsufficientFundsMessage()));
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
                resultMessage = "&a&lBLACKJACK!";
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
            }
            case PLAYER_WINS -> {
                payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
                resultMessage = "&a&lYOU WIN!";
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            }
            case DEALER_BUST -> {
                payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
                resultMessage = "&a&lDEALER BUSTS!";
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            }
            case PUSH -> {
                payout = session.betAmount; // Return bet
                resultMessage = "&e&lPUSH - TIE";
                resultMaterial = Material.YELLOW_CONCRETE;
                resultSound = Sound.BLOCK_NOTE_BLOCK_PLING;
            }
            case PLAYER_BUST -> {
                resultMessage = "&c&lBUST!";
                resultMaterial = Material.REDSTONE_BLOCK;
                resultSound = Sound.ENTITY_VILLAGER_NO;
            }
            case DEALER_WINS -> {
                resultMessage = "&c&lDEALER WINS";
                resultMaterial = Material.REDSTONE_BLOCK;
                resultSound = Sound.ENTITY_VILLAGER_NO;
            }
            default -> {
                resultMessage = "&7Game Over";
                resultMaterial = Material.GRAY_CONCRETE;
                resultSound = Sound.BLOCK_NOTE_BLOCK_BASS;
            }
        }

        // SAFETY-FIRST: Award winnings BEFORE showing result
        if (payout > 0) {
            GamblingEconomyHandler.awardWinnings(player.getUniqueId(), payout);
        }

        // Update display to show dealer's full hand
        updateDisplay(inventory, session, true);

        // Show result
        List<String> resultLore = new ArrayList<>();
        resultLore.add("");
        resultLore.add(ChatColorConverter.convert("&7Player: " + calculateHandValue(session.playerHand)));
        resultLore.add(ChatColorConverter.convert("&7Dealer: " + calculateHandValue(session.dealerHand)));
        resultLore.add("");
        if (payout > 0) {
            resultLore.add(ChatColorConverter.convert(payout > session.betAmount
                    ? "&aYou won " + String.format("%.2f", payout - session.betAmount) + " coins!"
                    : "&7Your bet was returned."));
        } else {
            resultLore.add(ChatColorConverter.convert("&cYou lost " + session.betAmount + " coins."));
        }
        resultLore.add("");
        resultLore.add(ChatColorConverter.convert("&eClose to continue"));

        ItemStack resultItem = ItemStackGenerator.generateItemStack(
                resultMaterial,
                ChatColorConverter.convert(resultMessage),
                resultLore,
                1
        );
        inventory.setItem(RESULT_SLOT, resultItem);

        player.playSound(player.getLocation(), resultSound, 1.0f, 1.0f);

        if (payout > session.betAmount) {
            player.sendMessage(ChatColorConverter.convert(
                    GamblingConfig.getWinMessage().replace("%amount%", String.format("%.2f", payout - session.betAmount))
            ));
        } else if (payout == 0) {
            player.sendMessage(ChatColorConverter.convert(
                    GamblingConfig.getLoseMessage().replace("%amount%", String.valueOf(session.betAmount))
            ));
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
            return SUIT_COLORS[suit] + CARD_NAMES[value - 1] + " &7of " + SUIT_NAMES[suit];
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
    private static class BlackjackSession {
        final UUID playerUUID;
        final List<Card> playerHand = new ArrayList<>();
        final List<Card> dealerHand = new ArrayList<>();
        int betAmount;
        GameState gameState = GameState.PLAYING;
        boolean canDoubleDown = true;

        BlackjackSession(UUID playerUUID, int betAmount) {
            this.playerUUID = playerUUID;
            this.betAmount = betAmount;
        }
    }

    /**
     * Event handler for the blackjack menu.
     */
    public static class BlackjackMenuEvents implements Listener {
        static final Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!menus.contains(event.getInventory())) return;
            if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;

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
            menus.remove(event.getInventory());
            activeSessions.remove(event.getPlayer().getUniqueId());
        }
    }
}
