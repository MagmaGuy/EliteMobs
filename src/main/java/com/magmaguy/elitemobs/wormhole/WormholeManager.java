package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ChunkLocationChecker;
import lombok.Getter;
import lombok.NonNull;
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

import java.util.*;

public class WormholeManager {
    // Static cooldown time in seconds (20 seconds)
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
    private final Map<UUID, PlayerWormholeData> playerTeleportData = new HashMap<>();
    // Maps to track rotation counters for each wormhole
    private final Map<WormholeEntry, Integer> rotationCounters = new HashMap<>();
    private BukkitTask wormholeTask;

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
     * Gets players that are near a specific wormhole
     */
    private HashSet<Player> getNearbyPlayers(WormholeEntry wormholeEntry) {
        HashSet<Player> nearbyPlayers = new HashSet<>();
        Location wormholeLocation = wormholeEntry.getLocation();

        if (wormholeLocation == null || wormholeLocation.getWorld() == null) {
            return nearbyPlayers;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(wormholeLocation.getWorld()) &&
                    player.getLocation().distanceSquared(wormholeLocation) <= Math.pow(PARTICLE_RENDER_DISTANCE, 2)) {
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
        for (Player player : nearbyPlayers) {
            if (player.getLocation().distanceSquared(wormholeEntry.getLocation()) > Math.pow(TELEPORT_DISTANCE_MULTIPLIER * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier(), 2))
                continue;
            if (!canPlayerTeleport(wormholeEntry, player)) continue;
            teleportPlayer(wormholeEntry, player);
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

        PlayerWormholeData playerWormholeData = playerTeleportData.get(player.getUniqueId());
        return playerWormholeData == null || playerWormholeData.canTeleport();
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
        if (sourceEntry.getWormhole().getWormholeConfigFields().isBlindPlayer()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 0));
        }

        player.teleport(finalDestination);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        player.setFlying(false);
    }

    /**
     * @param player           The player to add to cooldowns
     * @param destinationEntry The wormhole they teleported to
     */
    public void addPlayerToCooldown(Player player, @NonNull WormholeEntry destinationEntry) {
        playerTeleportData.put(player.getUniqueId(), new PlayerWormholeData(player, destinationEntry, System.currentTimeMillis()));
    }

    public void addPlayerToCooldown(Player player, Location destination) {
        WormholeEntry destinationEntry = null;
        for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
            if (wormholeEntry.getLocation() != null &&
                    wormholeEntry.getLocation().getWorld() != null &&
                    wormholeEntry.getLocation().getWorld().equals(destination.getWorld()) &&
                    destination.distanceSquared(wormholeEntry.getLocation()) <= Math.pow(TELEPORT_DISTANCE_MULTIPLIER * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier(), 2)) {
                destinationEntry = wormholeEntry;
                break;
            }
        }
        if (destinationEntry == null) return;
        addPlayerToCooldown(player, destinationEntry);
    }

    /**
     * Shuts down the manager
     */
    public void shutdown() {
        if (wormholeTask != null) {
            wormholeTask.cancel();
            wormholeTask = null;
        }

        playerTeleportData.clear();
        rotationCounters.clear();
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
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    /**
     * Main method to process all wormholes
     */
    private void processWormholes() {
        Collection<PlayerWormholeData> values = playerTeleportData.values();
        for (PlayerWormholeData value : values) {
            value.tick();
        }

        for (WormholeEntry wormholeEntry : WormholeEntry.getWormholeEntries()) {
            // Skip if location is null or chunk is not loaded
            if (wormholeEntry.getLocation() == null ||
                    !ChunkLocationChecker.chunkAtLocationIsLoaded(wormholeEntry.getLocation())) {
                continue;
            }

            // Get nearby players FIRST - skip everything if no one is around
            HashSet<Player> nearbyPlayers = getNearbyPlayers(wormholeEntry);
            if (nearbyPlayers.isEmpty()) continue;

            // Only initialize text display if players are nearby AND it's needed
            // Consider adding a counter to do this less frequently (every 20 ticks instead of 5)
            if ((wormholeEntry.getText() == null || !wormholeEntry.getText().isValid())
                    && wormholeEntry.getLocation().getWorld() != null) {
                wormholeEntry.initializeTextDisplay();
            }

            // Skip visual effects if particles are disabled
            if (!WormholesConfig.isNoParticlesMode() &&
                    wormholeEntry.getWormhole().getWormholeConfigFields().getStyle() != Wormhole.WormholeStyle.NONE) {
                displayVisualEffects(wormholeEntry, nearbyPlayers);
            }

            // Check for players who should teleport
            checkForTeleports(wormholeEntry, nearbyPlayers);
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
            this.destination = destinationWormhole.getLocation();
            this.timeStamp = timeStamp;
            this.wormholeEntry = destinationWormhole;
        }

        public boolean canTeleport() {
            return timeStamp + COOLDOWN_DURATION_SECONDS * 1000 < System.currentTimeMillis() &&
                    isHasLeftTeleportRadius();
        }

        //Has to run on the tick to see the distance. Should be efficient.
        public void tick() {
            if (isHasLeftTeleportRadius() && enoughTimeHasPassed()) playerTeleportData.remove(player.getUniqueId());
        }

        private boolean enoughTimeHasPassed() {
            return timeStamp + COOLDOWN_DURATION_SECONDS * 1000 < System.currentTimeMillis();
        }

        private boolean isHasLeftTeleportRadius() {
            if (hasLeftTeleportRadius) return true;
            if (!player.getWorld().equals(destination.getWorld())) return false;
            if (destination.distanceSquared(player.getLocation()) > Math.pow(TELEPORT_DISTANCE_MULTIPLIER * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier() + SAFE_DISTANCE, 2))
                return hasLeftTeleportRadius = true;
            return false;
        }
    }
}