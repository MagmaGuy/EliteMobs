package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Wormhole {
    @Getter
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
    private final Color particleColor;

    @Getter
    private List<List<Vector>> cachedRotations = new ArrayList<>();

    public Wormhole(WormholeConfigFields wormholeConfigFields) {
        this.wormholeConfigFields = wormholeConfigFields;
        this.particleColor = Color.fromRGB(wormholeConfigFields.getParticleColor());
        if (!wormholeConfigFields.getStyle().equals(WormholeStyle.NONE)) {
            this.cachedRotations = new VisualEffects(wormholeConfigFields).getCachedRotations();
        }
        wormholeEntry1 = new WormholeEntry(this, getWormholeConfigFields().getLocation1(), 1);
        wormholeEntry2 = new WormholeEntry(this, getWormholeConfigFields().getLocation2(), 2);
        wormholes.add(this);
    }

    public static void shutdown() {
        for (Wormhole wormhole : wormholes)
            wormhole.stop();
        wormholes.clear();
        WormholeEntry.getWormholeEntries().clear();
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

    public void onDungeonInstall(String dungeonFilename) {
        if (getWormholeEntry1().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry1().onDungeonInstall();
        } else if (getWormholeEntry2().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry2().onDungeonInstall();
        }
    }

    public void onDungeonUninstall(String dungeonFilename) {
        if (getWormholeEntry1().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry1().onDungeonUninstall();
        } else if (getWormholeEntry2().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry2().onDungeonUninstall();
        }
    }

}
