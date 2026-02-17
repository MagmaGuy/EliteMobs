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
 * Coin Flip gambling game.
 * Simple 50/50 chance - player picks heads or tails.
 * SAFETY: Outcome is determined and processed BEFORE any animation.
 */
public class CoinFlipGame {

    private static final Map<UUID, CoinFlipSession> activeSessions = new HashMap<>();

    // Slot constants
    private static final int COIN_SLOT = 4;
    private static final int HEADS_SLOT = 11;
    private static final int RESULT_SLOT = 13;
    private static final int TAILS_SLOT = 15;
    private static final int BET_INFO_SLOT = 22;

    /**
     * Starts a coin flip game for a player.
     * NOTE: The bet has already been processed in BettingMenu.startGame()
     *
     * @param player    The player
     * @param betAmount The amount bet
     */
    public static void startGame(Player player, int betAmount) {
        CoinFlipSession session = new CoinFlipSession(player.getUniqueId(), betAmount);
        activeSessions.put(player.getUniqueId(), session);

        String title = GamblingConfig.getCoinFlipMenuTitle();
        Inventory inventory = Bukkit.createInventory(player, 27, title);

        setupInitialDisplay(inventory, session);
        player.openInventory(inventory);
        CoinFlipMenuEvents.menus.add(inventory);
    }

    /**
     * Sets up the initial game display before player makes a choice.
     */
    private static void setupInitialDisplay(Inventory inventory, CoinFlipSession session) {
        // Coin display (animated later)
        ItemStack coin = ItemStackGenerator.generateItemStack(
                Material.GOLD_INGOT,
                GamblingConfig.getCoinFlipCoinTitle(),
                List.of(
                        "",
                        GamblingConfig.getCoinFlipChooseLore(),
                        GamblingConfig.getCoinFlipWillFlipLore()
                ),
                1
        );
        inventory.setItem(COIN_SLOT, coin);

        // Heads button
        ItemStack headsButton = ItemStackGenerator.generateItemStack(
                Material.PLAYER_HEAD,
                GamblingConfig.getCoinFlipHeadsButtonText(),
                List.of(
                        "",
                        GamblingConfig.getCoinFlipHeadsBetLore(),
                        ChatColorConverter.convert(GamblingConfig.getCoinFlipWinLore() + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipPayout()) + " " + GamblingConfig.getGamblingCurrencyWord())
                ),
                1
        );
        inventory.setItem(HEADS_SLOT, headsButton);

        // Result display (empty for now)
        ItemStack resultDisplay = ItemStackGenerator.generateItemStack(
                Material.GRAY_STAINED_GLASS_PANE,
                GamblingConfig.getCoinFlipMakeYourChoice(),
                List.of(),
                1
        );
        inventory.setItem(RESULT_SLOT, resultDisplay);

        // Tails button
        ItemStack tailsButton = ItemStackGenerator.generateItemStack(
                Material.SUNFLOWER,
                GamblingConfig.getCoinFlipTailsButtonText(),
                List.of(
                        "",
                        GamblingConfig.getCoinFlipTailsBetLore(),
                        ChatColorConverter.convert(GamblingConfig.getCoinFlipWinLore() + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipPayout()) + " " + GamblingConfig.getGamblingCurrencyWord())
                ),
                1
        );
        inventory.setItem(TAILS_SLOT, tailsButton);

        // Bet info
        ItemStack betInfo = ItemStackGenerator.generateItemStack(
                Material.EMERALD,
                GamblingConfig.getCoinFlipBetLabel() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord(),
                List.of(
                        "",
                        GamblingConfig.getCoinFlipNormalWinLabel() + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipPayout()),
                        GamblingConfig.getCoinFlipPayoutLabel() + GamblingConfig.getCoinFlipPayout() + "x",
                        "",
                        GamblingConfig.getCoinFlipEdgeChanceLore(),
                        ChatColorConverter.convert(GamblingConfig.getCoinFlipEdgePayoutLore() + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipEdgePayout()) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7)")
                ),
                1
        );
        inventory.setItem(BET_INFO_SLOT, betInfo);
    }

    /**
     * Process the player's coin flip choice.
     * SAFETY-FIRST: Outcome determined and winnings awarded BEFORE animation.
     *
     * @param player       The player
     * @param session      The game session
     * @param playerChoseHeads True if player chose heads
     * @param inventory    The game inventory
     */
    private static void processChoice(Player player, CoinFlipSession session, boolean playerChoseHeads, Inventory inventory) {
        if (session.hasChosen) return; // Prevent double-clicking
        session.hasChosen = true;

        // STEP 1: BACKEND FIRST - Determine outcome
        // Configurable chance to land on edge (bonus payout, player always wins!)
        double roll = ThreadLocalRandom.current().nextDouble();
        if (GamblingConfig.getCoinFlipEdgeChance() > 0 && roll < GamblingConfig.getCoinFlipEdgeChance()) {
            // EDGE! Super rare outcome
            session.landedOnEdge = true;
            session.playerWon = true;
            double payout = GamblingEconomyHandler.calculatePayout(session.betAmount, GamblingConfig.getCoinFlipEdgePayout());
            session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
        } else {
            // Normal flip
            boolean coinIsHeads = ThreadLocalRandom.current().nextBoolean();
            boolean playerWins = (coinIsHeads == playerChoseHeads);
            session.coinResult = coinIsHeads;
            session.playerWon = playerWins;

            // STEP 2: Resolve outcome BEFORE any animation
            if (playerWins) {
                double payout = GamblingEconomyHandler.calculatePayout(session.betAmount, GamblingConfig.getCoinFlipPayout());
                session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
            } else {
                GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), 0);
            }
        }

        // STEP 3: Disable choice buttons
        disableChoiceButtons(inventory);

        // STEP 4: NOW play the animation (safe to disconnect at any point)
        playCoinFlipAnimation(player, session, inventory);
    }

    /**
     * Disables the heads and tails buttons after a choice is made.
     */
    private static void disableChoiceButtons(Inventory inventory) {
        ItemStack disabled = ItemStackGenerator.generateItemStack(
                Material.GRAY_STAINED_GLASS_PANE,
                GamblingConfig.getCoinFlipFlipping(),
                List.of(),
                1
        );
        inventory.setItem(HEADS_SLOT, disabled);
        inventory.setItem(TAILS_SLOT, disabled);
    }

    /**
     * Plays the coin flip animation.
     */
    private static void playCoinFlipAnimation(Player player, CoinFlipSession session, Inventory inventory) {
        new BukkitRunnable() {
            final int totalTicks = 30; // 1.5 seconds of animation
            int ticks = 0;
            boolean showingHeads = true;

            @Override
            public void run() {
                if (!player.isOnline() || !CoinFlipMenuEvents.menus.contains(inventory)) {
                    cancel();
                    return;
                }

                // Flip animation
                if (ticks < totalTicks) {
                    // Alternate between heads and tails appearance
                    Material coinMaterial = showingHeads ? Material.PLAYER_HEAD : Material.SUNFLOWER;
                    String coinName = showingHeads ? GamblingConfig.getCoinFlipHeadsText() : GamblingConfig.getCoinFlipTailsText();

                    ItemStack animatedCoin = ItemStackGenerator.generateItemStack(
                            coinMaterial,
                            ChatColorConverter.convert(coinName),
                            List.of(GamblingConfig.getCoinFlipFlipping()),
                            1
                    );
                    inventory.setItem(COIN_SLOT, animatedCoin);

                    // Play flip sound
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f + (ticks / 20f));

                    showingHeads = !showingHeads;
                    ticks += 2; // Speed up near the end
                } else {
                    // Show final result
                    showResult(player, session, inventory);
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
    }

    /**
     * Shows the final result of the coin flip.
     */
    private static void showResult(Player player, CoinFlipSession session, Inventory inventory) {
        // Check for edge case first
        if (session.landedOnEdge) {
            // EDGE! Super rare outcome - special display
            ItemStack edgeCoin = ItemStackGenerator.generateItemStack(
                    Material.IRON_NUGGET,
                    GamblingConfig.getCoinFlipEdgeCoinTitle(),
                    List.of(GamblingConfig.getCoinFlipEdgeCoinLore()),
                    1
            );
            inventory.setItem(COIN_SLOT, edgeCoin);

            ItemStack edgeResult = ItemStackGenerator.generateItemStack(
                    Material.DIAMOND_BLOCK,
                    GamblingConfig.getCoinFlipEdgeResultTitle(),
                    List.of(
                            "",
                            GamblingConfig.getCoinFlipEdgeLandedLore(),
                            GamblingConfig.getCoinFlipEdgeRareLore(),
                            "",
                            ChatColorConverter.convert(GamblingConfig.getCoinFlipEdgeWonPrefix() + String.format("%.2f", session.winAmount) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7!"),
                            GamblingConfig.getCoinFlipEdgePayoutMultiplier(),
                            "",
                            GamblingConfig.getCoinFlipCloseToContinue()
                    ),
                    1
            );
            inventory.setItem(RESULT_SLOT, edgeResult);

            GamblingDisplay.playBigWinSound(player);
            player.sendMessage(
                    GamblingConfig.getCoinFlipEdgeChatMessage()
                            .replace("%amount%", String.format("%.2f", session.winAmount))
            );

            session.gameOver = true;
            return;
        }

        // Normal result - Update coin to show final result
        Material resultMaterial = session.coinResult ? Material.PLAYER_HEAD : Material.SUNFLOWER;
        String resultName = session.coinResult ? GamblingConfig.getCoinFlipHeadsText() : GamblingConfig.getCoinFlipTailsText();

        ItemStack finalCoin = ItemStackGenerator.generateItemStack(
                resultMaterial,
                ChatColorConverter.convert(resultName),
                List.of(GamblingConfig.getCoinFlipLandedOnLore() + (session.coinResult ? GamblingConfig.getCoinFlipHeadsWord() : GamblingConfig.getCoinFlipTailsWord()) + "!"),
                1
        );
        inventory.setItem(COIN_SLOT, finalCoin);

        // Show win/lose result
        if (session.playerWon) {
            ItemStack winResult = ItemStackGenerator.generateItemStack(
                    Material.EMERALD_BLOCK,
                    GamblingConfig.getCoinFlipYouWin(),
                    List.of(
                            "",
                            ChatColorConverter.convert(GamblingConfig.getCoinFlipWonPrefix() + String.format("%.2f", session.winAmount) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7!"),
                            "",
                            GamblingConfig.getCoinFlipCloseToContinue()
                    ),
                    1
            );
            inventory.setItem(RESULT_SLOT, winResult);

            GamblingDisplay.playWinSound(player);
            GamblingDisplay.sendWinMessage(player, session.winAmount, GamblingConfig.getBettingCoinFlipName());
        } else {
            ItemStack loseResult = ItemStackGenerator.generateItemStack(
                    Material.REDSTONE_BLOCK,
                    GamblingConfig.getCoinFlipYouLose(),
                    List.of(
                            "",
                            ChatColorConverter.convert(GamblingConfig.getCoinFlipLostPrefix() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord() + "&7."),
                            "",
                            GamblingConfig.getCoinFlipCloseToContinue()
                    ),
                    1
            );
            inventory.setItem(RESULT_SLOT, loseResult);

            GamblingDisplay.playLoseSound(player);
            GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingCoinFlipName());
        }

        session.gameOver = true;
    }

    public static void shutdown() {
        activeSessions.clear();
        CoinFlipMenuEvents.menus.clear();
    }

    /**
     * Stores game session data.
     */
    private static class CoinFlipSession extends GamblingSession {
        boolean hasChosen = false;
        boolean coinResult; // true = heads, false = tails
        boolean landedOnEdge = false; // 1% chance - 10x payout!
        boolean playerWon;
        double winAmount;
        boolean gameOver = false;

        CoinFlipSession(UUID playerUUID, int betAmount) {
            super(playerUUID, betAmount);
        }
    }

    /**
     * Event handler for the coin flip menu.
     */
    public static class CoinFlipMenuEvents implements Listener {
        static final Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            Player player = GamblingDisplay.validateClick(event, menus);
            if (player == null) return;

            CoinFlipSession session = activeSessions.get(player.getUniqueId());
            if (session == null || session.hasChosen) return;

            int slot = event.getSlot();

            if (slot == HEADS_SLOT) {
                processChoice(player, session, true, event.getInventory());
            } else if (slot == TAILS_SLOT) {
                processChoice(player, session, false, event.getInventory());
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (!menus.remove(event.getInventory())) return;
            CoinFlipSession session = activeSessions.remove(event.getPlayer().getUniqueId());
            if (session == null) return;
            // If player closed without choosing, forfeit the bet
            if (!session.hasChosen) {
                GamblingEconomyHandler.resolveOutcome(event.getPlayer().getUniqueId(), 0);
            }
        }
    }
}
