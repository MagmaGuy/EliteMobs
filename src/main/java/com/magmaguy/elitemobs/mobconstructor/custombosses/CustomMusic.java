package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;


public class CustomMusic {
    private static final HashMap<Player, CustomMusic> playerSongSingleton = new HashMap<>();
    private static final HashMap<World, CustomMusic> dungeonMusic = new HashMap<>();
    private final HashMap<Player, CustomMusic> players = new HashMap<>();
    private final ContentType contentType;
    private CustomBossEntity customBossEntity = null;
    private ContentPackagesConfigFields contentPackagesConfigFields = null;
    @Getter
    private String name;
    @Getter
    private int durationTicks;
    @Getter
    private String name2 = null;
    @Getter
    private int durationTicks2 = -1;
    private BukkitTask bossScannerTask = null;
    private BukkitTask songTask = null;
    private World world;

    //Format: name=rsp.name length=durations_milliseconds->name=rsp.name length=duration_milliseconds
    public CustomMusic(String rawString, CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        contentType = ContentType.BOSS;
        if (!rawString.contains("->")) {
            parse(rawString, 1);
        } else {
            String[] rawEntries = rawString.split("->");
            parse(rawEntries[0], 1);
            parse(rawEntries[1], 2);
        }
    }

    public CustomMusic(String rawString, ContentPackagesConfigFields contentPackagesConfigFields, World world) {
        this.world = world;
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        contentType = ContentType.DUNGEON;
        if (!rawString.contains("->")) {
            parse(rawString, 1);
        } else {
            String[] rawEntries = rawString.split("->");
            parse(rawEntries[0], 1);
            parse(rawEntries[1], 2);
        }
        dungeonMusic.put(world, this);
    }

    public static void shutdown() {
        dungeonMusic.clear();
        playerSongSingleton.clear();
    }

    private void parse(String rawString, int entryNumber) {
        String[] strings = rawString.split(" ");
        for (String string : strings) {
            String[] parsed = string.split("=");
            switch (parsed[0]) {
                case "name":
                    if (entryNumber == 1) name = parsed[1];
                    else name2 = parsed[1];
                    break;
                case "length":
                    if (entryNumber == 1) {
                        durationTicks = (int) (Integer.parseInt(parsed[1]) / 1000D * 20D);
                    } else {
                        durationTicks2 = (int) (Integer.parseInt(parsed[1]) / 1000D * 20D);
                    }
                    break;
                default:
                    Logger.warn("Failed to get value for boss music!");
            }
        }
    }

    public void start(CustomBossEntity customBossEntity) {
        if (bossScannerTask != null) {
            bossScannerTask.cancel();
        }
        bossScannerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!customBossEntity.exists()) {
                    stop();
                    return;
                }
                play(customBossEntity.getLocation(), customBossEntity.getCustomBossesConfigFields().getFollowDistance());
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }

    public void stop() {
        if (bossScannerTask != null) {
            bossScannerTask.cancel();
        }
        for (Map.Entry<Player, CustomMusic> entry : players.entrySet()) {
            entry.getKey().stopSound(name);
            if (name2 != null)
                entry.getKey().stopSound(name2);
            entry.getValue().songTask.cancel();
        }
    }

    private void play(Location location, double range) {
        // Cache the squared range for faster distance checks
        double rangeSquared = range * range;

        // Get players directly instead of all entities
        for (Player player : location.getWorld().getPlayers()) {
            // Fast distance check using distanceSquared
            if (player.getLocation().distanceSquared(location) > rangeSquared)
                continue;

            CustomMusic currentCustomMusic = playerSongSingleton.get(player);
            if (currentCustomMusic != null && !currentCustomMusic.equals(this)) {
                currentCustomMusic.songTask.cancel();
                player.stopSound(currentCustomMusic.name);
                if (currentCustomMusic.name2 != null) {
                    try {
                        player.stopSound(currentCustomMusic.name2);
                    } catch (Exception e) {
                        Logger.warn("Error trying to stop song, key was " + name2 + " . Reporting this to the author would be appreciated. This does not break anything.");
                    }
                }
                playerSongSingleton.remove(player);
            }

            if (!players.containsKey(player)) {
                player.playSound(player.getLocation(), name, SoundCategory.MUSIC, 1f, 1f);
                startLoopingTask(player, durationTicks);
            }
        }
    }

    private void play(Player player) {
        startLoopingTask(player, durationTicks);
        players.put(player, this);
        //Boss music overrides dungeon music
        if (playerSongSingleton.containsKey(player) && !players.get(player).equals(this)) return;
        player.playSound(player.getLocation(), name, SoundCategory.MUSIC, 1f, 1f);
        playerSongSingleton.put(player, this);
    }

    private void startLoopingTask(Player player, int durationTicks) {
        //Case for a song with no transition
        CustomMusic customMusic = this;
        if (name2 == null) {
            songTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (contentType == ContentType.BOSS && !customBossEntity.exists() ||
                            contentType == ContentType.BOSS && player.getLocation().distanceSquared(customBossEntity.getLivingEntity().getLocation()) > Math.pow(customBossEntity.getFollowDistance() * 1.5, 2) ||
                            contentType == ContentType.DUNGEON && !player.getWorld().equals(world)) {
                        cancel();
                        players.remove(player);
                        playerSongSingleton.remove(player);
                        return;
                    }
                    if (playerSongSingleton.containsKey(player) && !players.get(player).equals(customMusic)) return;
                    if (!playerSongSingleton.containsKey(player)) playerSongSingleton.put(player, customMusic);
                    player.playSound(player.getLocation(), name, SoundCategory.MUSIC,1f, 1f);
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, durationTicks);
        }
        //case for a song with a transition
        else {
            player.playSound(player.getLocation(), name, SoundCategory.MUSIC, 1f, 1f);
            songTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (contentType == ContentType.BOSS && !customBossEntity.exists() ||
                            contentType == ContentType.BOSS && player.getLocation().distanceSquared(customBossEntity.getLivingEntity().getLocation()) > Math.pow(customBossEntity.getFollowDistance() * 1.5, 2) ||
                            contentType == ContentType.DUNGEON && !player.getWorld().equals(world)) {
                        cancel();
                        players.remove(player);
                        playerSongSingleton.remove(player);
                        return;
                    }
                    player.playSound(player.getLocation(), name2, SoundCategory.MUSIC,1f, 1f);
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, durationTicks, durationTicks2);
        }

        players.put(player, this);
    }

    private enum ContentType {
        BOSS,
        DUNGEON
    }

    public static class CustomMusicEvents implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTeleport(PlayerTeleportEvent event) {
            EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getDestination().getWorld().getUID());
            if (eliteMobsWorld == null || eliteMobsWorld.getContentPackagesConfigFields().getSong() == null) return;
            CustomMusic customMusic = dungeonMusic.get(event.getDestination().getWorld());
            if (customMusic == null) {
                Logger.warn("aFailed to get custom music for " + event.getDestination().getWorld().getName());
                return;
            }
            //Wait for a second after teleporting, just to make sure
            new BukkitRunnable() {
                @Override
                public void run() {
                    customMusic.play(event.getPlayer());
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onLogin(PlayerJoinEvent event) {
            EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getPlayer().getWorld().getUID());
            if (eliteMobsWorld == null || eliteMobsWorld.getContentPackagesConfigFields().getSong() == null) return;
            CustomMusic customMusic = dungeonMusic.get(event.getPlayer().getWorld());
            if (customMusic == null) {
                Logger.warn("Failed to get custom music for " + event.getPlayer().getWorld().getName());
                return;
            }
            //Wait for a second after teleporting, just to make sure
            new BukkitRunnable() {
                @Override
                public void run() {
                    customMusic.play(event.getPlayer());
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20);
        }
    }
}
