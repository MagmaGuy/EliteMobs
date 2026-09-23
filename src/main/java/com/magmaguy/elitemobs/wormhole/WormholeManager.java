package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.util.ChunkLocationChecker;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WormholeManager {
    // Minimum cooldown time in seconds
    private static final long COOLDOWN_DURATION_SECONDS = 5;
    // Distance for player-specific particle rendering
    private static final double PARTICLE_RENDER_DISTANCE = 30.0;
    // Distance at which a player is considered "away" from a wormhole and can use it again
    private static final double SAFE_DISTANCE = 2.0;
    // Distance for teleportation trigger
    private static final double TELEPORT_DISTANCE_MULTIPLIER = 1.5;
    private static WormholeManager instance;
    // Map to track players in cooldown with expiration timestamps
    @Getter
    // Concurrent map: mutated from listeners while the per-tick task copies its values; the plain
    // HashMap shared the same mid-copy ArrayIndexOutOfBoundsException risk as wormholeEntries.
    private final Map<UUID, PlayerWormholeData> playerTeleportData = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<UUID> activeTravel = new HashSet<>();
    // Retain uncertain economy outcomes across an EM reload; never replay an ambiguous mutation.
    private static final Map<UUID, TravelRecovery> unresolvedTravel = new HashMap<>();
    private record TravelRecovery(double amount, String status, String wormhole) { }
    private BukkitTask wormholeTask;
    private static final int TELEPORT_CHECK_INTERVAL = 5; // Check teleports every 5 ticks
    private int tickCounter = 0;

    // Constructor
    private WormholeManager() {
        startWormholeTask();
    }

    /**
     * Gets the singleton instance of the manager
     *
     * @return the WormholeManager instance
     */
    public static WormholeManager getInstance(boolean shuttingDown) {
        if (shuttingDown) return instance;
        if (instance == null) {
            instance = new WormholeManager();
        }
        if (instance.wormholeTask == null || instance.wormholeTask.isCancelled()) instance.startWormholeTask();
        return instance;
    }

    /**
     * Gets players that are near a specific wormhole (within 30 blocks).
     * Uses optimized distanceSquared check to avoid sqrt calculation.
     *
     * @param wormholeEntry The wormhole entry to check
     * @return List of players within render distance, empty list if none
     */
    private List<Player> getNearbyPlayers(WormholeEntry wormholeEntry) {
        List<Player> nearbyPlayers = new ArrayList<>();
        Location wormholeLocation = wormholeEntry.getLocation();

        if (wormholeLocation == null) return nearbyPlayers;
        try {
            if (wormholeLocation.getWorld() == null) return nearbyPlayers;
        } catch (IllegalArgumentException e) {
            return nearbyPlayers;
        }

        double maxDistanceSquared = PARTICLE_RENDER_DISTANCE * PARTICLE_RENDER_DISTANCE;

        for (org.bukkit.entity.Entity entity : wormholeLocation.getWorld().getNearbyEntities(
                wormholeLocation, PARTICLE_RENDER_DISTANCE, PARTICLE_RENDER_DISTANCE,
                PARTICLE_RENDER_DISTANCE, candidate -> candidate instanceof Player)) {
            Player player = (Player) entity;
            if (player.getWorld().equals(wormholeLocation.getWorld()) &&
                    player.getLocation().distanceSquared(wormholeLocation) <= maxDistanceSquared) {
                nearbyPlayers.add(player);
            }
        }

        return nearbyPlayers;
    }

    /**
     * Checks if any players should be teleported by this wormhole
     */
    private void checkForTeleports(WormholeEntry wormholeEntry, List<Player> nearbyPlayers) {
        double teleportDistanceSquared = Math.pow(TELEPORT_DISTANCE_MULTIPLIER * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier(), 2);
        for (Player player : nearbyPlayers) {
            if (player.getWorld() != wormholeEntry.getLocation().getWorld()
                    || player.getLocation().distanceSquared(wormholeEntry.getLocation()) > teleportDistanceSquared)
                continue;
            if (!canPlayerTeleport(wormholeEntry, player)) continue;
            teleportPlayer(wormholeEntry, player);
        }
    }

    /**
     * Checks if a player can teleport through a wormhole
     */
    private boolean canPlayerTeleport(WormholeEntry wormholeEntry, Player player) {
        UUID playerId = player.getUniqueId();
        if (activeTravel.contains(playerId) || unresolvedTravel.containsKey(playerId)) return false;
        PlayerWormholeData cooldown = playerTeleportData.get(playerId);
        if (cooldown != null && !cooldown.canTeleport()) return false;
        // Check permissions
        if (!PlayerQuestCooldowns.getBypassedPlayers().contains(player) &&
                wormholeEntry.getWormhole().getWormholeConfigFields().getPermission() != null &&
                !wormholeEntry.getWormhole().getWormholeConfigFields().getPermission().isEmpty() &&
                !player.hasPermission(wormholeEntry.getWormhole().getWormholeConfigFields().getPermission())) {
            return false;
        }

        return true;
    }

    /**
     * Teleports a player through a wormhole
     */
    private void teleportPlayer(WormholeEntry sourceEntry, Player player) {
        // Determine destination wormhole
        WormholeEntry destinationEntry;

        if (sourceEntry == sourceEntry.getWormhole().getWormholeEntry1()) {
            destinationEntry = sourceEntry.getWormhole().getWormholeEntry2();
        } else {
            destinationEntry = sourceEntry.getWormhole().getWormholeEntry1();
        }

        Location destination = destinationEntry.getLocation();

        // Check if destination is valid - Location.getWorld() throws on Paper when world is unloaded
        boolean destinationInvalid = destination == null;
        if (!destinationInvalid) {
            try {
                destinationInvalid = destination.getWorld() == null;
            } catch (IllegalArgumentException e) {
                destinationInvalid = true;
            }
        }
        if (destinationInvalid) {
            // Check destination entry for messages first (it's the broken side), then source entry
            String missingMessage = destinationEntry.getPortalMissingMessage();
            if (missingMessage == null) missingMessage = sourceEntry.getPortalMissingMessage();

            if (missingMessage == null) {
                player.sendMessage(WormholesConfig.getDefaultPortalMissingMessage());
            } else {
                player.sendMessage(missingMessage);
            }

            if (player.isOp() || player.hasPermission("elitemobs.*")) {
                // Check for download hint first (rich formatted message)
                boolean showDownload = destinationEntry.isShowDownloadHint() || sourceEntry.isShowDownloadHint();
                if (showDownload) {
                    sendDownloadHint(player);
                } else {
                    String opMsg = destinationEntry.getOpMessage();
                    if (opMsg == null) opMsg = sourceEntry.getOpMessage();
                    if (opMsg != null) Logger.sendSimpleMessage(player, opMsg);
                }
            }
            return;
        }

        UUID playerId = player.getUniqueId();
        if (!activeTravel.add(playerId)) return;
        double price = sourceEntry.getWormhole().getWormholeConfigFields().getCoinCost();
        if (!Double.isFinite(price) || price < 0) {
            activeTravel.remove(playerId);
            Logger.warn("Invalid wormhole price in " + sourceEntry.getWormhole().getWormholeConfigFields().getFilename());
            return;
        }
        Location target = destination.clone();
        boolean paid = false;
        boolean arrived = false;
        try {
            if (price > 0) {
                try {
                    paid = EconomyHandler.tryWithdraw(playerId, price);
                } catch (RuntimeException uncertain) {
                    retainRecovery(playerId, price, "withdrawal-unknown", sourceEntry, uncertain);
                    return;
                }
                if (!paid) {
                    player.sendMessage(WormholesConfig.getInsufficientCurrencyForWormholeMessage()
                            .replace("$amount", EconomyHandler.formatCurrency(price)));
                    return;
                }
            }
            boolean accepted = PlayerTeleportEvent.teleportPlayer(player, target);
            arrived = accepted && atDestination(player, target);
        } catch (RuntimeException failure) {
            // A post-teleport listener can fail after movement committed.
            arrived = atDestination(player, target);
            Logger.warn("Wormhole travel failed for " + playerId + ": " + failure);
        } finally {
            if (paid && !arrived) {
                try {
                    if (!EconomyHandler.refundPayment(playerId, price))
                        retainRecovery(playerId, price, "refund-rejected", sourceEntry, null);
                } catch (RuntimeException uncertain) {
                    retainRecovery(playerId, price, "refund-unknown", sourceEntry, uncertain);
                }
            }
            try {
                if (arrived) addPlayerToCooldown(player, destinationEntry);
            } finally {
                activeTravel.remove(playerId);
            }
        }
        if (!arrived) return;
        if (sourceEntry.getWormhole().getWormholeConfigFields().isBlindPlayer())
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 0));
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        player.setFlying(false);
    }

    private static boolean atDestination(Player player, Location destination) {
        Location actual = player.getLocation();
        return actual.getWorld() == destination.getWorld() && actual.distanceSquared(destination) < 1.0e-6;
    }

    private static void retainRecovery(UUID playerId, double amount, String status,
                                       WormholeEntry source, RuntimeException failure) {
        String filename = source.getWormhole().getWormholeConfigFields().getFilename();
        unresolvedTravel.put(playerId, new TravelRecovery(amount, status, filename));
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) Logger.sendMessage(player,
                "&cWormhole payment could not be settled. Contact an administrator before trying again.");
        Logger.warn("Wormhole payment requires manual reconciliation: player=" + playerId
                + ", amount=" + amount + ", status=" + status + ", wormhole=" + filename
                + (failure == null ? "" : ", failure=" + failure)
                + ". Further wormhole travel is blocked for this player; no automatic payment retry.");
    }

    private void sendDownloadHint(Player player) {
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(CommandMessagesConfig.getWormholeOpDownloadMessage()),
                SpigotMessage.commandHoverMessage(
                        InitializeConfig.getEmSetupDisplay(),
                        InitializeConfig.getEmSetupHover(),
                        "/em setup"),
                SpigotMessage.simpleMessage(" &8| "),
                SpigotMessage.hoverLinkMessage(
                        InitializeConfig.getDiscordLinkDisplay(),
                        InitializeConfig.getDiscordLinkHover(),
                        DiscordLinks.mainLink));
    }

    /**
     * @param player           The player to add to cooldowns
     * @param destinationEntry The wormhole they teleported to
     */
    public void addPlayerToCooldown(Player player, @NonNull WormholeEntry destinationEntry) {
        playerTeleportData.put(player.getUniqueId(), new PlayerWormholeData(player, destinationEntry, System.currentTimeMillis()));
    }

    public void addPlayerToCooldown(Player player, Location destination) {
        if (activeTravel.contains(player.getUniqueId())) return;
        WormholeEntry destinationEntry = null;
        for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
            try {
                if (wormholeEntry.getLocation() != null &&
                        wormholeEntry.getLocation().getWorld() != null &&
                        wormholeEntry.getLocation().getWorld().equals(destination.getWorld()) &&
                        destination.distanceSquared(wormholeEntry.getLocation()) <= Math.pow(TELEPORT_DISTANCE_MULTIPLIER * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier(), 2)) {
                    destinationEntry = wormholeEntry;
                    break;
                }
            } catch (IllegalArgumentException e) {
                // World unloaded, skip this entry
            }
        }
        if (destinationEntry == null) return;
        addPlayerToCooldown(player, destinationEntry);
    }

    /**
     * Shuts down the manager and cleans up all resources
     */
    public void shutdown() {
        // Cancel the task first to stop processing
        if (wormholeTask != null) {
            wormholeTask.cancel();
            wormholeTask = null;
        }

        // Clean up all wormhole entries (clear lines and text displays)
        for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
            if (wormholeEntry != null) {
                wormholeEntry.stop();
            }
        }

        // Clear player teleport data
        playerTeleportData.clear();

        // Reset singleton to allow clean restart
        instance = null;
        unresolvedTravel.forEach((playerId, recovery) -> Logger.warn(
                "Unresolved wormhole payment at shutdown: " + playerId + " " + recovery));
    }

    /**
     * Starts the main task for processing wormholes
     */
    private void startWormholeTask() {
        wormholeTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Process all wormholes in a single task
                processWormholes();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1); // Run every tick for smooth 20 TPS animation
    }

    /**
     * Main method to process all wormholes
     */
    private void processWormholes() {
        tickCounter++;

        // Tick player cooldowns (copy to avoid ConcurrentModificationException)
        for (PlayerWormholeData value : new java.util.ArrayList<>(playerTeleportData.values())) {
            value.tick();
        }

        // Tick all wormhole entries with distance-based culling
        for (WormholeEntry wormholeEntry : new java.util.ArrayList<>(WormholeEntry.getWormholeEntries())) {
            // Skip if chunk isn't loaded - Location.getWorld() throws IllegalArgumentException on Paper when world is unloaded
            if (wormholeEntry.getLocation() == null) continue;
            try {
                if (wormholeEntry.getLocation().getWorld() == null ||
                    !ChunkLocationChecker.chunkAtLocationIsLoaded(wormholeEntry.getLocation())) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }

            // Get nearby players (within 30 blocks) - skip everything if none,
            // but tear down any lingering line visuals first so they can't
            // accumulate as orphans across the away-period.
            List<Player> nearbyPlayers = getNearbyPlayers(wormholeEntry);
            if (nearbyPlayers.isEmpty()) {
                wormholeEntry.onNoNearbyPlayers();
                continue;
            }

            // Tick visuals with nearby players list (for LOS culling)
            wormholeEntry.tick(nearbyPlayers);

            // Only process teleports every 5 ticks
            if (tickCounter % TELEPORT_CHECK_INTERVAL == 0) {
                checkForTeleports(wormholeEntry, nearbyPlayers);
            }
        }
    }

    private class PlayerWormholeData {
        private final Player player;
        private final Location destination;
        private final long timeStamp;
        private final WormholeEntry wormholeEntry;
        private boolean hasLeftTeleportRadius = false;

        public PlayerWormholeData(Player player, WormholeEntry destinationWormhole, long timeStamp) {
            this.player = player;
            this.destination = destinationWormhole.getLocation().clone();
            this.timeStamp = timeStamp;
            this.wormholeEntry = destinationWormhole;
        }

        public boolean canTeleport() {
            return timeStamp + COOLDOWN_DURATION_SECONDS * 1000 < System.currentTimeMillis() &&
                    isHasLeftTeleportRadius();
        }

        //Has to run on the tick to see the distance. Should be efficient.
        public void tick() {
            if (isHasLeftTeleportRadius() && enoughTimeHasPassed()) playerTeleportData.remove(player.getUniqueId(), this);
        }

        private boolean enoughTimeHasPassed() {
            return timeStamp + COOLDOWN_DURATION_SECONDS * 1000 < System.currentTimeMillis();
        }

        private boolean isHasLeftTeleportRadius() {
            if (hasLeftTeleportRadius) return true;
            try {
                if (!player.getWorld().equals(destination.getWorld())) return hasLeftTeleportRadius = true;
            } catch (IllegalArgumentException e) {
                return hasLeftTeleportRadius = true;
            }
            if (destination.distanceSquared(player.getLocation()) > Math.pow(TELEPORT_DISTANCE_MULTIPLIER * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier() + SAFE_DISTANCE, 2))
                return hasLeftTeleportRadius = true;
            return false;
        }
    }
}
