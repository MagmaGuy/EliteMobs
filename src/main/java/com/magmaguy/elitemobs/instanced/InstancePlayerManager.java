package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerJoinArenaEvent;
import com.magmaguy.elitemobs.api.PlayerJoinDungeonEvent;
import com.magmaguy.elitemobs.api.PlayerLeaveArenaEvent;
import com.magmaguy.elitemobs.api.PlayerLeaveDungeonEvent;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.api.instanced.MatchJoinEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.AlternativeDurabilityLoss;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class InstancePlayerManager {

    static void restoreFullHealth(Player player) {
        if (player == null || !player.isOnline() || player.isDead()) return;
        player.setHealth(player.getMaxHealth());
    }

    private static void restoreFullHealthIfMatchFinished(Player player, MatchInstance matchInstance) {
        if (matchInstance.state != MatchInstance.InstancedRegionState.COMPLETED &&
                matchInstance.state != MatchInstance.InstancedRegionState.COMPLETED_DEFEAT &&
                matchInstance.state != MatchInstance.InstancedRegionState.COMPLETED_VICTORY)
            return;
        restoreFullHealth(player);
    }

    public static boolean addNewPlayer(Player player, MatchInstance matchInstance) {
        return addNewPlayers(List.of(player), matchInstance);
    }

    /**
     * Atomically admits a set of players. All ordinary constraints and every cancellable join
     * event are checked before any match/player state is mutated, so a party is never split by a
     * full instance, missing permission, another active match, or an API veto. A listener may see
     * an uncancelled preflight event for an earlier member before a later member vetoes the batch;
     * those events describe admission attempts and no player state has changed at that point.
     */
    public static boolean addNewPlayers(Collection<Player> requestedPlayers, MatchInstance matchInstance) {
        LinkedHashMap<UUID, Player> uniquePlayers = new LinkedHashMap<>();
        for (Player player : requestedPlayers)
            if (player != null) uniquePlayers.putIfAbsent(player.getUniqueId(), player);
        List<Player> playersToAdd = uniquePlayers.values().stream()
                .filter(player -> !matchInstance.players.contains(player))
                .toList();
        if (playersToAdd.isEmpty()) return false;

        if (!canAdmitPlayers(playersToAdd, matchInstance, true)) return false;
        for (Player player : playersToAdd)
            if (!fireJoinEvent(matchInstance, player)) return false;
        // Join listeners execute synchronously and can change match/player state. Fail closed if
        // anything changed during the batch preflight instead of admitting only part of a party.
        if (!canAdmitPlayers(playersToAdd, matchInstance, true)) return false;

        LinkedHashMap<UUID, Location> previousLocations = new LinkedHashMap<>();
        for (Player player : playersToAdd)
            previousLocations.put(player.getUniqueId(), player.getLocation());

        // Keep the mutation phase deliberately small and non-callback-based. Once this loop has
        // completed, every member is visible in the match before post-join API events are fired.
        playersToAdd.forEach(player -> registerPlayer(
                player,
                matchInstance,
                previousLocations.get(player.getUniqueId())));

        List<BukkitTask> entryTasks = new ArrayList<>();
        try {
            for (Player player : playersToAdd)
                entryTasks.add(scheduleEntry(player, matchInstance));
        } catch (RuntimeException exception) {
            entryTasks.forEach(BukkitTask::cancel);
            rollbackRegistrations(playersToAdd, matchInstance);
            Logger.warn("Failed to schedule an instance-entry batch: " + exception.getMessage());
            return false;
        }

        playersToAdd.forEach(player -> notifyAdmission(player, matchInstance));
        return true;
    }

    private static boolean canAdmitPlayers(List<Player> playersToAdd,
                                           MatchInstance matchInstance,
                                           boolean sendFeedback) {
        //Right now new players can't join ongoing instances
        if (!matchInstance.state.equals(MatchInstance.InstancedRegionState.WAITING)) {
            if (sendFeedback) playersToAdd.get(0).sendMessage(ArenasConfig.getArenasOngoingMessage());
            return false;
        }
        //Check if match is full
        if (matchInstance.players.size() + playersToAdd.size() > matchInstance.maxPlayers) {
            if (sendFeedback) playersToAdd.get(0).sendMessage(ArenasConfig.getArenaFullMessage());
            return false;
        }
        for (Player player : playersToAdd) {
            if (!player.isOnline() || !player.isValid() || !PlayerData.isInMemory(player.getUniqueId())
                    || matchInstance.players.contains(player)) return false;
            // Both indexes are checked. A disagreement between them is treated as occupied instead
            // of allowing one player to become a member of two instances.
            if (PlayerData.getMatchInstance(player) != null || MatchInstance.getAnyPlayerInstance(player) != null)
                return false;
            if (matchInstance.getPermission() != null && !player.hasPermission(matchInstance.getPermission()))
                return false;
        }
        return true;
    }

    private static void registerPlayer(Player player, MatchInstance matchInstance, Location previousLocation) {
        matchInstance.participants.add(player);
        matchInstance.players.add(player);
        PlayerData.setMatchInstance(player, matchInstance);
        matchInstance.getPreviousPlayerLocations().put(player, previousLocation);
    }

    private static void rollbackRegistrations(List<Player> players, MatchInstance matchInstance) {
        for (Player player : players) {
            matchInstance.players.remove(player);
            matchInstance.participants.remove(player);
            matchInstance.getPreviousPlayerLocations().remove(player);
            if (PlayerData.getMatchInstance(player) == matchInstance)
                PlayerData.setMatchInstance(player, null);
        }
    }

    /** Clears a completed admission and balances its public join notifications without teleporting. */
    public static void rollbackAdmissions(Collection<Player> admittedPlayers, MatchInstance matchInstance) {
        for (Player player : admittedPlayers) {
            boolean wasParticipant = matchInstance.participants.remove(player);
            matchInstance.players.remove(player);
            matchInstance.spectators.remove(player);
            matchInstance.playerLives.remove(player);
            matchInstance.getPreviousPlayerLocations().remove(player);
            if (PlayerData.getMatchInstance(player) == matchInstance)
                PlayerData.setMatchInstance(player, null);
            if (wasParticipant) fireLeaveEvents(matchInstance, player);
        }
    }

    private static void notifyAdmission(Player player, MatchInstance matchInstance) {
        try {
            player.sendMessage(ArenasConfig.getArenaJoinPlayerMessage().replace("$count", matchInstance.minPlayers + ""));
            player.sendTitle(ArenasConfig.getJoinPlayerTitle(), ArenasConfig.getJoinPlayerSubtitle(), 60, 60 * 3, 60);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to show instance join feedback to " + player.getName() + ": " + exception.getMessage());
        }

        try {
            fireTypedJoinEvent(matchInstance, player);
        } catch (RuntimeException exception) {
            Logger.warn("A post-join API callback failed for " + player.getName() + ": " + exception.getMessage());
        }
    }

    private static BukkitTask scheduleEntry(Player player, MatchInstance matchInstance) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()
                        || !matchInstance.players.contains(player)
                        || PlayerData.getMatchInstance(player) != matchInstance)
                    return;

                //Teleport the player to the correct location
                MatchInstance.MatchInstanceEvents.teleportBypass = true;
                Location destination = (matchInstance.state.equals(MatchInstance.InstancedRegionState.WAITING) && matchInstance.lobbyLocation != null)
                        ? matchInstance.lobbyLocation
                        : matchInstance.startLocation;

                // Use PlayerTeleportEvent to trigger dungeon music and other listeners
                PlayerTeleportEvent.teleportPlayer(player, destination);

                //Set the lives that the player has //todo: this needs to become configurable and be expanded upon in the future
                matchInstance.playerLives.put(player, 3);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    public static void removePlayer(Player player, MatchInstance matchInstance) {
        boolean wasParticipant = matchInstance.participants.contains(player);
        restoreFullHealthIfMatchFinished(player, matchInstance);

        //Remove match instance where needed
        PlayerData.setMatchInstance(player, null);
        matchInstance.players.remove(player);
        if (!matchInstance.spectators.contains(player)) {
            matchInstance.participants.remove(player);
            PlayerData.setMatchInstance(player, null);
        }
        if (wasParticipant && !matchInstance.participants.contains(player))
            fireLeaveEvents(matchInstance, player);

        if (matchInstance.players.isEmpty() && matchInstance.getDeathLocationByPlayer(player) != null)
            matchInstance.getDeathLocationByPlayer(player).clear(false);

        //Teleport the player out
        if (player.isOnline()) {
            MatchInstance.MatchInstanceEvents.teleportBypass = true;
            if (matchInstance instanceof DungeonInstance) {
                Location location = matchInstance.previousPlayerLocations.get(player);
                if (location != null) player.teleport(location);
                else player.teleport(matchInstance.exitLocation);
            } else
                player.teleport(matchInstance.exitLocation);
        }

        //End the match if there are no players left because they all died
        if (matchInstance.state != MatchInstance.InstancedRegionState.COMPLETED &&
                matchInstance.state != MatchInstance.InstancedRegionState.COMPLETED_DEFEAT &&
                matchInstance.state != MatchInstance.InstancedRegionState.COMPLETED_VICTORY &&
                matchInstance.players.isEmpty() && !matchInstance.isDestroyingMatch()) {
            matchInstance.defeat();
        } else
            //Remove lives
            matchInstance.playerLives.remove(player);
    }

    public static void playerDeath(MatchInstance matchInstance, Player player) {
        if (!matchInstance.players.contains(player)) return;
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        AttributeManager.setAttribute(player, "generic_max_health", AttributeManager.getAttributeBaseValue(player, "generic_max_health"));
        matchInstance.players.remove(player);
        if (matchInstance.players.isEmpty()) {
            matchInstance.defeat();
            MatchInstance.MatchInstanceEvents.teleportBypass = true;
            if (matchInstance.previousPlayerLocations.get(player) != null)
                player.teleport(matchInstance.previousPlayerLocations.get(player));
            else if (matchInstance.exitLocation != null)
                player.teleport(matchInstance.exitLocation);
            PlayerData.setMatchInstance(player, null);
            matchInstance.participants.remove(player);
            fireLeaveEvents(matchInstance, player);
            return;
        }
        // When spectating is disabled the server wants vanilla spectator mode to never be
        // applied inside instanced content (it's the whole point of the toggle, and it closes
        // the spectator-teleport exploit surface entirely). Waiting for a revive *is* spectator
        // mode, so that state can no longer exist: each participant effectively gets a single
        // life, and a dead player is removed from the instance instead of becoming a spectator
        // at a death banner.
        if (!DungeonsConfig.isAllowSpectatorsInInstancedContent()) {
            MatchInstance.MatchInstanceEvents.teleportBypass = true;
            if (matchInstance.previousPlayerLocations.get(player) != null)
                player.teleport(matchInstance.previousPlayerLocations.get(player));
            else if (matchInstance.exitLocation != null)
                player.teleport(matchInstance.exitLocation);
            PlayerData.setMatchInstance(player, null);
            matchInstance.participants.remove(player);
            matchInstance.playerLives.remove(player);
            fireLeaveEvents(matchInstance, player);
            return;
        }

        new InstanceDeathLocation(player, matchInstance);
        matchInstance.addSpectator(player, true);
    }

    public static void revivePlayer(MatchInstance matchInstance, Player player, InstanceDeathLocation deathLocation) {
        matchInstance.playerLives.put(player, matchInstance.playerLives.get(player) - 1);
        matchInstance.players.add(player);
        player.setGameMode(GameMode.SURVIVAL);
        matchInstance.spectators.remove(player);
        player.setHealth(player.getMaxHealth());
        MatchInstance.MatchInstanceEvents.teleportBypass = true;
        player.teleport(deathLocation.getRespawnLocation());
        PlayerData.setMatchInstance(player, matchInstance);
    }

    public static void addSpectator(MatchInstance matchInstance, Player player, boolean wasPlayer) {
        if (!wasPlayer && !fireJoinEvent(matchInstance, player)) return;

        if (!wasPlayer) matchInstance.previousPlayerLocations.put(player, player.getLocation());
        matchInstance.participants.add(player);
        player.sendMessage(ArenasConfig.getArenaJoinSpectatorMessage());
        player.sendTitle(ArenasConfig.getJoinSpectatorTitle(), ArenasConfig.getJoinSpectatorSubtitle(), 60, 60 * 3, 60);
        matchInstance.spectators.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        if (!wasPlayer) {
            MatchInstance.MatchInstanceEvents.teleportBypass = true;
            player.teleport(matchInstance.startLocation);
        }
        PlayerData.setMatchInstance(player, matchInstance);
        if (!wasPlayer) fireTypedJoinEvent(matchInstance, player);
    }

    public static void removeSpectator(MatchInstance matchInstance, Player player) {
        boolean wasParticipant = matchInstance.participants.contains(player);
        restoreFullHealthIfMatchFinished(player, matchInstance);
        matchInstance.spectators.remove(player);
        if (!matchInstance.players.contains(player)) {
            PlayerData.setMatchInstance(player, null);
            matchInstance.participants.remove(player);
        }
        if (wasParticipant && !matchInstance.participants.contains(player))
            fireLeaveEvents(matchInstance, player);
        player.setGameMode(GameMode.SURVIVAL);
        MatchInstance.MatchInstanceEvents.teleportBypass = true;
        if (matchInstance instanceof DungeonInstance) {
            Location location = matchInstance.previousPlayerLocations.get(player);
            if (location != null) player.teleport(location);
            else player.teleport(matchInstance.exitLocation);
        } else
            player.teleport(matchInstance.exitLocation);
        PlayerData.setMatchInstance(player, null);
        matchInstance.playerLives.remove(player);
        if (matchInstance.getDeathLocationByPlayer(player) != null)
            matchInstance.getDeathLocationByPlayer(player).clear(false);
    }

    public static void removeAnyKind(MatchInstance matchInstance, Player player) {
        if (matchInstance.players.contains(player)) matchInstance.removePlayer(player);
        if (matchInstance.spectators.contains(player)) matchInstance.removeSpectator(player);
        if (matchInstance.participants.remove(player)) fireLeaveEvents(matchInstance, player);
        PlayerData.setMatchInstance(player, null);
    }

    private static boolean fireJoinEvent(MatchInstance matchInstance, Player player) {
        MatchJoinEvent event = new MatchJoinEvent(matchInstance, player);
        new EventCaller(event);
        return !event.isCancelled();
    }

    private static void fireTypedJoinEvent(MatchInstance matchInstance, Player player) {
        if (matchInstance instanceof ArenaInstance arenaInstance)
            new EventCaller(new PlayerJoinArenaEvent(arenaInstance, player));
        else if (matchInstance instanceof DungeonInstance dungeonInstance)
            new EventCaller(new PlayerJoinDungeonEvent(dungeonInstance, player));
    }

    private static void fireLeaveEvents(MatchInstance matchInstance, Player player) {
        new EventCaller(new MatchLeaveEvent(matchInstance, player));
        if (matchInstance instanceof ArenaInstance arenaInstance)
            new EventCaller(new PlayerLeaveArenaEvent(arenaInstance, player));
        else if (matchInstance instanceof DungeonInstance dungeonInstance)
            new EventCaller(new PlayerLeaveDungeonEvent(dungeonInstance, player));
    }

}
