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
 * Higher or Lower card game.
 * Player guesses if the next card is higher or lower than the current card.
 * Correct guesses multiply winnings, wrong guess loses everything.
 * SAFETY: Outcome is determined and processed BEFORE card reveal animation.
 */
public class HigherLowerGame {

    private static final Map<UUID, HigherLowerSession> activeSessions = new HashMap<>();

    // Slot constants
    private static final int CURRENT_CARD_SLOT = 11;
    private static final int NEXT_CARD_SLOT = 15;
    private static final int HIGHER_SLOT = 19;
    private static final int CASH_OUT_SLOT = 22;
    private static final int LOWER_SLOT = 25;
    private static final int STREAK_SLOT = 4;
    private static final int BET_INFO_SLOT = 13;

    // Card values (2-14, where 11=J, 12=Q, 13=K, 14=A)
    private static final String[] CARD_NAMES = {"", "", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
    private static final Material[] CARD_MATERIALS = {
            Material.WHITE_BANNER, // 2
            Material.WHITE_BANNER, // 3
            Material.WHITE_BANNER, // 4
            Material.WHITE_BANNER, // 5
            Material.ORANGE_BANNER, // 6
            Material.ORANGE_BANNER, // 7
            Material.ORANGE_BANNER, // 8
            Material.ORANGE_BANNER, // 9
            Material.RED_BANNER, // 10
            Material.PURPLE_BANNER, // Jack
            Material.PURPLE_BANNER, // Queen
            Material.YELLOW_BANNER, // King
            Material.GOLD_BLOCK // Ace
    };

    /**
     * Starts a higher/lower game for a player.
     * NOTE: The bet has already been processed in BettingMenu.startGame()
     *
     * @param player    The player
     * @param betAmount The amount bet
     */
    public static void startGame(Player player, int betAmount) {
        HigherLowerSession session = new HigherLowerSession(player.getUniqueId(), betAmount);
        // Draw initial card
        session.currentCard = drawCard();
        activeSessions.put(player.getUniqueId(), session);

        String title = ChatColorConverter.convert(GamblingConfig.getHigherLowerMenuTitle());
        Inventory inventory = Bukkit.createInventory(player, 27, title);

        updateDisplay(inventory, session);
        player.openInventory(inventory);
        HigherLowerMenuEvents.menus.add(inventory);
    }

    /**
     * Draws a random card (2-14).
     */
    private static int drawCard() {
        return ThreadLocalRandom.current().nextInt(2, 15); // 2 through 14 (Ace high)
    }

    /**
     * Gets the display name for a card value.
     */
    private static String getCardName(int value) {
        if (value < 2 || value > 14) return "?";
        return CARD_NAMES[value];
    }

    /**
     * Gets the material for displaying a card.
     */
    private static Material getCardMaterial(int value) {
        if (value < 2 || value > 14) return Material.GRAY_BANNER;
        return CARD_MATERIALS[value - 2];
    }

    /**
     * Updates the game display.
     */
    private static void updateDisplay(Inventory inventory, HigherLowerSession session) {
        // Current card
        ItemStack currentCard = ItemStackGenerator.generateItemStack(
                getCardMaterial(session.currentCard),
                ChatColorConverter.convert("&6&l" + getCardName(session.currentCard)),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Value: &f" + session.currentCard),
                        ChatColorConverter.convert("&7Is the next card higher or lower?")
                ),
                1
        );
        inventory.setItem(CURRENT_CARD_SLOT, currentCard);

        // Next card (hidden)
        ItemStack nextCard = ItemStackGenerator.generateItemStack(
                Material.GRAY_BANNER,
                ChatColorConverter.convert("&8&l???"),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Make your guess!")
                ),
                1
        );
        inventory.setItem(NEXT_CARD_SLOT, nextCard);

        // Higher button
        ItemStack higherButton = ItemStackGenerator.generateItemStack(
                Material.LIME_CONCRETE,
                ChatColorConverter.convert("&a&lHIGHER"),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Click if you think the next"),
                        ChatColorConverter.convert("&7card is &ahigher &7than " + getCardName(session.currentCard))
                ),
                1
        );
        inventory.setItem(HIGHER_SLOT, higherButton);

        // Cash out button
        double currentPayout = session.betAmount * session.multiplier;
        ItemStack cashOutButton;
        if (session.streak > 0) {
            cashOutButton = ItemStackGenerator.generateItemStack(
                    Material.GOLD_INGOT,
                    ChatColorConverter.convert("&e&lCASH OUT"),
                    List.of(
                            "",
                            ChatColorConverter.convert("&7Current Winnings: &a" + String.format("%.2f", currentPayout)),
                            ChatColorConverter.convert("&7Click to take your winnings!")
                    ),
                    1
            );
        } else {
            cashOutButton = ItemStackGenerator.generateItemStack(
                    Material.GRAY_STAINED_GLASS_PANE,
                    ChatColorConverter.convert("&7Cash Out"),
                    List.of(
                            "",
                            ChatColorConverter.convert("&7Win at least once to cash out!")
                    ),
                    1
            );
        }
        inventory.setItem(CASH_OUT_SLOT, cashOutButton);

        // Lower button
        ItemStack lowerButton = ItemStackGenerator.generateItemStack(
                Material.RED_CONCRETE,
                ChatColorConverter.convert("&c&lLOWER"),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Click if you think the next"),
                        ChatColorConverter.convert("&7card is &clower &7than " + getCardName(session.currentCard))
                ),
                1
        );
        inventory.setItem(LOWER_SLOT, lowerButton);

        // Streak display
        ItemStack streakDisplay = ItemStackGenerator.generateItemStack(
                Material.BLAZE_POWDER,
                ChatColorConverter.convert("&6Streak: " + session.streak),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Multiplier: &e" + String.format("%.2f", session.multiplier) + "x"),
                        ChatColorConverter.convert("&7Potential Win: &a" + String.format("%.2f", currentPayout))
                ),
                1
        );
        inventory.setItem(STREAK_SLOT, streakDisplay);

        // Bet info
        ItemStack betInfo = ItemStackGenerator.generateItemStack(
                Material.EMERALD,
                ChatColorConverter.convert("&aBet: " + session.betAmount),
                List.of(
                        "",
                        ChatColorConverter.convert("&7Each correct guess multiplies by &e" + GamblingConfig.getHigherLowerMultiplier() + "x")
                ),
                1
        );
        inventory.setItem(BET_INFO_SLOT, betInfo);
    }

    /**
     * Process the player's guess.
     * SAFETY-FIRST: Outcome determined BEFORE animation.
     *
     * @param player      The player
     * @param session     The game session
     * @param guessHigher True if player guessed higher
     * @param inventory   The game inventory
     */
    private static void processGuess(Player player, HigherLowerSession session, boolean guessHigher, Inventory inventory) {
        if (session.isProcessing || session.gameOver) return;
        session.isProcessing = true;

        // STEP 1: BACKEND FIRST - Draw next card and determine outcome
        int nextCard = drawCard();
        boolean isHigher = nextCard > session.currentCard;
        boolean isEqual = nextCard == session.currentCard;
        boolean playerWins = !isEqual && (guessHigher == isHigher);

        // Store for animation
        session.nextCard = nextCard;
        session.lastGuessCorrect = playerWins;

        // STEP 2: Process outcome BEFORE animation
        if (playerWins) {
            session.streak++;
            session.multiplier *= GamblingConfig.getHigherLowerMultiplier();
            // Don't award yet - player might continue
        } else {
            session.gameOver = true;
            // Player loses - bet was already taken, nothing more to do
        }

        // STEP 3: Disable buttons
        disableButtons(inventory);

        // STEP 4: Show card reveal animation
        revealCardAnimation(player, session, inventory, nextCard, playerWins);
    }

    /**
     * Handles player cashing out.
     */
    private static void cashOut(Player player, HigherLowerSession session, Inventory inventory) {
        if (session.streak < 1 || session.gameOver) return;

        session.gameOver = true;
        double payout = session.betAmount * session.multiplier;

        // Award winnings
        GamblingEconomyHandler.awardWinnings(player.getUniqueId(), payout);

        // Update display
        disableButtons(inventory);
        showCashOutResult(inventory, session, payout);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(ChatColorConverter.convert(
                GamblingConfig.getWinMessage().replace("%amount%", String.format("%.2f", payout))
        ));
    }

    /**
     * Disables all action buttons.
     */
    private static void disableButtons(Inventory inventory) {
        ItemStack disabled = ItemStackGenerator.generateItemStack(
                Material.GRAY_STAINED_GLASS_PANE,
                ChatColorConverter.convert("&7Wait..."),
                List.of(),
                1
        );
        inventory.setItem(HIGHER_SLOT, disabled);
        inventory.setItem(LOWER_SLOT, disabled);
        inventory.setItem(CASH_OUT_SLOT, disabled);
    }

    /**
     * Plays the card reveal animation.
     */
    private static void revealCardAnimation(Player player, HigherLowerSession session, Inventory inventory, int nextCard, boolean playerWins) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !HigherLowerMenuEvents.menus.contains(inventory)) {
                    cancel();
                    return;
                }

                ticks++;

                if (ticks <= 10) {
                    // Shuffle animation
                    int randomValue = ThreadLocalRandom.current().nextInt(2, 15);
                    ItemStack shuffledCard = ItemStackGenerator.generateItemStack(
                            getCardMaterial(randomValue),
                            ChatColorConverter.convert("&8" + getCardName(randomValue)),
                            List.of(ChatColorConverter.convert("&7Revealing...")),
                            1
                    );
                    inventory.setItem(NEXT_CARD_SLOT, shuffledCard);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f);
                } else if (ticks == 12) {
                    // Show actual card
                    ItemStack revealedCard = ItemStackGenerator.generateItemStack(
                            getCardMaterial(nextCard),
                            ChatColorConverter.convert("&6&l" + getCardName(nextCard)),
                            List.of(
                                    "",
                                    ChatColorConverter.convert("&7Value: &f" + nextCard)
                            ),
                            1
                    );
                    inventory.setItem(NEXT_CARD_SLOT, revealedCard);
                } else if (ticks == 20) {
                    // Show result
                    if (playerWins) {
                        showWinResult(player, session, inventory);
                    } else {
                        showLoseResult(player, session, inventory);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
    }

    /**
     * Shows win result and allows player to continue or cash out.
     */
    private static void showWinResult(Player player, HigherLowerSession session, Inventory inventory) {
        session.currentCard = session.nextCard;
        session.isProcessing = false;

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        player.sendMessage(ChatColorConverter.convert("&a[Casino] Correct! Streak: " + session.streak + " | Multiplier: " + String.format("%.2f", session.multiplier) + "x"));

        // Update display for next round
        updateDisplay(inventory, session);
    }

    /**
     * Shows lose result.
     */
    private static void showLoseResult(Player player, HigherLowerSession session, Inventory inventory) {
        // Update center card to show GAME OVER
        ItemStack gameOver = ItemStackGenerator.generateItemStack(
                Material.REDSTONE_BLOCK,
                ChatColorConverter.convert("&c&lGAME OVER"),
                List.of(
                        "",
                        ChatColorConverter.convert("&7You lost! The card was " + getCardName(session.nextCard)),
                        ChatColorConverter.convert("&7You lost &c" + session.betAmount + " coins"),
                        "",
                        ChatColorConverter.convert("&eClose to continue")
                ),
                1
        );
        inventory.setItem(BET_INFO_SLOT, gameOver);

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendMessage(ChatColorConverter.convert(
                GamblingConfig.getLoseMessage().replace("%amount%", String.valueOf(session.betAmount))
        ));
    }

    /**
     * Shows cash out result.
     */
    private static void showCashOutResult(Inventory inventory, HigherLowerSession session, double payout) {
        ItemStack cashOutResult = ItemStackGenerator.generateItemStack(
                Material.EMERALD_BLOCK,
                ChatColorConverter.convert("&a&lCASHED OUT!"),
                List.of(
                        "",
                        ChatColorConverter.convert("&7You cashed out with &a" + String.format("%.2f", payout) + " coins!"),
                        ChatColorConverter.convert("&7Streak: &e" + session.streak),
                        "",
                        ChatColorConverter.convert("&eClose to continue")
                ),
                1
        );
        inventory.setItem(BET_INFO_SLOT, cashOutResult);
    }

    public static void shutdown() {
        activeSessions.clear();
        HigherLowerMenuEvents.menus.clear();
    }

    /**
     * Stores game session data.
     */
    private static class HigherLowerSession {
        final UUID playerUUID;
        final int betAmount;
        int currentCard;
        int nextCard;
        int streak = 0;
        double multiplier = 1.0;
        boolean isProcessing = false;
        boolean lastGuessCorrect = false;
        boolean gameOver = false;

        HigherLowerSession(UUID playerUUID, int betAmount) {
            this.playerUUID = playerUUID;
            this.betAmount = betAmount;
        }
    }

    /**
     * Event handler for the higher/lower menu.
     */
    public static class HigherLowerMenuEvents implements Listener {
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

            HigherLowerSession session = activeSessions.get(player.getUniqueId());
            if (session == null) return;

            int slot = event.getSlot();

            if (slot == HIGHER_SLOT && !session.isProcessing && !session.gameOver) {
                processGuess(player, session, true, event.getInventory());
            } else if (slot == LOWER_SLOT && !session.isProcessing && !session.gameOver) {
                processGuess(player, session, false, event.getInventory());
            } else if (slot == CASH_OUT_SLOT && session.streak > 0 && !session.gameOver) {
                cashOut(player, session, event.getInventory());
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            menus.remove(event.getInventory());
            activeSessions.remove(event.getPlayer().getUniqueId());
        }
    }
}
