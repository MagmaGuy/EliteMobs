package wormhole;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.*;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
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
    private String portalMissingMessage = null;
    private String opMessage = null;
    private Color particleColor;
    @Getter
    private ArmorStand armorStand1 = null;
    @Getter
    private ArmorStand armorStand2 = null;

    public Wormhole(WormholeConfigFields wormholeConfigFields) {
        this.wormholeConfigFields = wormholeConfigFields;
        if (wormholeConfigFields.getLocation1().contains(","))
            location1 = ConfigurationLocation.serialize(wormholeConfigFields.getLocation1());
        else
            location1 = getDungeonLocation(wormholeConfigFields.getLocation1());
        if (wormholeConfigFields.getLocation2().contains(","))
            location2 = ConfigurationLocation.serialize(wormholeConfigFields.getLocation2());
        else
            location2 = getDungeonLocation(wormholeConfigFields.getLocation2());
        wormholes.add(this);
        if (location1 != null) start(location1);
        if (location2 != null) start(location2);
        this.particleColor = Color.fromRGB(wormholeConfigFields.getParticleColor());
        if (!wormholeConfigFields.getStyle().equals(WormholeStyle.NONE))
            initializeVisualEffect();
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

    public static void shutdown() {
        for (Wormhole wormhole : wormholes)
            wormhole.stop();
        wormholes.clear();
    }

    private Location getDungeonLocation(String dungeonFilename) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(dungeonFilename);
        if (minidungeon == null) {
            new WarningMessage("Dungeon " + dungeonFilename + " is not a valid dungeon packager name! Wormhole " + wormholeConfigFields.getFilename() + " will not lead anywhere.");
            this.portalMissingMessage = WormholesConfig.getDefaultPortalMissingMessage();
            return null;
        }
        if (!minidungeon.isDownloaded() || !minidungeon.isInstalled()) {
            new InfoMessage("Wormhole " + wormholeConfigFields.getFilename() + " will not lead anywhere because the dungeon " + dungeonFilename + " is not installed!");
            this.portalMissingMessage = WormholesConfig.getDungeonNotInstalledMessage().replace("$dungeonID", minidungeon.getDungeonPackagerConfigFields().getName());
           // TextComponent textComponent1 = new TextComponent("[EliteMobs - OP-only message] Download links are available on ");
           // TextComponent textComponent2 = new TextComponent("https://magmaguy.itch.io/");
           // textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://magmaguy.itch.io/"));
           // TextComponent textComponent3 = new TextComponent(" (free and premium) and ");
           // TextComponent textComponent4 = new TextComponent("https://www.patreon.com/magmaguy");
           // textComponent4.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/magmaguy"));
           // TextComponent textComponent5 = new TextComponent(" (premium). You can check the difference between the two and get support here: ");
           // TextComponent textComponent6 = new TextComponent(DiscordLinks.mainLink);
           // textComponent6.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DiscordLinks.mainLink));

            this.opMessage = "[EliteMobs - OP-only message] Download links are available on https://magmaguy.itch.io/ " +
                    "(free and premium) and https://www.patreon.com/magmaguy (premium). You can check the difference " +
                    "between the two and get support here: " + DiscordLinks.mainLink;

            return null;
        }
        return minidungeon.getTeleportLocation().clone().subtract(minidungeon.getTeleportLocation().getDirection().clone().setY(0).normalize()).add(new Vector(0, 1, 0));
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
            if (this.portalMissingMessage == null)
                player.sendMessage(ChatColorConverter.convert(WormholesConfig.getDefaultPortalMissingMessage()));
            else {
                player.sendMessage(this.portalMissingMessage);
                if (player.isOp() || player.hasPermission("elitemobs.*"))
                    player.sendMessage(opMessage);
            }
            return;
        }
        if (wormholeConfigFields.isBlindPlayer())
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 0));
        player.teleport(destination);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        player.setVelocity(destination.getDirection().normalize());
        playerCooldowns.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> playerCooldowns.remove(player), 20 * 5L);
    }

    private void initializeTextDisplays() {
        initializeTextDisplay1();
        initializeTextDisplay2();
    }

    private void initializeTextDisplay1() {
        if (armorStand1 != null && armorStand1.isValid()) return;
        if (location1 != null && wormholeConfigFields.getLocation1Text() != null && !wormholeConfigFields.getLocation1Text().isEmpty())
            armorStand1 = initializeTextDisplay(location1, wormholeConfigFields.getLocation1Text());
    }

    private void initializeTextDisplay2() {
        if (armorStand2 != null && armorStand2.isValid()) return;
        if (location2 != null && wormholeConfigFields.getLocation2Text() != null && !wormholeConfigFields.getLocation2Text().isEmpty())
            armorStand2 = initializeTextDisplay(location2, wormholeConfigFields.getLocation2Text());
    }

    private ArmorStand initializeTextDisplay(Location location, String locationText) {
        if (location == null || location.getWorld() == null) return null;
        ArmorStand armorStand = location.getWorld().spawn(location.clone().add(new Vector(0, 1.2, 0)), ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand armorStand) {
                armorStand.setCustomName(ChatColorConverter.convert(locationText));
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setPersistent(false);
            }
        });
        EntityTracker.registerVisualEffects(armorStand);
        return armorStand;
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
                initializeTextDisplays();

                if (counter >= cachedRotations.size())
                    counter = 0;

                for (Vector vector : cachedRotations.get(counter)) {
                    Location particleLocation = location.clone().add(vector);
                    location.getWorld().spawnParticle(Particle.REDSTONE, particleLocation.getX(), particleLocation.getY(), particleLocation.getZ(),
                            1, 0, 0, 0,
                            1, new Particle.DustOptions(particleColor, 1));
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
        Vector front = new Vector(.5, 0, 0);
        cachedLocations.add(front);
        //side
        Vector side = new Vector(0, 0, .5);
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
        EntityTracker.unregisterVisualEffect(armorStand1);
        EntityTracker.unregisterVisualEffect(armorStand2);
    }

    public enum WormholeStyle {
        NONE,
        CRYSTAL,
        CUBE
    }

}
