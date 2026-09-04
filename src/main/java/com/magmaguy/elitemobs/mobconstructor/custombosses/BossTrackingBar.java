package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.wormhole.WormholeNavigation;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Boss tracking bar for location-based boss tracking.
 * This shows boss location and distance information to players who are tracking the boss.
 * For health-based boss bars, see {@link com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay}
 */
public class BossTrackingBar implements BossTrackingLifecycle.Handle {
    private static final String DEFAULT_LOCATION_MESSAGE = "$name: $distance blocks away!";

    private final CustomBossEntity customBossEntity;
    private final Map<Player, BossBar> bossBars = new HashMap<>();
    private final HashSet<Player> trackingPlayers = new HashSet<>();
    private BukkitTask bossBarUpdater;

    public BossTrackingBar(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        start();
    }

    @Override
    public void notifyPlayers() {
        Location bossLoc = customBossEntity.getLocation();
        if (bossLoc == null || bossLoc.getWorld() == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(bossLoc.getWorld())) continue;
            TextComponent interactiveMessage = new TextComponent(MobCombatSettingsConfig.getBossLocationMessage());
            interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs track boss " + customBossEntity.getEliteUUID()));
            interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DefaultConfig.getTrackMessage().replace("$name", customBossEntity.getName())).create()));
            player.spigot().sendMessage(interactiveMessage);
        }
    }

    public void addTrackingPlayer(Player player) {
        if (!customBossEntity.exists()) {
            remove();
            return;
        }
        if (trackingPlayers.contains(player)) {
            removeTrackingPlayer(player);
            return;
        }
        trackingPlayers.add(player);
    }

    public void removeTrackingPlayer(Player player) {
        trackingPlayers.remove(player);
        BossBar bossBar = bossBars.remove(player);
        if (bossBar != null) BossBarOrderManager.hide(player, bossBar);
    }

    public void remove() {
        customBossEntity.removeTracking(this);
    }

    @Override
    public void dispose() {
        bossBars.forEach((player, bossBar) -> BossBarOrderManager.hide(player, bossBar));
        bossBars.clear();
        trackingPlayers.clear();
        if (bossBarUpdater != null)
            bossBarUpdater.cancel();
    }

    /**
     * Used not only for generating the boss bar message, but also the message that appears on the tracking screen for players
     *
     * @param player
     * @param locationString
     * @return
     */
    public String bossBarMessage(Player player, String locationString) {
        String locationMessage = getLocationMessageTemplate();
        Location trackedLocation = getTrackedLocation(player);
        if (trackedLocation == null)
            return MobCombatSettingsConfig.getDefaultOtherWorldBossLocationMessage()
                    .replace("$name", customBossEntity.getName());

        if (locationMessage.contains("$distance") ||
                locationMessage.contains("$location") ||
                locationMessage.contains("$name")) {
            String trackedLocationString = trackedLocation.getBlockX() +
                    ", " + trackedLocation.getBlockY() +
                    ", " + trackedLocation.getBlockZ();
            return locationMessage
                    .replace("$name", customBossEntity.getName())
                    .replace("$distance", "" + (int) trackedLocation.distance(player.getLocation()))
                    .replace("$location", trackedLocationString);
        }

        return locationMessage;

    }

    private void updateBossBar(Player player, BossBar bossBar) {
        if (player == null) return;
        if (bossBar == null) return;
        Location loc = customBossEntity.getLocation();
        if (loc == null || loc.getWorld() == null) return;
        String locationString = loc.getBlockX() +
                ", " + loc.getBlockY() +
                ", " + loc.getBlockZ();
        bossBar.setTitle(bossBarMessage(player, locationString));
        // Clamp to [0,1] — ratio can briefly exceed 1 during dynamic level scaling
        double progress = Math.min(1.0, Math.max(0.0,
                customBossEntity.getHealth() / customBossEntity.getMaxHealth()));
        bossBar.setProgress(progress);
    }

    public void start() {
        bossBarUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                //This can happen on phase changes where boss bars might not be configured on subsequent entities
                if (!customBossEntity.exists()) {
                    cancel();
                    remove();
                    return;
                }

                //Cancel if the boss's world is no longer loaded (e.g. world/chunk unloaded)
                Location bossLocation = customBossEntity.getLocation();
                if (bossLocation == null || bossLocation.getWorld() == null ||
                        Bukkit.getWorld(bossLocation.getWorld().getUID()) == null) {
                    cancel();
                    remove();
                    return;
                }

                trackingPlayers.removeIf(player -> !player.isOnline());
                for (Player player : new HashSet<>(trackingPlayers)) {
                    if (!bossBars.containsKey(player)) createBossBar(player);
                    else updateBossBar(player, bossBars.get(player));
                }

                bossBars.entrySet().removeIf(entry -> {
                    Player player = entry.getKey();
                    boolean shouldRemove = !player.isOnline() || !trackingPlayers.contains(player);
                    if (shouldRemove) BossBarOrderManager.hide(player, entry.getValue());
                    return shouldRemove;
                });
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    private void createBossBar(Player player) {
        Location loc = customBossEntity.getLocation();
        if (loc == null || loc.getWorld() == null) return;
        String locationString = (int) loc.getX() +
                ", " + (int) loc.getY() +
                ", " + (int) loc.getZ();
        BossBar bossBar = Bukkit.createBossBar(bossBarMessage(player, locationString), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);

        if (!customBossEntity.exists()) return;

        bossBars.put(player, bossBar);
        updateBossBar(player, bossBar);
        //The permanent per-boss sort key keeps this bar in the same stack slot on the
        //client no matter when it gets shown relative to other bosses' bars
        BossBarOrderManager.show(player, bossBar, BossBarOrderManager.sortKeyFor(customBossEntity.getEliteUUID()));
    }

    private String getLocationMessageTemplate() {
        String configuredLocationMessage = customBossEntity.getCustomBossesConfigFields().getLocationMessage();
        if (configuredLocationMessage == null || configuredLocationMessage.isBlank())
            return DEFAULT_LOCATION_MESSAGE;
        return configuredLocationMessage;
    }

    private Location getTrackedLocation(Player player) {
        if (player == null) return null;
        Location playerLocation = player.getLocation();
        Location bossLocation = customBossEntity.getLocation();
        return WormholeNavigation.findNavigationTarget(playerLocation, bossLocation);
    }

}
