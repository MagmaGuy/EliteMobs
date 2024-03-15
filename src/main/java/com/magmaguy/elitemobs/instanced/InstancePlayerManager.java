package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerJoinArenaEvent;
import com.magmaguy.elitemobs.api.PlayerJoinDungeonEvent;
import com.magmaguy.elitemobs.api.instanced.MatchJoinEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.AlternativeDurabilityLoss;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InstancePlayerManager {

    public static boolean addNewPlayer(Player player, MatchInstance matchInstance) {
        MatchJoinEvent event = new MatchJoinEvent(matchInstance, player);
        if (event.isCancelled()) return false;

        //Right now new players can't join ongoing instances
        if (!matchInstance.state.equals(MatchInstance.InstancedRegionState.WAITING)) {
            player.sendMessage(ArenasConfig.getArenasOngoingMessage());
            return false;
        }
        //Check if match is full
        if (matchInstance.players.size() + 1 > matchInstance.maxPlayers) {
            player.sendMessage(ArenasConfig.getArenaFullMessage());
            return false;
        }
        //Check permissions
        if (matchInstance.getPermission() != null && !player.hasPermission(matchInstance.getPermission()))
            return false;

        //Add the player to the relevant fields
        matchInstance.participants.add(player);
        matchInstance.players.add(player);
        PlayerData.setMatchInstance(player, matchInstance);
        player.sendMessage(ArenasConfig.getArenaJoinPlayerMessage().replace("$count", matchInstance.minPlayers + ""));
        player.sendTitle(ArenasConfig.getJoinPlayerTitle(), ArenasConfig.getJoinPlayerSubtitle(), 60, 60 * 3, 60);

        //Should be the first join of the player, do the join events
        matchInstance.getPreviousPlayerLocations().put(player, player.getLocation());
        if (matchInstance instanceof ArenaInstance arenaInstance)
            new EventCaller(new PlayerJoinArenaEvent(arenaInstance));
        else if (matchInstance instanceof DungeonInstance dungeonInstance)
            new EventCaller(new PlayerJoinDungeonEvent(dungeonInstance));


        new BukkitRunnable() {
            @Override
            public void run() {
                //Teleport the player to the correct location
                MatchInstance.MatchInstanceEvents.teleportBypass = true;
                if (matchInstance.state.equals(MatchInstance.InstancedRegionState.WAITING) && matchInstance.lobbyLocation != null)
                    player.teleport(matchInstance.lobbyLocation);
                else
                    player.teleport(matchInstance.startLocation);

                //Set the lives that the player has //todo: this needs to become configurable and be expanded upon in the future
                matchInstance.playerLives.put(player, 3);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);

        return true;
    }

    public static void removePlayer(Player player, MatchInstance matchInstance) {
        new MatchLeaveEvent(matchInstance, player);

        //Remove match instance where needed
        PlayerData.setMatchInstance(player, null);
        matchInstance.players.remove(player);
        if (!matchInstance.spectators.contains(player)) {
            matchInstance.participants.remove(player);
            PlayerData.setMatchInstance(player, null);
        }

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
                matchInstance.players.isEmpty()) {
            matchInstance.defeat();
        } else
            //Remove lives
            matchInstance.playerLives.remove(player);
    }

    public static void playerDeath(MatchInstance matchInstance, Player player) {
        if (!matchInstance.players.contains(player)) return;
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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
        MatchInstance.MatchInstanceEvents.teleportBypass = true;
        player.teleport(deathLocation.getBannerBlock().getLocation());
        PlayerData.setMatchInstance(player, matchInstance);
    }

    public static void addSpectator(MatchInstance matchInstance, Player player, boolean wasPlayer) {
        new MatchJoinEvent(matchInstance, player);

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
    }

    public static void removeSpectator(MatchInstance matchInstance, Player player) {
        matchInstance.spectators.remove(player);
        if (!matchInstance.players.contains(player)) {
            PlayerData.setMatchInstance(player, null);
            matchInstance.participants.remove(player);
        }
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
        matchInstance.participants.remove(player);
        PlayerData.setMatchInstance(player, null);
    }

}
