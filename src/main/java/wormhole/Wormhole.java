package wormhole;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Wormhole {
    private static final HashSet<Wormhole> wormholes = new HashSet<>();
    private final HashSet<Player> playerCooldowns = new HashSet<>();
    private final WormholeConfigFields wormholeConfigFields;
    private final List<Vector> cachedLocations = new ArrayList<>();
    private final List<List<Vector>> cachedRotations = new ArrayList<List<Vector>>();
    private Location location1;
    private Location location2;
    private BukkitTask bukkitTask;

    public Wormhole(WormholeConfigFields wormholeConfigFields) {
        this.wormholeConfigFields = wormholeConfigFields;
        location1 = ConfigurationLocation.serialize(wormholeConfigFields.getLocation1());
        location2 = ConfigurationLocation.serialize(wormholeConfigFields.getLocation2());
        wormholes.add(this);
        if (location1 != null) start(location1);
        if (location2 != null) start(location2);
        if (!wormholeConfigFields.getStyle().equals(WormholeStyle.CRYSTAL)) {
            initializeVisualEffect();
        }
    }

    public static void shutdown() {
        for (Wormhole wormhole : wormholes)
            wormhole.stop();
        wormholes.clear();
    }

    private void start(Location location) {
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers())
                    checkPoint(location, player.getLocation(), player);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }

    private void checkPoint(Location wormholeLocation, Location playerLocation, Player player) {
        if (wormholeLocation == null) return;
        if (playerCooldowns.contains(player)) return;
        if (wormholeConfigFields.getPermission() != null && !wormholeConfigFields.getPermission().isEmpty() &&
                !player.hasPermission(wormholeConfigFields.getPermission()))
            return;
        if (!Objects.equals(wormholeLocation.getWorld(), playerLocation.getWorld())) return;
        if (wormholeLocation.distance(playerLocation) > 2) return;
        if (wormholeConfigFields.getCoinCost() > 0) {
            if (EconomyHandler.checkCurrency(player.getUniqueId()) < wormholeConfigFields.getCoinCost()) {
                player.sendMessage(ChatColorConverter.convert(TranslationConfig.getInsufficientCurrencyForWormholeMessage()).replace("$amount", "" + wormholeConfigFields.getCoinCost()));
                return;
            } else
                EconomyHandler.subtractCurrency(player.getUniqueId(), wormholeConfigFields.getCoinCost());
        }

        Location destination;
        if (wormholeLocation == location1) destination = location2;
        else destination = location1;
        if (destination == null) {
            player.sendMessage(TranslationConfig.getMissingWormholeDestinationMessage());
            return;
        }
        if (wormholeConfigFields.isBlindPlayer())
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 0));
        player.teleport(destination);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        player.setVelocity(destination.getDirection().normalize());
        playerCooldowns.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> playerCooldowns.remove(player), 20 * 3L);
    }

    private void initializeVisualEffect() {
        switch (wormholeConfigFields.getStyle()) {
            case CUBE:
                generateCube();
                break;
            case CRYSTAL:
                generateCrystal();
                break;
            default:
                new WarningMessage("Missing wormhole style for " + wormholeConfigFields.getStyle());
        }
        if (location1 != null)
            doVisualEffect(location1);
        if (location2 != null)
            doVisualEffect(location2);
    }

    private void doVisualEffect(Location location) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (bukkitTask.isCancelled()) {
                    cancel();
                    return;
                }

                if (!ChunkLocationChecker.locationIsLoaded(location)) return;

                if (counter >= cachedRotations.size())
                    counter = 0;

                for (Vector vector : cachedRotations.get(counter)) {
                    Location particleLocation = location.clone().add(vector);
                    location.getWorld().spawnParticle(Particle.REDSTONE, particleLocation.getX(), particleLocation.getY(), particleLocation.getZ(),
                            1, 0, 0, 0,
                            1, new Particle.DustOptions(Color.PURPLE, 1));
                }

                counter++;

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    private void generateCrystal() {
        //top
        Vector top = new Vector(0, 1, 0);
        cachedLocations.add(top);
        //bottom
        Vector bottom = new Vector(0, -1, 0);
        cachedLocations.add(bottom);
        //front
        Vector front = new Vector(.3, 0, 0);
        cachedLocations.add(front);
        //side
        Vector side = new Vector(0, 0, .3);
        cachedLocations.add(side);

        finishCuboidInitialization(top, bottom, front, side);

    }

    private void generateCube() {
        //top
        Vector top = new Vector(0, 1, 0);
        cachedLocations.add(top);
        //bottom
        Vector bottom = new Vector(0, -1, 0);
        cachedLocations.add(bottom);
        //front
        Vector front = new Vector(1, 0, 0);
        cachedLocations.add(front);
        //side
        Vector side = new Vector(0, 0, 1);
        cachedLocations.add(side);

        finishCuboidInitialization(top, bottom, front, side);

    }

    private void finishCuboidInitialization(Vector top, Vector bottom, Vector front, Vector side) {
        trace(top, front);
        trace(bottom, front);
        trace(front, side);

        List<Vector> otherSidesList = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            double rotation = Math.PI / 2 * i;
            for (Vector vector : cachedLocations) {
                Vector rotatedVector = vector.clone().rotateAroundY(rotation);
                if (rotatedVector.getX() != vector.getX() || rotatedVector.getZ() != vector.getZ())
                    otherSidesList.add(rotatedVector);
            }
        }

        cachedLocations.addAll(otherSidesList);

        cacheRotations();
    }

    private void cacheRotations() {
        double rotationPointCount = 100;
        for (int i = 0; i < rotationPointCount; i++) {
            double rotation = (Math.PI * 2) / rotationPointCount * i;
            List<Vector> vectors = new ArrayList<>();
            for (Vector vector : cachedLocations) {
                vectors.add(vector.clone().rotateAroundY(rotation));
            }
            cachedRotations.add(vectors);
        }
    }

    private void trace(Vector source, Vector target) {
        Vector ray = target.clone().subtract(source).normalize().multiply(.2);
        Vector clonedSource = source.clone();
        int counter = 0;
        while (clonedSource.distance(target) > 0.1 && counter < 20) {
            clonedSource = clonedSource.add(ray);
            cachedLocations.add(clonedSource.clone());
            counter++;
        }
    }

    private void stop() {
        bukkitTask.cancel();
    }

    public enum WormholeStyle {
        NONE,
        CRYSTAL,
        CUBE
    }

}
