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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class CustomMusic {
    private static final HashMap<UUID, CustomMusic> playerSongSingleton = new HashMap<>();
    private static final HashMap<UUID, CustomMusic> dungeonMusic = new HashMap<>();
    private static final Set<CustomMusic> activeMusic = new HashSet<>();
    private final PlayerTaskRegistry playerTasks = new PlayerTaskRegistry();
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
        CustomMusic previousMusic = dungeonMusic.put(world.getUID(), this);
        if (previousMusic != null && previousMusic != this) previousMusic.stop();
    }

    public static void shutdown() {
        Set<CustomMusic> musicToStop = new HashSet<>(activeMusic);
        musicToStop.addAll(dungeonMusic.values());
        musicToStop.addAll(playerSongSingleton.values());
        musicToStop.forEach(CustomMusic::stop);
        dungeonMusic.clear();
        playerSongSingleton.clear();
        activeMusic.clear();
    }

    public static void removeDungeonMusic(UUID worldUUID) {
        CustomMusic customMusic = dungeonMusic.remove(worldUUID);
        if (customMusic == null) return;
        customMusic.stop();
        customMusic.world = null;
        playerSongSingleton.entrySet().removeIf(entry -> entry.getValue().equals(customMusic));
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
        if (bossScannerTask != null) bossScannerTask.cancel();
        activeMusic.add(this);
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
        if (bossScannerTask != null) bossScannerTask.cancel();
        bossScannerTask = null;
        Set<UUID> playerIds = playerTasks.playerIds();
        playerTasks.cancelAll();
        for (UUID playerId : playerIds) stopSounds(playerId);
        playerSongSingleton.entrySet().removeIf(entry -> entry.getValue() == this);
        activeMusic.remove(this);
    }

    private void play(Location location, double range) {
        for (UUID playerId : playerTasks.playerIds(PlayerTaskRegistry.Role.PLAYBACK_LOOP)) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (!isPlaybackEligible(player)) stopPlayer(playerId, true);
        }
        if (location == null || location.getWorld() == null) return;
        for (Player player : location.getWorld().getPlayers())
            if (MusicPlaybackEligibility.isWithinRange(player.getLocation(), location, range))
                beginPlayback(player);
    }

    private void play(Player player) {
        beginPlayback(player);
    }

    private void beginPlayback(Player player) {
        if (!isPlaybackEligible(player)) return;
        UUID playerId = player.getUniqueId();
        CustomMusic currentMusic = playerSongSingleton.get(playerId);
        if (currentMusic != null && currentMusic != this) {
            //Dungeon ambience never interrupts an active boss track.
            if (contentType == ContentType.DUNGEON && currentMusic.contentType == ContentType.BOSS &&
                    currentMusic.playerTasks.contains(playerId, PlayerTaskRegistry.Role.PLAYBACK_LOOP))
                return;
            currentMusic.stopPlayer(playerId, true);
        }
        if (playerTasks.contains(playerId, PlayerTaskRegistry.Role.PLAYBACK_LOOP)) return;
        playerTasks.cancel(playerId, PlayerTaskRegistry.Role.DELAYED_START);
        playerSongSingleton.put(playerId, this);
        player.playSound(player.getLocation(), name, SoundCategory.MUSIC, 1f, 1f);
        startLoopingTask(playerId);
    }

    private void startLoopingTask(UUID playerId) {
        String loopingTrack = name2 == null ? name : name2;
        long initialDelay = Math.max(1, durationTicks);
        long period = Math.max(1, name2 == null ? durationTicks : durationTicks2);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = org.bukkit.Bukkit.getPlayer(playerId);
                if (playerSongSingleton.get(playerId) != CustomMusic.this || !isPlaybackEligible(player)) {
                    stopPlayer(playerId, true);
                    return;
                }
                player.playSound(player.getLocation(), loopingTrack, SoundCategory.MUSIC, 1f, 1f);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, initialDelay, period);
        playerTasks.replace(playerId, PlayerTaskRegistry.Role.PLAYBACK_LOOP, task::cancel);
        activeMusic.add(this);
    }

    private boolean isPlaybackEligible(Player player) {
        if (player == null || !player.isOnline()) return false;
        if (contentType == ContentType.DUNGEON)
            return world != null && player.getWorld().equals(world);
        if (customBossEntity == null || !customBossEntity.exists() || customBossEntity.getLivingEntity() == null)
            return false;
        return MusicPlaybackEligibility.isWithinRange(
                player.getLocation(),
                customBossEntity.getLivingEntity().getLocation(),
                customBossEntity.getFollowDistance() * 1.5);
    }

    private void stopPlayer(UUID playerId, boolean stopSound) {
        playerTasks.cancelPlayer(playerId);
        playerSongSingleton.remove(playerId, this);
        if (stopSound) stopSounds(playerId);
    }

    private void stopSounds(UUID playerId) {
        Player player = org.bukkit.Bukkit.getPlayer(playerId);
        if (player == null) return;
        player.stopSound(name);
        if (name2 != null) player.stopSound(name2);
    }

    private void schedulePlay(UUID playerId) {
        playerTasks.cancel(playerId, PlayerTaskRegistry.Role.DELAYED_START);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                playerTasks.release(playerId, PlayerTaskRegistry.Role.DELAYED_START);
                play(org.bukkit.Bukkit.getPlayer(playerId));
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20);
        playerTasks.replace(playerId, PlayerTaskRegistry.Role.DELAYED_START, task::cancel);
        activeMusic.add(this);
    }

    private enum ContentType {
        BOSS,
        DUNGEON
    }

    public static class CustomMusicEvents implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTeleport(PlayerTeleportEvent event) {
            if (event.getDestination() == null || event.getDestination().getWorld() == null) return;
            UUID playerId = event.getPlayer().getUniqueId();
            CustomMusic currentMusic = playerSongSingleton.get(playerId);
            if (currentMusic != null && currentMusic.contentType == ContentType.DUNGEON &&
                    !event.getDestination().getWorld().equals(currentMusic.world))
                currentMusic.stopPlayer(playerId, true);
            EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getDestination().getWorld().getUID());
            if (eliteMobsWorld == null || eliteMobsWorld.getContentPackagesConfigFields().getSong() == null) return;
            CustomMusic customMusic = dungeonMusic.get(event.getDestination().getWorld().getUID());
            if (customMusic == null) {
                Logger.warn("Failed to get custom music for " + event.getDestination().getWorld().getName());
                return;
            }
            customMusic.schedulePlay(playerId);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onLogin(PlayerJoinEvent event) {
            EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getPlayer().getWorld().getUID());
            if (eliteMobsWorld == null || eliteMobsWorld.getContentPackagesConfigFields().getSong() == null) return;
            CustomMusic customMusic = dungeonMusic.get(event.getPlayer().getWorld().getUID());
            if (customMusic == null) {
                Logger.warn("Failed to get custom music for " + event.getPlayer().getWorld().getName());
                return;
            }
            customMusic.schedulePlay(event.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event) {
            UUID playerId = event.getPlayer().getUniqueId();
            for (CustomMusic customMusic : new HashSet<>(activeMusic))
                customMusic.stopPlayer(playerId, false);
        }
    }
}
