package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.magmacore.util.ChatColorConverter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized manager for all wormholes in the game.
 * Handles visual effects, teleportation, and player cooldowns.
 */
public class WormholeManager {
    // Static cooldown time in seconds (20 seconds)
    private static final long COOLDOWN_DURATION_SECONDS = 20;
    // Distance for player-specific particle rendering
    private static final double PARTICLE_RENDER_DISTANCE = 30.0;
    // Distance at which a player is considered "away" from a wormhole and can use it again
    private static final double SAFE_DISTANCE = 3.0;
    // Distance for teleportation trigger
    private static final double TELEPORT_DISTANCE_MULTIPLIER = 1.5;
    private static WormholeManager instance;
    // Map to track players in cooldown with expiration timestamps
    @Getter
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    // Map to track which destination wormhole a player teleported to
    private final Map<UUID, WormholeEntry> playerDestinations = new HashMap<>();
    // Maps to track rotation counters for each wormhole
    private final Map<WormholeEntry, Integer> rotationCounters = new HashMap<>();
    private BukkitTask wormholeTask;

    // Constructor
    private WormholeManager() {
        startWormholeTask();
    }

    /**
     * Gets the singleton instance of the manager
     * @return the WormholeManager instance
     */
    public static WormholeManager getInstance() {
        if (instance == null) {
            instance = new WormholeManager();
        }
        return instance;
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

                // Check cooldowns and safe distances
                processPlayerCooldowns();
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 0, 5);
    }

    /**
     * Main method to process all wormholes
     */
    private void processWormholes() {
        // Get online players once per tick
        HashSet<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());

        // Iterate through all wormhole entries
        for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
            // Skip if location is null or chunk is not loaded
            if (wormholeEntry.getLocation() == null ||
                    !ChunkLocationChecker.locationIsLoaded(wormholeEntry.getLocation())) {
                continue;
            }

            // Initialize text display if needed
            if (wormholeEntry.getText() == null || !wormholeEntry.getText().isValid()) {
                Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                    if (wormholeEntry.getLocation() != null && wormholeEntry.getLocation().getWorld() != null) {
                        wormholeEntry.initializeTextDisplay();
                    }
                });
            }

            // Get nearby players for this wormhole
            HashSet<Player> nearbyPlayers = getNearbyPlayers(wormholeEntry, onlinePlayers);
            if (nearbyPlayers.isEmpty()) continue;

            // Skip visual effects if particles are disabled
            if (!WormholesConfig.isNoParticlesMode() &&
                    wormholeEntry.getWormhole().getWormholeConfigFields().getStyle() != Wormhole.WormholeStyle.NONE) {
                // Display visual effects to nearby players
                displayVisualEffects(wormholeEntry, nearbyPlayers);
            }

            // Check for players who should teleport
            checkForTeleports(wormholeEntry, nearbyPlayers);
        }
    }

    /**
     * Check if players have moved away from their destination wormhole
     * and remove their cooldown if they have
     */
    private void processPlayerCooldowns() {
        long currentTime = System.currentTimeMillis();
        HashSet<UUID> toRemove = new HashSet<>();

        // Process all players in cooldown
        for (Map.Entry<UUID, Long> entry : playerCooldowns.entrySet()) {
            UUID playerUUID = entry.getKey();
            Long expirationTime = entry.getValue();

            // Check if cooldown has expired by time
            if (currentTime > expirationTime) {
                toRemove.add(playerUUID);
                playerDestinations.remove(playerUUID);
                continue;
            }

            // Get the player's destination wormhole
            WormholeEntry destinationEntry = playerDestinations.get(playerUUID);
            if (destinationEntry == null) {
                continue; // No destination tracked, keep the time-based cooldown
            }

            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue; // Player offline, keep the cooldown
            }

            // Check if player has moved away from destination wormhole
            if (destinationEntry.getLocation() != null &&
                    destinationEntry.getLocation().getWorld() != null &&
                    player.getWorld().equals(destinationEntry.getLocation().getWorld()) &&
                    player.getLocation().distance(destinationEntry.getLocation()) > SAFE_DISTANCE) {
                // Player is now far enough away, clear cooldown
                toRemove.add(playerUUID);
                playerDestinations.remove(playerUUID);
            }
        }

        // Remove all players marked for cooldown removal
        for (UUID uuid : toRemove) {
            playerCooldowns.remove(uuid);
        }
    }

    /**
     * Gets players that are near a specific wormhole
     */
    private HashSet<Player> getNearbyPlayers(WormholeEntry wormholeEntry, HashSet<Player> onlinePlayers) {
        HashSet<Player> nearbyPlayers = new HashSet<>();
        Location wormholeLocation = wormholeEntry.getLocation();

        if (wormholeLocation == null || wormholeLocation.getWorld() == null) {
            return nearbyPlayers;
        }

        for (Player player : onlinePlayers) {
            if (player.getWorld().equals(wormholeLocation.getWorld()) &&
                    player.getLocation().distance(wormholeLocation) <= PARTICLE_RENDER_DISTANCE) {
                nearbyPlayers.add(player);
            }
        }

        return nearbyPlayers;
    }

    /**
     * Displays the visual effects for a wormhole
     */
    private void displayVisualEffects(WormholeEntry wormholeEntry, HashSet<Player> nearbyPlayers) {
        // Get or initialize rotation counter for this wormhole
        int rotationIndex = rotationCounters.getOrDefault(wormholeEntry, 0);

        // Reset if we've gone through all rotations
        if (rotationIndex >= wormholeEntry.getWormhole().getCachedRotations().size()) {
            rotationIndex = 0;
        }

        // Apply reduced particles mode if enabled
        if (WormholesConfig.isReducedParticlesMode() && rotationIndex % 2 != 0) {
            rotationCounters.put(wormholeEntry, rotationIndex + 1);
            return;
        }

        // Get the vectors for the current frame
        if (wormholeEntry.getWormhole().getCachedRotations().isEmpty()) return;

        // Spawn particles for each nearby player
        for (Player player : nearbyPlayers) {
            for (Vector vector : wormholeEntry.getWormhole().getCachedRotations().get(rotationIndex)) {
                Location particleLocation = wormholeEntry.getLocation().clone().add(vector);
                player.spawnParticle(
                        Particle.DUST,
                        particleLocation,
                        1, 0, 0, 0, 0,
                        new Particle.DustOptions(wormholeEntry.getWormhole().getParticleColor(), 1)
                );
            }
        }

        // Update the rotation counter
        rotationCounters.put(wormholeEntry, rotationIndex + 1);
    }

    /**
     * Checks if any players should be teleported by this wormhole
     */
    private void checkForTeleports(WormholeEntry wormholeEntry, HashSet<Player> nearbyPlayers) {
        double teleportDistance = TELEPORT_DISTANCE_MULTIPLIER *
                wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier();

        for (Player player : nearbyPlayers) {
            // Skip players in cooldown
            if (isPlayerInCooldown(player)) {
                continue;
            }

            // Check if player is within teleport range
            if (player.getLocation().distance(wormholeEntry.getLocation()) <= teleportDistance) {
                // Check if player can teleport
                if (canPlayerTeleport(wormholeEntry, player)) {
                    teleportPlayer(wormholeEntry, player);
                }
            }
        }
    }

    /**
     * Checks if a player can teleport through a wormhole
     */
    private boolean canPlayerTeleport(WormholeEntry wormholeEntry, Player player) {
        // Check permissions
        if (!PlayerQuestCooldowns.getBypassedPlayers().contains(player) &&
                wormholeEntry.getWormhole().getWormholeConfigFields().getPermission() != null &&
                !wormholeEntry.getWormhole().getWormholeConfigFields().getPermission().isEmpty() &&
                !player.hasPermission(wormholeEntry.getWormhole().getWormholeConfigFields().getPermission())) {
            return false;
        }

        // Check currency
        if (wormholeEntry.getWormhole().getWormholeConfigFields().getCoinCost() > 0) {
            double coinCost = wormholeEntry.getWormhole().getWormholeConfigFields().getCoinCost() +
                    wormholeEntry.getWormhole().getWormholeConfigFields().getCoinCost() *
                            GuildRank.currencyBonusMultiplier(player.getUniqueId());

            if (EconomyHandler.checkCurrency(player.getUniqueId()) < coinCost) {
                player.sendMessage(ChatColorConverter.convert(WormholesConfig.getInsufficientCurrencyForWormholeMessage())
                        .replace("$amount", "" + coinCost));
                return false;
            }

            EconomyHandler.subtractCurrency(player.getUniqueId(), coinCost);
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

        // Check if destination is valid
        if (destination == null || destination.getWorld() == null) {
            if (sourceEntry.getPortalMissingMessage() == null) {
                player.sendMessage(ChatColorConverter.convert(WormholesConfig.getDefaultPortalMissingMessage()));
            } else {
                player.sendMessage(sourceEntry.getPortalMissingMessage());
                if (player.isOp() || player.hasPermission("elitemobs.*")) {
                    player.sendMessage(sourceEntry.getOpMessage());
                }
            }
            return;
        }

        // Add player to cooldown and track destination BEFORE teleport
        // to prevent double teleports
        addPlayerToCooldown(player, destinationEntry);

        // Clone location to ensure we have a stable copy
        final Location finalDestination = destination.clone();

        // Perform teleport on the main thread
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (sourceEntry.getWormhole().getWormholeConfigFields().isBlindPlayer()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 0));
            }

            player.teleport(finalDestination);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
            player.setFlying(false);

            // No velocity applied to prevent bouncing
        });
    }

    /**
     * Checks if a player is currently in cooldown
     * @param player The player to check
     * @return true if the player is in cooldown
     */
    public boolean isPlayerInCooldown(Player player) {
        Long cooldownEnd = playerCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return false;

        // Check if cooldown has expired
        return System.currentTimeMillis() <= cooldownEnd;
    }

    /**
     * Adds a player to the cooldown list with a 20-second timeout and tracks their destination
     * @param player The player to add to cooldowns
     * @param destinationEntry The wormhole they teleported to
     */
    public void addPlayerToCooldown(Player player, WormholeEntry destinationEntry) {
        // Calculate expiration time (current time + 20 seconds)
        long expirationTime = System.currentTimeMillis() + (COOLDOWN_DURATION_SECONDS * 1000);
        playerCooldowns.put(player.getUniqueId(), expirationTime);

        // Track which wormhole the player teleported to
        playerDestinations.put(player.getUniqueId(), destinationEntry);
    }

    /**
     * Removes a player from the cooldown list
     * @param player The player to remove
     */
    public void removePlayerFromCooldown(Player player) {
        playerCooldowns.remove(player.getUniqueId());
        playerDestinations.remove(player.getUniqueId());
    }

    /**
     * Gets a set of all players currently in cooldown
     * @return A set of players in cooldown
     */
    public HashSet<Player> getPlayersInCooldown() {
        HashSet<Player> players = new HashSet<>();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> entry : playerCooldowns.entrySet()) {
            // Only include non-expired cooldowns
            if (currentTime <= entry.getValue()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    players.add(player);
                }
            }
        }
        return players;
    }

    /**
     * Shuts down the manager
     */
    public void shutdown() {
        if (wormholeTask != null) {
            wormholeTask.cancel();
            wormholeTask = null;
        }

        playerCooldowns.clear();
        playerDestinations.clear();
        rotationCounters.clear();
    }

    /**
     * For backward compatibility with other code that might use this method
     */
    public Map<UUID, WormholeEntry> getPlayerCooldowns() {
        return playerDestinations;
    }
}