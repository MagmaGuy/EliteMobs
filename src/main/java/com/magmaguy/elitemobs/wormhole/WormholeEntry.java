package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class WormholeEntry implements PersistentObject {
    @Getter
    private static final Set<WormholeEntry> wormholeEntries = new HashSet<>();
    @Getter
    private final Wormhole wormhole;
    private final int wormholeNumber;
    @Getter
    private Location location;
    @Getter
    private String locationString;
    @Getter
    private String armorStandText;
    @Getter
    @Setter
    private BukkitTask wormholeTask;
    @Getter
    @Setter
    private ArmorStand text;
    private String worldName;
    @Getter
    @Setter
    private String portalMissingMessage = null;
    @Getter
    @Setter
    private String opMessage = null;
    private PersistentObjectHandler persistentObjectHandler = null;

    public WormholeEntry(Wormhole wormhole, String locationString, int wormholeNumber) {
        this.wormhole = wormhole;
        this.wormholeNumber = wormholeNumber;
        this.locationString = locationString;
        if (locationString == null) {
            new WarningMessage("Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " is missing a wormhole location! Fix this!");
            return;
        }
        setLocationFromConfiguration();
        if (wormholeNumber == 1) this.armorStandText = wormhole.getWormholeConfigFields().getLocation1Text();
        else this.armorStandText = wormhole.getWormholeConfigFields().getLocation2Text();

        persistentObjectHandler = new PersistentObjectHandler(this);
        if (ChunkLocationChecker.locationIsLoaded(location)) chunkLoad();
    }

    private Location getDungeonLocation() {
        EMPackage emPackage = EMPackage.getEmPackages().get(locationString);
        if (emPackage == null) {
            new WarningMessage("Dungeon " + locationString + " is not a valid dungeon packager name! Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " will not lead anywhere.");
            setPortalMissingMessage(WormholesConfig.getDefaultPortalMissingMessage());
            return null;
        }
        if (!emPackage.isDownloaded() || !emPackage.isInstalled()) {
            //new InfoMessage("Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " will not lead anywhere because the dungeon " + locationString + " is not installed!");
            setPortalMissingMessage(WormholesConfig.getDungeonNotInstalledMessage().replace("$dungeonID", emPackage.getDungeonPackagerConfigFields().getName()));

            this.opMessage = ChatColorConverter.convert("&8[EliteMobs - OP-only message] &fDownload links are available on &9https://magmaguy.itch.io/ &f" + "(free and premium) and &9https://www.patreon.com/magmaguy &f(premium). You can check the difference " + "between the two and get support here: " + DiscordLinks.mainLink);

            if (emPackage instanceof SchematicPackage) return null;
        }
        Location teleportLocation = emPackage.getDungeonPackagerConfigFields().getTeleportLocation();
        if (teleportLocation == null) return null;
        Vector offsetVector = teleportLocation.getDirection().clone().setY(0).normalize().multiply(1.5 * wormhole.getWormholeConfigFields().getSizeMultiplier()).setY(-1 * wormhole.getWormholeConfigFields().getSizeMultiplier());

        worldName = emPackage.getDungeonPackagerConfigFields().getWorldName();

        return teleportLocation.clone().subtract(offsetVector);
    }

    private void setLocationFromConfiguration() {
        if (locationString.contains(",")) {
            this.worldName = ConfigurationLocation.worldName(locationString);
            this.location = ConfigurationLocation.serialize(locationString);
        } else {
            this.location = getDungeonLocation();
        }
    }

    public void onDungeonInstall() {
        this.location = getDungeonLocation();
        if (persistentObjectHandler != null) persistentObjectHandler = new PersistentObjectHandler(this);
        persistentObjectHandler = new PersistentObjectHandler(this);
        chunkLoad();
    }

    public void onDungeonUninstall() {
        stop();
        location = null;
        wormholeTask = null;
    }

    @Override
    public void chunkLoad() {
        initializeTextDisplay();
        if (wormholeTask != null && !wormholeTask.isCancelled()) wormholeTask.cancel();
        if (location == null) setLocationFromConfiguration();
        wormholeTask = WormholeTask.startWormholeTask(this);
    }

    @Override
    public void chunkUnload() {
        //This is handled by the repeating task itself. Also there seems to be some bug with this unload, it seems arbitrary
    }

    @Override
    public void worldLoad(World world) {
        getDungeonLocation();
        if (ChunkLocationChecker.locationIsLoaded(location)) chunkLoad();
    }

    @Override
    public void worldUnload() {
        location.setWorld(null);
        stop();
    }

    @Override
    public Location getPersistentLocation() {
        return location;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    public void stop() {
        if (wormholeTask != null) {
            wormholeTask.cancel();
        }
    }

    public void updateLocation(Player player) {
        if (wormholeTask != null) {
            wormholeTask.cancel();
            wormholeTask = null;
        }
        locationString = ConfigurationLocation.deserialize(player.getLocation());
        location = player.getLocation().add(new Vector(0, 1 * wormhole.getWormholeConfigFields().getSizeMultiplier(), 0));
        wormhole.getWormholeConfigFields().setWormholeEntryLocation(location, wormholeNumber);
        wormholeTask = WormholeTask.startWormholeTask(this);
        if (persistentObjectHandler != null) persistentObjectHandler.remove();
        persistentObjectHandler = new PersistentObjectHandler(this);
    }

    private void initializeTextDisplay() {
        if (text != null && text.isValid() && !text.isEmpty()) return;
        if (armorStandText == null) return;
        if (location == null || location.getWorld() == null) return;
        text = location.getWorld().spawn(location.clone().add(new Vector(0, 1.2, 0).multiply(wormhole.getWormholeConfigFields().getSizeMultiplier())), ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand armorStand) {
                armorStand.setCustomName(ChatColorConverter.convert(armorStandText));
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setPersistent(false);
            }
        });
        EntityTracker.registerVisualEffects(text);
    }

}
