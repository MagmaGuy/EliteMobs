package com.magmaguy.elitemobs.instanced;


import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class MatchInstance {

    protected static final HashSet<MatchInstance> instances = new HashSet<>();
    @Getter
    protected final HashMap<Block, InstanceDeathLocation> deathBanners = new HashMap<>();
    @Getter
    protected final Map<Player, Location> previousPlayerLocations = new HashMap<>();
    @Getter
    protected HashSet<Player> players = new HashSet<>();
    protected HashMap<Player, Integer> playerLives = new HashMap();
    @Getter
    protected HashSet<Player> participants = new HashSet<>();
    protected HashSet<Player> spectators = new HashSet<>();
    @Getter
    protected InstancedRegionState state = InstancedRegionState.WAITING;
    protected Location lobbyLocation = null;
    protected Location startLocation;
    protected Location exitLocation;
    protected int minPlayers;
    protected int maxPlayers;
    protected World world;


    public MatchInstance(Location startLocation, Location exitLocation, int minPlayers, int maxPlayers) {
        this.startLocation = startLocation;
        this.exitLocation = exitLocation;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;

        startWatchdogs();
        instanceMessages();
        instances.add(this);
    }

    public static void shutdown() {
        HashSet<MatchInstance> cloneInstance = new HashSet<>(instances);
        cloneInstance.forEach(MatchInstance::resetMatch);
        instances.clear();
    }

    public static MatchInstance getPlayerInstance(Player player) {
        for (MatchInstance matchInstance : instances)
            for (Player iteratedPlayer : matchInstance.players)
                if (iteratedPlayer.equals(player)) return matchInstance;
        return null;
    }

    public static MatchInstance getSpectatorInstance(Player player) {
        for (MatchInstance matchInstance : instances)
            for (Player iteratedPlayer : matchInstance.spectators)
                if (iteratedPlayer.equals(player)) return matchInstance;
        return null;
    }

    public static MatchInstance getAnyPlayerInstance(Player player) {
        for (MatchInstance matchInstance : instances) {
            for (Player iteratedPlayer : matchInstance.players)
                if (iteratedPlayer.equals(player)) return matchInstance;
            for (Player iteratedPlayer : matchInstance.spectators)
                if (iteratedPlayer.equals(player)) return matchInstance;
        }
        return null;
    }

    public boolean addNewPlayer(Player player) {
        return InstancePlayerManager.addNewPlayer(player, this);
    }

    public void removePlayer(Player player) {
        InstancePlayerManager.removePlayer(player, this);
    }

    public void playerDeath(Player player) {
        InstancePlayerManager.playerDeath(this, player);
    }

    public void revivePlayer(Player player, InstanceDeathLocation deathLocation) {
        InstancePlayerManager.revivePlayer(this, player, deathLocation);
    }

    public void addSpectator(Player player, boolean wasPlayer) {
        InstancePlayerManager.addSpectator(this, player, wasPlayer);
    }

    public void removeSpectator(Player player) {
        InstancePlayerManager.removeSpectator(this, player);
    }

    public void removeAnyKind(Player player) {
        InstancePlayerManager.removeAnyKind(this, player);
    }

    private void startWatchdogs() {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerWatchdog();
                spectatorWatchdog();
                intruderWatchdog();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void playerWatchdog() {
        ((HashSet<Player>) players.clone()).forEach(player -> {
            if (!player.isOnline()) removePlayer(player);
            if (!isInRegion(player.getLocation())) {
                MatchInstanceEvents.teleportBypass = true;
                player.teleport(startLocation);
            }
        });
    }

    private void spectatorWatchdog() {
        ((HashSet<Player>) spectators.clone()).forEach(player -> {
            if (!player.isOnline()) removeSpectator(player);
            if (!isInRegion(player.getLocation())) {
                MatchInstanceEvents.teleportBypass = true;
                player.teleport(startLocation);
            }
        });
    }

    private void intruderWatchdog() {
        if (state != InstancedRegionState.ONGOING) return;
        for (Player player : Bukkit.getOnlinePlayers())
            if (!players.contains(player) && !spectators.contains(player) && isInRegion(player.getLocation())) {
                MatchInstanceEvents.teleportBypass = true;
                //player.teleport(exitLocation);
            }
    }

    private void instanceMessages() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MetadataHandler.PLUGIN, () -> {
            if (state == InstancedRegionState.WAITING)
                announce(ArenasConfig.getArenaStartHintMessage().replace("$count", minPlayers + ""));
        }, 0L, 20 * 60L);
    }

    protected void announce(String message) {
        participants.forEach(player -> player.sendMessage(ChatColorConverter.convert(message)));
    }

    public void countdownMatch() {
        if (state != InstancedRegionState.WAITING) return;
        if (players.size() < minPlayers) {
            announce(ArenasConfig.getNotEnoughPlayersMessage().replace("$amount", minPlayers + ""));
            return;
        }
        state = InstancedRegionState.STARTING;
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (players.size() < minPlayers) {
                    cancel();
                    endMatch();
                    return;
                }
                counter++;
                players.forEach(player -> startMessage(counter, player));
                spectators.forEach(player -> startMessage(counter, player));
                if (counter >= 3) {
                    startMatch();
                    cancel();
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
    }

    private void startMessage(int counter, Player player) {
        player.sendTitle(ArenasConfig.getStartingTitle(), ArenasConfig.getStartingSubtitle()
                .replace("$count", (3 - counter) + ""), 0, 20, 0);
    }

    protected abstract boolean isInRegion(Location location);

    protected void startMatch() {
        state = InstancedRegionState.ONGOING;
        players.forEach(player -> player.teleport(startLocation));
        participants = (HashSet<Player>) players.clone();
    }

    protected void endMatch() {
        state = InstancedRegionState.COMPLETED;
    }

    protected void resetMatch() {
        state = InstancedRegionState.WAITING;
        HashSet<Player> copy = new HashSet<>(participants);
        copy.forEach(this::removeAnyKind);
        players.clear();
        spectators.clear();
        spectators.clear();
        deathBanners.values().forEach(deathLocation -> deathLocation.clear(false));
        deathBanners.clear();
    }

    protected InstanceDeathLocation getDeathLocationByPlayer(Player player) {
        for (InstanceDeathLocation deathLocation : deathBanners.values())
            if (deathLocation.getDeadPlayer().equals(player))
                return deathLocation;
        return null;
    }

    public enum InstancedRegionState {
        WAITING, STARTING, ONGOING, COMPLETED
    }

    public static class MatchInstanceEvents implements Listener {
        public static boolean teleportBypass = false;

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent event) {
            HashSet<MatchInstance> copy = new HashSet<>(instances);
            copy.forEach(instance -> instance.removeAnyKind(event.getPlayer()));
        }

        @EventHandler
        public void onPlayerBreakBlockEvent(BlockBreakEvent event) {
            for (MatchInstance matchInstance : instances)
                if (matchInstance.state.equals(InstancedRegionState.ONGOING))
                    if (matchInstance.getDeathBanners().get(event.getBlock()) != null)
                        matchInstance.getDeathBanners().get(event.getBlock()).clear(true);
        }

        /**
         * This event scans for damage that would kill the player and cancels it with custom behavior it if would
         *
         * @param event Damage event
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerDamage(EntityDamageEvent event) {
            if (!event.getEntityType().equals(EntityType.PLAYER)) return;
            Player player = (Player) event.getEntity();
            if (event.getFinalDamage() < player.getHealth()) return;
            MatchInstance matchInstance = PlayerData.getMatchInstance(player);
            if (matchInstance == null) return;
            if (matchInstance.state != InstancedRegionState.ONGOING) matchInstance.removePlayer(player);
            event.setCancelled(true);
            matchInstance.playerDeath(player);
        }

        @EventHandler (ignoreCancelled = true)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
            if (teleportBypass) {
                teleportBypass = false;
                return;
            }
            MatchInstance matchInstance = PlayerData.getMatchInstance(event.getPlayer());
            if (matchInstance == null) return;
            event.setCancelled(true);
        }
    }

}
