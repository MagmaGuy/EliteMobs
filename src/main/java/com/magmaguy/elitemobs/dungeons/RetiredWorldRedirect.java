package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
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
import java.util.List;
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
            if (destination != null && !PlayerTeleportEvent.teleportPlayer(player, destination.clone()))
                warnOnce(worldName, "Could not move " + player.getName() + " out of retired world " + worldName + ".");
        });
    }

    /**
     * True for the server's default world and for any world a package uses as its own or wormhole world.
     * Reads every package's configuration, so the answer doesn't depend on package initialization order.
     */
    public static boolean isInUse(String worldName) {
        if (!Bukkit.getWorlds().isEmpty() && Bukkit.getWorlds().get(0).getName().equalsIgnoreCase(worldName))
            return true;
        for (Map<String, ContentPackagesConfigFields> packages : List.of(ContentPackagesConfig.getDungeonPackages(),
                ContentPackagesConfig.getEnchantedChallengeDungeonPackages()))
            for (ContentPackagesConfigFields fields : packages.values())
                if (worldName.equalsIgnoreCase(fields.getWorldName()) || worldName.equalsIgnoreCase(fields.getWormholeWorldName()))
                    return true;
        return false;
    }

    /** Null when the world isn't retired, is still in use, or has no arrival point outside the retired worlds. */
    private static Location destination(String worldName) {
        ContentPackagesConfigFields fields = retired.get(worldName);
        if (fields == null) return null;
        if (isInUse(worldName))
            return warnOnce(worldName, "Retired world " + worldName + " of package " + fields.getFilename()
                    + " is the default world or still used by a package; its players are not moved.");
        Location destination = fields.getTeleportLocation();
        if (destination != null && destination.isWorldLoaded() && !retired.containsKey(destination.getWorld().getName()))
            return destination;
        return warnOnce(worldName, "Players in retired world " + worldName + " cannot be moved: package "
                + fields.getFilename() + " has no loaded teleportLocation outside its retired worlds.");
    }

    private static Location warnOnce(String worldName, String message) {
        if (warned.add(worldName)) Logger.warn(message);
        return null;
    }
}
