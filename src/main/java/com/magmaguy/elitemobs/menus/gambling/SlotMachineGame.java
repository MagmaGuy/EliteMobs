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
 * Slot Machine gambling game.
 * Match 3 symbols to win big, 2 symbols for a small return.
 * SAFETY: Outcome is determined and processed BEFORE any animation.
 */
public class SlotMachineGame {

    private static final Map<UUID, SlotSession> activeSessions = new HashMap<>();

    // Slot constants - 3x3 grid display
    private static final int REEL_1_TOP = 3;
    private static final int REEL_1_MID = 12;
    private static final int REEL_1_BOT = 21;
    private static final int REEL_2_TOP = 4;
    private static final int REEL_2_MID = 13;
    private static final int REEL_2_BOT = 22;
    private static final int REEL_3_TOP = 5;
    private static final int REEL_3_MID = 14;
    private static final int REEL_3_BOT = 23;
    private static final int SPIN_BUTTON_SLOT = 31;
    private static final int BET_INFO_SLOT = 27;
    private static final int SPIN_AGAIN_SLOT = 30;
    private static final int PAYOUT_INFO_SLOT = 35;

    // Payline indicator slots
    private static final int PAYLINE_LEFT = 11;
    private static final int PAYLINE_RIGHT = 15;
    private static final int TOTAL_WEIGHT;

    static {
        int total = 0;
        for (Symbol s : Symbol.values()) {
            total += s.weight;
        }
        TOTAL_WEIGHT = total; // = 100
    }

    /**
     * Starts a slot machine game for a player.
     * NOTE: The bet has already been processed in BettingMenu.startGame()
     *
     * @param player    The player
     * @param betAmount The amount bet
     */
    public static void startGame(Player player, int betAmount) {
        SlotSession session = new SlotSession(player.getUniqueId(), betAmount);
        activeSessions.put(player.getUniqueId(), session);

        String title = GamblingConfig.getSlotsMenuTitle();
        Inventory inventory = Bukkit.createInventory(player, 36, title);

        setupInitialDisplay(inventory, session);
        player.openInventory(inventory);
        SlotMachineMenuEvents.menus.add(inventory);
    }

    /**
     * Gets a weighted random symbol.
     */
    private static Symbol getWeightedRandomSymbol() {
        int roll = ThreadLocalRandom.current().nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (Symbol s : Symbol.values()) {
            cumulative += s.weight;
            if (roll < cumulative) {
                return s;
            }
        }
        return Symbol.CHERRY; // Fallback
    }

    /**
     * Sets up the initial slot machine display.
     */
    private static void setupInitialDisplay(Inventory inventory, SlotSession session) {
        // Fill background
        ItemStack glass = ItemStackGenerator.generateItemStack(
                Material.BLACK_STAINED_GLASS_PANE,
                " ",
                List.of(),
                1
        );
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, glass);
        }

        // Payline indicators
        ItemStack paylineIndicator = ItemStackGenerator.generateItemStack(
                Material.YELLOW_STAINED_GLASS_PANE,
                GamblingConfig.getSlotsPaylineLabel(),
                List.of(),
                1
        );
        inventory.setItem(PAYLINE_LEFT, paylineIndicator);
        inventory.setItem(PAYLINE_RIGHT, paylineIndicator);

        // Initial reel display (random static symbols)
        updateReelDisplay(inventory, REEL_1_TOP, REEL_1_MID, REEL_1_BOT, getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol());
        updateReelDisplay(inventory, REEL_2_TOP, REEL_2_MID, REEL_2_BOT, getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol());
        updateReelDisplay(inventory, REEL_3_TOP, REEL_3_MID, REEL_3_BOT, getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol());

        // Spin button
        ItemStack spinButton = ItemStackGenerator.generateItemStack(
                Material.LIME_CONCRETE,
                GamblingConfig.getSlotsSpinButtonText(),
                List.of(
                        "",
                        GamblingConfig.getSlotsSpinClickLore(),
                        GamblingConfig.getSlotsSpinBetLore() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord()
                ),
                1
        );
        inventory.setItem(SPIN_BUTTON_SLOT, spinButton);

        // Bet info
        ItemStack betInfo = ItemStackGenerator.generateItemStack(
                Material.EMERALD,
                GamblingConfig.getSlotsBetLabel() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord(),
                List.of(
                        "",
                        GamblingConfig.getSlotsMatch3Lore(),
                        GamblingConfig.getSlotsMatch2Lore()
                ),
                1
        );
        inventory.setItem(BET_INFO_SLOT, betInfo);

        // Payout info - shows rarity-based payouts
        List<String> payoutLore = new ArrayList<>();
        payoutLore.add("");
        payoutLore.add(GamblingConfig.getSlotsPayoutsTitle());
        payoutLore.add("");
        for (Symbol symbol : Symbol.values()) {
            String rarity = symbol.getChancePercent() >= 20 ? "&7" :
                           symbol.getChancePercent() >= 10 ? "&e" :
                           symbol.getChancePercent() >= 5 ? "&6" : "&b";
            String multiplier = String.format("%.1f", symbol.getPayoutMultiplier());
            String twoMatch = String.format("%.2f", symbol.getPayoutMultiplier() * 0.25);
            payoutLore.add(ChatColorConverter.convert(GamblingConfig.getSlotsPayoutSymbolFormat().replace("$symbol", symbol.displayName).replace("$multiplier", multiplier).replace("$twoMatch", twoMatch)));
        }
        payoutLore.add("");
        payoutLore.add(GamblingConfig.getSlotsChanceRarityNote());

        ItemStack payoutInfo = ItemStackGenerator.generateItemStack(
                Material.BOOK,
                GamblingConfig.getSlotsPayoutTableTitle(),
                payoutLore,
                1
        );
        inventory.setItem(PAYOUT_INFO_SLOT, payoutInfo);
    }

    /**
     * Updates the display for a single reel column.
     */
    private static void updateReelDisplay(Inventory inventory, int topSlot, int midSlot, int botSlot,
                                          Symbol top, Symbol mid, Symbol bot) {
        inventory.setItem(topSlot, createGreyedSymbol(top));
        inventory.setItem(midSlot, mid.createItemStack());
        inventory.setItem(botSlot, createGreyedSymbol(bot));
    }

    /**
     * Creates a greyed-out version of a symbol for non-payline positions.
     */
    private static ItemStack createGreyedSymbol(Symbol symbol) {
        return ItemStackGenerator.generateItemStack(
                symbol.material,
                ChatColorConverter.convert("&8" + symbol.displayName.substring(2)), // Remove color code
                List.of(),
                1
        );
    }

    /**
     * Process the spin action.
     * SAFETY-FIRST: Outcome determined and payout awarded BEFORE animation.
     *
     * @param player    The player
     * @param session   The game session
     * @param inventory The game inventory
     */
    private static void processSpin(Player player, SlotSession session, Inventory inventory) {
        if (session.isSpinning || session.gameOver) return;
        session.isSpinning = true;

        // STEP 1: BACKEND FIRST - Determine all reel results
        Symbol[] results = {
                getWeightedRandomSymbol(),
                getWeightedRandomSymbol(),
                getWeightedRandomSymbol()
        };
        session.results = results;

        // STEP 2: Calculate payout BEFORE animation
        double payout = calculatePayout(results, session.betAmount);
        session.winAmount = payout;
        session.playerWon = payout > 0;

        // STEP 3: Resolve outcome BEFORE animation (safety-first)
        session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);

        // STEP 4: Disable spin button
        disableSpinButton(inventory);

        // STEP 5: NOW play the animation (safe to disconnect at any point)
        playSpinAnimation(player, session, inventory, results, payout);
    }

    /**
     * Calculates the payout based on the reel results.
     * Payout is proportional to rarity: rarer symbols pay more.
     */
    private static double calculatePayout(Symbol[] results, int betAmount) {
        // Check for 3 matching - full payout based on rarity
        if (results[0] == results[1] && results[1] == results[2]) {
            return betAmount * results[0].getPayoutMultiplier();
        }

        // Check for 2 matching - partial payout (25% of what 3-match would be)
        // Use the rarer of the two matched symbols for payout calculation
        Symbol matchedSymbol = null;
        if (results[0] == results[1]) {
            matchedSymbol = results[0];
        } else if (results[1] == results[2]) {
            matchedSymbol = results[1];
        } else if (results[0] == results[2]) {
            matchedSymbol = results[0];
        }

        if (matchedSymbol != null) {
            // 2-match pays 25% of the 3-match payout for that symbol
            return betAmount * matchedSymbol.getPayoutMultiplier() * 0.25;
        }

        // No match
        return 0;
    }

    /**
     * Disables the spin button during animation.
     */
    private static void disableSpinButton(Inventory inventory) {
        ItemStack disabled = ItemStackGenerator.generateItemStack(
                Material.GRAY_CONCRETE,
                GamblingConfig.getSlotsSpinning(),
                List.of(),
                1
        );
        inventory.setItem(SPIN_BUTTON_SLOT, disabled);
    }

    /**
     * Plays the slot machine spin animation.
     */
    private static void playSpinAnimation(Player player, SlotSession session, Inventory inventory,
                                          Symbol[] finalResults, double payout) {
        new BukkitRunnable() {
            final int REEL_1_STOP = 15;
            final int REEL_2_STOP = 25;
            final int REEL_3_STOP = 35;
            final int ANIMATION_END = 45;
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !SlotMachineMenuEvents.menus.contains(inventory)) {
                    cancel();
                    return;
                }

                ticks++;

                // Reel 1 animation (stops first)
                if (ticks <= REEL_1_STOP) {
                    Symbol top = getWeightedRandomSymbol();
                    Symbol mid = getWeightedRandomSymbol();
                    Symbol bot = getWeightedRandomSymbol();
                    updateReelDisplay(inventory, REEL_1_TOP, REEL_1_MID, REEL_1_BOT, top, mid, bot);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.0f);
                } else if (ticks == REEL_1_STOP + 1) {
                    // Lock in reel 1
                    Symbol above = getWeightedRandomSymbol();
                    Symbol below = getWeightedRandomSymbol();
                    updateReelDisplay(inventory, REEL_1_TOP, REEL_1_MID, REEL_1_BOT, above, finalResults[0], below);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
                }

                // Reel 2 animation (stops second)
                if (ticks <= REEL_2_STOP && ticks > 5) {
                    Symbol top = getWeightedRandomSymbol();
                    Symbol mid = getWeightedRandomSymbol();
                    Symbol bot = getWeightedRandomSymbol();
                    updateReelDisplay(inventory, REEL_2_TOP, REEL_2_MID, REEL_2_BOT, top, mid, bot);
                } else if (ticks == REEL_2_STOP + 1) {
                    // Lock in reel 2
                    Symbol above = getWeightedRandomSymbol();
                    Symbol below = getWeightedRandomSymbol();
                    updateReelDisplay(inventory, REEL_2_TOP, REEL_2_MID, REEL_2_BOT, above, finalResults[1], below);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
                }

                // Reel 3 animation (stops last)
                if (ticks <= REEL_3_STOP && ticks > 10) {
                    Symbol top = getWeightedRandomSymbol();
                    Symbol mid = getWeightedRandomSymbol();
                    Symbol bot = getWeightedRandomSymbol();
                    updateReelDisplay(inventory, REEL_3_TOP, REEL_3_MID, REEL_3_BOT, top, mid, bot);
                } else if (ticks == REEL_3_STOP + 1) {
                    // Lock in reel 3
                    Symbol above = getWeightedRandomSymbol();
                    Symbol below = getWeightedRandomSymbol();
                    updateReelDisplay(inventory, REEL_3_TOP, REEL_3_MID, REEL_3_BOT, above, finalResults[2], below);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
                }

                // Show result
                if (ticks >= ANIMATION_END) {
                    showResult(player, session, inventory, payout);
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
    }

    /**
     * Shows the final result of the spin.
     */
    private static void showResult(Player player, SlotSession session, Inventory inventory, double payout) {
        session.gameOver = true;

        if (payout > 0) {
            // Win!
            boolean isJackpot = session.results[0] == Symbol.SEVEN &&
                    session.results[1] == Symbol.SEVEN &&
                    session.results[2] == Symbol.SEVEN;
            boolean isThreeMatch = session.results[0] == session.results[1] &&
                    session.results[1] == session.results[2];

            String resultTitle;
            Material resultMaterial;
            Sound resultSound;
            float pitch;

            if (isJackpot) {
                resultTitle = GamblingConfig.getSlotsJackpotTitle();
                resultMaterial = Material.DIAMOND_BLOCK;
                resultSound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
                pitch = 1.0f;
            } else if (isThreeMatch) {
                resultTitle = GamblingConfig.getSlotsBigWinTitle();
                resultMaterial = Material.EMERALD_BLOCK;
                resultSound = Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 1.2f;
            } else {
                resultTitle = GamblingConfig.getSlotsSmallWinTitle();
                resultMaterial = Material.GOLD_BLOCK;
                resultSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                pitch = 1.5f;
            }

            ItemStack resultItem = ItemStackGenerator.generateItemStack(
                    resultMaterial,
                    resultTitle,
                    List.of(
                            "",
                            ChatColorConverter.convert(GamblingConfig.getSlotsWonPrefix() + String.format("%.2f", payout) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7!"),
                            "",
                            GamblingConfig.getSlotsCloseToContinue()
                    ),
                    1
            );
            inventory.setItem(SPIN_BUTTON_SLOT, resultItem);

            player.playSound(player.getLocation(), resultSound, 1.0f, pitch);
            GamblingDisplay.sendWinMessage(player, payout, GamblingConfig.getBettingSlotsName());
        } else {
            // Loss
            ItemStack loseItem = ItemStackGenerator.generateItemStack(
                    Material.REDSTONE_BLOCK,
                    GamblingConfig.getSlotsNoMatchTitle(),
                    List.of(
                            "",
                            ChatColorConverter.convert(GamblingConfig.getSlotsLostPrefix() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord() + "&7."),
                            "",
                            GamblingConfig.getSlotsCloseToContinue()
                    ),
                    1
            );
            inventory.setItem(SPIN_BUTTON_SLOT, loseItem);

            GamblingDisplay.playLoseSound(player);
            GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingSlotsName());
        }

        // Add "Spin Again" button if the player can afford another bet
        if (GamblingEconomyHandler.canAffordBet(player.getUniqueId(), session.betAmount)) {
            ItemStack spinAgainButton = ItemStackGenerator.generateItemStack(
                    Material.LIME_CONCRETE,
                    ChatColorConverter.convert("&a&lSPIN AGAIN"),
                    List.of(
                            "",
                            ChatColorConverter.convert("&7Click to spin again!"),
                            ChatColorConverter.convert("&7Bet: &6" + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord())
                    ),
                    1
            );
            inventory.setItem(SPIN_AGAIN_SLOT, spinAgainButton);
        }
    }

    public static void shutdown() {
        activeSessions.clear();
        SlotMachineMenuEvents.menus.clear();
    }

    /**
     * Slot machine symbols with their materials and weights (rarity).
     * Payout is calculated as: TOTAL_WEIGHT / weight (rarer = higher payout)
     */
    private enum Symbol {
        CHERRY(Material.SWEET_BERRIES, "&cCherry", 35),      // Most common - 2.86x
        LEMON(Material.MELON_SLICE, "&eLemon", 28),          // Common - 3.57x
        ORANGE(Material.PUMPKIN, "&6Orange", 20),            // Uncommon - 5x
        BELL(Material.BELL, "&eGolden Bell", 10),            // Rare - 10x
        BAR(Material.IRON_INGOT, "&fSilver Bar", 5),         // Very Rare - 20x
        SEVEN(Material.DIAMOND, "&b&lLucky 7", 2);           // Legendary - 50x JACKPOT!

        private final Material material;
        private final String displayName;
        private final int weight;

        Symbol(Material material, String displayName, int weight) {
            this.material = material;
            this.displayName = displayName;
            this.weight = weight;
        }

        /**
         * Gets the payout multiplier based on rarity.
         * Rarer symbols (lower weight) have higher payouts.
         */
        public double getPayoutMultiplier() {
            return (double) TOTAL_WEIGHT / weight;
        }

        /**
         * Gets the chance percentage for this symbol.
         */
        public double getChancePercent() {
            return (weight * 100.0) / TOTAL_WEIGHT;
        }

        public ItemStack createItemStack() {
            return ItemStackGenerator.generateItemStack(
                    material,
                    ChatColorConverter.convert(displayName),
                    List.of(),
                    1
            );
        }
    }

    /**
     * Stores game session data.
     */
    private static class SlotSession extends GamblingSession {
        Symbol[] results;
        boolean isSpinning = false;
        boolean playerWon = false;
        double winAmount = 0;
        boolean gameOver = false;

        SlotSession(UUID playerUUID, int betAmount) {
            super(playerUUID, betAmount);
        }
    }

    /**
     * Event handler for the slot machine menu.
     */
    public static class SlotMachineMenuEvents implements Listener {
        static final Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            Player player = GamblingDisplay.validateClick(event, menus);
            if (player == null) return;

            SlotSession session = activeSessions.get(player.getUniqueId());
            if (session == null || session.isSpinning) return;

            if (event.getSlot() == SPIN_BUTTON_SLOT && !session.gameOver) {
                processSpin(player, session, event.getInventory());
            } else if (event.getSlot() == SPIN_AGAIN_SLOT && session.gameOver) {
                if (!GamblingEconomyHandler.canAffordBet(player.getUniqueId(), session.betAmount)) {
                    player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
                    return;
                }
                if (!GamblingEconomyHandler.placeBet(player.getUniqueId(), session.betAmount)) {
                    player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
                    return;
                }
                session.gameOver = false;
                session.isSpinning = false;
                session.results = null;
                session.playerWon = false;
                session.winAmount = 0;
                setupInitialDisplay(event.getInventory(), session);
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (!menus.remove(event.getInventory())) return;
            SlotSession session = activeSessions.remove(event.getPlayer().getUniqueId());
            if (session == null) return;
            // If player closed without spinning, forfeit the bet
            if (!session.isSpinning && !session.gameOver) {
                GamblingEconomyHandler.resolveOutcome(event.getPlayer().getUniqueId(), 0);
            }
        }
    }
}
