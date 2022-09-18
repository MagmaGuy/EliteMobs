package com.magmaguy.elitemobs.instanced;


import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.Cylinder;
import com.magmaguy.elitemobs.utils.VisualArmorStand;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MatchInstance {

    protected static final HashSet<MatchInstance> instances = new HashSet<>();
    @Getter
    private final HashMap<Block, DeathLocation> deathBanners = new HashMap<>();
    @Getter
    protected HashSet<Player> players = new HashSet<>();
    protected HashMap<Player, Integer> playerLives = new HashMap();
    protected HashSet<Player> participants = new HashSet<>();
    protected HashSet<Player> spectators = new HashSet<>();
    protected InstancedRegionState state = InstancedRegionState.IDLE;
    protected Location startLocation;
    protected Location exitLocation;
    protected HashMap<String, Location> spawnPoints = new HashMap<>();
    private Location corner1;
    private Location corner2;
    private int minPlayers;
    private int maxPlayers;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;
    private World world;
    private boolean cylindricalArena;
    private final Map<Player, Location> previousPlayerLocations = new HashMap<>();
    private Cylinder cylinder;

    public MatchInstance(Location startLocation, Location exitLocation, int minPlayers, int maxPlayers) {
        instantiate(startLocation, exitLocation, minPlayers, maxPlayers);
    }

    public MatchInstance(Location corner1,
                         Location corner2,
                         Location startLocation,
                         Location exitLocation,
                         int minPlayers,
                         int maxPlayers,
                         List<String> spawnPoints,
                         boolean cylindricalArena) {
        addSpawnPoints(spawnPoints);
        instantiate(corner1, corner2, startLocation, exitLocation, minPlayers, maxPlayers, cylindricalArena);
    }

    public MatchInstance(Location corner1,
                         Location corner2,
                         Location startLocation,
                         Location exitLocation,
                         int minPlayers,
                         int maxPlayers,
                         boolean cylindricalArena) {
        instantiate(corner1, corner2, startLocation, exitLocation, minPlayers, maxPlayers, cylindricalArena);
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

    public void instantiate(Location startLocation, Location exitLocation, int minPlayers, int maxPlayers) {
        this.startLocation = startLocation;
        this.exitLocation = exitLocation;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;

        startWatchdogs();
        instanceMessages();
        instances.add(this);
    }

    public void instantiate(Location corner1, Location corner2, Location startLocation, Location exitLocation, int minPlayers, int maxPlayers, boolean cylindricalArena) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.startLocation = startLocation;
        this.exitLocation = exitLocation;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.cylindricalArena = cylindricalArena;
        if (corner1.getX() < corner2.getX()) {
            minX = (int) corner1.getX();
            maxX = (int) corner2.getX();
        } else {
            minX = (int) corner2.getX();
            maxX = (int) corner1.getX();
        }

        if (corner1.getY() < corner2.getY()) {
            minY = (int) corner1.getY();
            maxY = (int) corner2.getY();
        } else {
            minY = (int) corner2.getY();
            maxY = (int) corner1.getY();
        }

        if (corner1.getZ() < corner2.getZ()) {
            minZ = (int) corner1.getZ();
            maxZ = (int) corner2.getZ();
        } else {
            minZ = (int) corner2.getZ();
            maxZ = (int) corner1.getZ();
        }
        this.world = corner1.getWorld();
        this.state = InstancedRegionState.IDLE;

        if (cylindricalArena)
            cylinder = new Cylinder(
                    new Vector((maxX - minX) / 2D + minX, minY,
                            (maxZ - minZ) / 2D + minZ),
                    (Math.abs(maxX) - Math.abs(minX)) / 2D, maxY - minY);

        startWatchdogs();

        instanceMessages();

        instances.add(this);
    }

    public void addSpawnPoints(List<String> rawSpawnPoints) {
        for (String string : rawSpawnPoints) {
            String[] splitEntry = string.split(":");
            String name = "";
            String location = "";
            for (String subString : splitEntry) {
                String[] splitSubEntry = subString.split("=");
                switch (splitSubEntry[0].toLowerCase()) {
                    case "name":
                        name = splitSubEntry[1];
                        break;
                    case "location":
                        location = splitSubEntry[1];
                        break;
                    default:
                        new WarningMessage("Invalid entry for the spawn points of instanced content: " + splitSubEntry[0]);
                        break;
                }
            }
            spawnPoints.put(name, ConfigurationLocation.serialize(location));
        }
    }

    public void addPlayer(Player player) {
        if (state.equals(InstancedRegionState.IDLE)) previousPlayerLocations.put(player, player.getLocation());
        participants.add(player);
        if (state.equals(InstancedRegionState.ONGOING)) {
            player.sendMessage(ArenasConfig.getArenasOngoingMessage());
            return;
        }
        if (players.size() + 1 > maxPlayers) {
            player.sendMessage(ArenasConfig.getArenaFullMessage());
            return;
        }
        player.sendMessage(ArenasConfig.getArenaJoinPlayerMessage().replace("$count", minPlayers + ""));
        player.sendTitle(ArenasConfig.getJoinPlayerTitle(), ArenasConfig.getJoinPlayerSubtitle(), 60, 60 * 3, 60);
        players.add(player);
        MatchInstanceEvents.teleportBypass = true;
        player.teleport(startLocation);
        PlayerData.setMatchInstance(player, this);
        playerLives.put(player, 3);
    }

    public void removePlayer(Player player) {
        PlayerData.setMatchInstance(player, null);
        players.remove(player);
        if (!spectators.contains(player)) {
            participants.remove(player);
            PlayerData.setMatchInstance(player, null);
        }


        if (players.isEmpty() && getDeathLocationByPlayer(player) != null)
            getDeathLocationByPlayer(player).clear(false);

        if (player.isOnline()) {
            MatchInstanceEvents.teleportBypass = true;
            if (this instanceof DungeonInstance) {
                Location location = previousPlayerLocations.get(player);
                if (location != null) player.teleport(location);
                else player.teleport(exitLocation);
            } else
                player.teleport(exitLocation);
        }

        if (players.isEmpty())
            endMatch();

        /*
        if (state.equals(InstancedRegionState.ONGOING) && players.isEmpty()){
            if (player.isOnline()) {
                MatchInstanceEvents.teleportBypass = true;
                if (this instanceof DungeonInstance) {
                    Location location = previousPlayerLocations.get(player);
                    if (location != null) player.teleport(location);
                    else player.teleport(exitLocation);
                } else
                    player.teleport(exitLocation);
            } else if (getDeathLocationByPlayer(player) != null)

            endMatch();
        }
         */
        playerLives.remove(player);
    }

    public void playerDeath(Player player) {
        if (!players.contains(player)) return;
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        players.remove(player);
        if (players.isEmpty()) {
            endMatch();
            MatchInstanceEvents.teleportBypass = true;
            player.teleport(exitLocation);
            PlayerData.setMatchInstance(player, null);
            participants.remove(player);
            return;
        }
        new DeathLocation(player);
        addSpectator(player, true);
    }

    public void revivePlayer(Player player, DeathLocation deathLocation) {
        playerLives.put(player, playerLives.get(player) - 1);
        players.add(player);
        player.setGameMode(GameMode.SURVIVAL);
        spectators.remove(player);
        MatchInstanceEvents.teleportBypass = true;
        player.teleport(deathLocation.bannerBlock.getLocation());
        PlayerData.setMatchInstance(player, this);
    }

    public void addSpectator(Player player, boolean wasPlayer) {
        if (!wasPlayer) previousPlayerLocations.put(player, player.getLocation());
        participants.add(player);
        player.sendMessage(ArenasConfig.getArenaJoinSpectatorMessage());
        player.sendTitle(ArenasConfig.getJoinSpectatorTitle(), ArenasConfig.getJoinSpectatorSubtitle(), 60, 60 * 3, 60);
        spectators.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        MatchInstanceEvents.teleportBypass = true;
        if (!wasPlayer) player.teleport(startLocation);
        PlayerData.setMatchInstance(player, this);
    }

    public void removeSpectator(Player player) {
        spectators.remove(player);
        if (!players.contains(player)) {
            PlayerData.setMatchInstance(player, null);
            participants.remove(player);
        }
        player.setGameMode(GameMode.SURVIVAL);
        MatchInstanceEvents.teleportBypass = true;
        if (this instanceof DungeonInstance) {
            Location location = previousPlayerLocations.get(player);
            if (location != null) player.teleport(location);
            else player.teleport(exitLocation);
        } else
            player.teleport(exitLocation);
        PlayerData.setMatchInstance(player, null);
        playerLives.remove(player);
        if (getDeathLocationByPlayer(player) != null)
            getDeathLocationByPlayer(player).clear(false);
    }

    public void removeAnyKind(Player player) {
        if (players.contains(player)) removePlayer(player);
        if (spectators.contains(player)) removeSpectator(player);
        participants.remove(player);
        PlayerData.setMatchInstance(player, null);
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
                player.teleport(exitLocation);
            }
    }

    private void instanceMessages() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MetadataHandler.PLUGIN, () -> {
            if (state == InstancedRegionState.IDLE) {
                players.forEach(player -> player.sendMessage(ArenasConfig.getArenaStartHintMessage().replace("$count", minPlayers + "")));
            }
        }, 0L, 20 * 60L);
    }


    private boolean isInRegion(Location location) {
        if (this instanceof DungeonInstance) {
            return location.getWorld().equals(startLocation.getWorld());
        } else {
            if (!cylindricalArena)
                return location.getWorld().equals(world) &&
                        minX <= location.getX() &&
                        maxX >= location.getX() &&
                        minY <= location.getY() &&
                        maxY >= location.getY() &&
                        minZ <= location.getZ() &&
                        maxZ >= location.getZ();
            else
                return location.getWorld().equals(world) &&
                        cylinder.contains(location);
        }
    }

    public void countdownMatch() {
        if (state != InstancedRegionState.IDLE) return;
        if (players.size() < minPlayers) {
            players.forEach(player -> player.sendMessage(ArenasConfig.getNotEnoughPlayersMessage().replace("$amount", minPlayers + "")));
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

    protected void startMatch() {
        state = InstancedRegionState.ONGOING;
        participants = (HashSet<Player>) players.clone();
    }

    protected void endMatch() {
        state = InstancedRegionState.COMPLETED;
        //instances.remove(this);
    }

    protected void resetMatch() {
        state = InstancedRegionState.IDLE;
        HashSet<Player> copy = new HashSet<>(participants);
        copy.forEach(this::removeAnyKind);
        /*
        players.forEach(player -> player.teleport(exitLocation));
        spectators.forEach(player -> {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(exitLocation);
        });
        ((HashSet<Player>) players.clone()).forEach(this::removePlayer);
        ((HashSet<Player>) spectators.clone()).forEach(this::removeSpectator);
         */
        players.clear();
        spectators.clear();
        spectators.clear();
        deathBanners.values().forEach(deathLocation -> deathLocation.clear(false));
        deathBanners.clear();
    }

    private DeathLocation getDeathLocationByPlayer(Player player) {
        for (DeathLocation deathLocation : deathBanners.values())
            if (deathLocation.deadPlayer.equals(player))
                return deathLocation;
        return null;
    }

    public enum InstancedRegionState {
        IDLE, STARTING, ONGOING, COMPLETED
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

        @EventHandler
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

    private class DeathLocation {
        private Block bannerBlock;
        private Player deadPlayer;
        private Location deathLocation = null;
        private ArmorStand nameTag;
        private ArmorStand livesLeft;
        private ArmorStand instructions;

        private DeathLocation(Player player) {
            if (playerLives.get(player) < 1)
                return;
            this.deadPlayer = player;
            this.bannerBlock = player.getLocation().getBlock();
            findBannerLocation(player.getLocation());
            instructions = VisualArmorStand.VisualArmorStand(deathLocation.clone().add(new Vector(0, 2.2, 0)), ChatColorConverter.convert("&2Punch to rez!"));
            nameTag = VisualArmorStand.VisualArmorStand(deathLocation.clone().add(new Vector(0, 2, 0)), player.getDisplayName());
            livesLeft = VisualArmorStand.VisualArmorStand(deathLocation.clone().add(new Vector(0, 1.8, 0)), playerLives.get(deadPlayer) + " lives left!");
            if (deathLocation != null)
                deathBanners.put(bannerBlock, this);

            bannerWatchdog();
        }

        private void findBannerLocation(Location location) {
            if (location.getBlock().getType().isAir())
                setBannerBlock(location);
            else if (location.getY() < 320)
                findBannerLocation(location.add(new Vector(0, 1, 0)));
        }

        private void setBannerBlock(Location location) {
            deathLocation = location;
            bannerBlock = location.getBlock();
            location.getBlock().setType(Material.RED_BANNER, false);
        }

        public void clear(boolean resurrect) {
            bannerBlock.setType(Material.AIR);
            EntityTracker.unregister(nameTag, RemovalReason.EFFECT_TIMEOUT);
            EntityTracker.unregister(instructions, RemovalReason.EFFECT_TIMEOUT);
            EntityTracker.unregister(livesLeft, RemovalReason.EFFECT_TIMEOUT);
            deathBanners.remove(bannerBlock);
            if (resurrect)
                revivePlayer(deadPlayer, this);
        }

        //This is necessary because physics updates might remove the banner while it should still be on there
        public void bannerWatchdog() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!deathBanners.containsKey(bannerBlock)) {
                        cancel();
                        return;
                    }
                    if (bannerBlock.getType().equals(Material.RED_BANNER)) return;
                    EntityTracker.unregister(nameTag, RemovalReason.EFFECT_TIMEOUT);
                    EntityTracker.unregister(instructions, RemovalReason.EFFECT_TIMEOUT);
                    EntityTracker.unregister(livesLeft, RemovalReason.EFFECT_TIMEOUT);
                    findBannerLocation(deathLocation);
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 5, 5);
        }

    }

}
