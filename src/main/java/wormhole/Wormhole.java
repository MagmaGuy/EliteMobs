package wormhole;

import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Wormhole {
    private static final HashSet<Wormhole> wormholes = new HashSet<>();
    @Getter
    private static final HashSet<Player> playerCooldowns = new HashSet<>();
    @Getter
    private final WormholeConfigFields wormholeConfigFields;
    @Getter
    private final WormholeEntry wormholeEntry1;
    @Getter
    private final WormholeEntry wormholeEntry2;
    @Getter
    private List<List<Vector>> cachedRotations = new ArrayList<List<Vector>>();
    @Getter
    @Setter
    private String portalMissingMessage = null;
    @Getter
    @Setter
    private String opMessage = null;
    @Getter
    private Color particleColor;

    public Wormhole(WormholeConfigFields wormholeConfigFields) {
        this.wormholeConfigFields = wormholeConfigFields;
        this.particleColor = Color.fromRGB(wormholeConfigFields.getParticleColor());
        if (!wormholeConfigFields.getStyle().equals(WormholeStyle.NONE)) {
            this.cachedRotations = new VisualEffects(wormholeConfigFields).getCachedRotations();
        }
        if (wormholeConfigFields.getLocation1().contains(",")) {
            wormholeEntry1 = new WormholeEntry(ConfigurationLocation.serialize(wormholeConfigFields.getLocation1()), this, getWormholeConfigFields().getLocation1Text());
        } else {
            wormholeEntry1 = new WormholeEntry(getDungeonLocation(wormholeConfigFields.getLocation1()), this, getWormholeConfigFields().getLocation1Text());
        }
        if (wormholeConfigFields.getLocation2().contains(",")) {
            wormholeEntry2 = new WormholeEntry(ConfigurationLocation.serialize(wormholeConfigFields.getLocation2()), this, getWormholeConfigFields().getLocation2Text());
        } else {
            wormholeEntry2 = new WormholeEntry(getDungeonLocation(wormholeConfigFields.getLocation2()), this, getWormholeConfigFields().getLocation2Text());
        }
        wormholes.add(this);
    }

    private Location getDungeonLocation(String dungeonFilename) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(dungeonFilename);
        if (minidungeon == null) {
            new WarningMessage("Dungeon " + dungeonFilename + " is not a valid dungeon packager name! Wormhole " + getWormholeConfigFields().getFilename() + " will not lead anywhere.");
            setPortalMissingMessage(WormholesConfig.getDefaultPortalMissingMessage());
            return null;
        }
        if (!minidungeon.isDownloaded() || !minidungeon.isInstalled()) {
            new InfoMessage("Wormhole " + getWormholeConfigFields().getFilename() + " will not lead anywhere because the dungeon " + dungeonFilename + " is not installed!");
            setPortalMissingMessage(WormholesConfig.getDungeonNotInstalledMessage().replace("$dungeonID", minidungeon.getDungeonPackagerConfigFields().getName()));

            this.opMessage = "&8[EliteMobs - OP-only message] &fDownload links are available on &9https://magmaguy.itch.io/ &f" +
                    "(free and premium) and &9https://www.patreon.com/magmaguy &f(premium). You can check the difference " +
                    "between the two and get support here: " + DiscordLinks.mainLink;

            return null;
        }
        return minidungeon.getTeleportLocation().clone().subtract(minidungeon.getTeleportLocation().getDirection().clone().setY(0).normalize().multiply(getWormholeConfigFields().getSizeMultiplier())).add(new Vector(0, 1, 0));
    }

    public static void shutdown() {
        for (Wormhole wormhole : wormholes)
            wormhole.stop();
        wormholes.clear();
    }

    private void stop() {
        wormholeEntry1.stop();
        wormholeEntry2.stop();
    }

    public enum WormholeStyle {
        NONE,
        CRYSTAL,
        ICOSAHEDRON,
        CUBE
    }

}
