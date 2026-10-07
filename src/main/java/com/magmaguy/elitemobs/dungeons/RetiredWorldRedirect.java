package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** A package's retired worlds still load if present, and anyone inside is sent to the package's arrival point. */
public final class RetiredWorldRedirect implements Listener {
    private static final Map<String, ContentPackagesConfigFields> retired = new HashMap<>();
    private static final Set<String> warned = new HashSet<>();

    public static void register(World world, ContentPackagesConfigFields fields) {
        if (world == null) return;
        retired.put(world.getName(), fields);
        // A reload keeps the world loaded, so players can already be inside it.
        for (Player player : world.getPlayers()) redirect(player);
    }

    public static void unregister(String worldName) {
        retired.remove(worldName);
        warned.remove(worldName);
    }

    public static void shutdown() {
        retired.clear();
        warned.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        redirect(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        redirect(event.getPlayer());
    }

    private static void redirect(Player player) {
        String worldName = player.getWorld().getName();
        if (destination(worldName) == null) return;
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (!player.isOnline() || !player.getWorld().getName().equals(worldName)) return;
            Location destination = destination(worldName);
            if (destination != null) PlayerTeleportEvent.teleportPlayer(player, destination.clone());
        });
    }

    /** Null when the world isn't retired, or when the arrival point is missing or would land in a retired world. */
    private static Location destination(String worldName) {
        ContentPackagesConfigFields fields = retired.get(worldName);
        if (fields == null) return null;
        Location destination = fields.getTeleportLocation();
        if (destination != null && destination.getWorld() != null
                && !retired.containsKey(destination.getWorld().getName()))
            return destination;
        if (warned.add(worldName))
            Logger.warn("Players in retired world " + worldName + " cannot be moved: package " + fields.getFilename()
                    + " has no teleportLocation outside its retired worlds.");
        return null;
    }
}
