package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;

public class Wormhole {
    @Getter
    private static final HashSet<Wormhole> wormholes = new HashSet<>();
    // Instanced wormhole configs keyed by blueprint world name, and the live copies keyed by instance world.
    private static final Map<String, List<WormholeConfigFields>> instancedBlueprints = new HashMap<>();
    private static final Map<UUID, List<Wormhole>> instancedWormholes = new HashMap<>();

    @Getter
    private final WormholeConfigFields wormholeConfigFields;
    @Getter
    private final WormholeEntry wormholeEntry1;
    // Null for a one-ended flight portal.
    @Getter
    private final WormholeEntry wormholeEntry2;
    @Getter
    private final Color particleColor;
    // The dungeon instance world this copy lives in; null for wormholes loaded straight from their config.
    @Getter
    private final String instanceWorldName;
    @Getter
    private List<List<Vector>> cachedRotations = new ArrayList<>();


    public Wormhole(WormholeConfigFields wormholeConfigFields) {
        this(wormholeConfigFields, null);
    }

    private Wormhole(WormholeConfigFields wormholeConfigFields, String instanceWorldName) {
        this.wormholeConfigFields = wormholeConfigFields;
        this.instanceWorldName = instanceWorldName;
        this.particleColor = Color.fromRGB(wormholeConfigFields.getParticleColor());
        if (wormholeConfigFields.getStyle() != null && !wormholeConfigFields.getStyle().equals(WormholeStyle.NONE)) {
            this.cachedRotations = new ArrayList<>(new VisualEffects(wormholeConfigFields).getCachedRotations());
        }
        wormholeEntry1 = new WormholeEntry(this, rebind(getWormholeConfigFields().getLocation1(), instanceWorldName), 1);
        wormholeEntry2 = wormholeConfigFields.isFlightPortal() && wormholeConfigFields.getLocation2() == null
                ? null : new WormholeEntry(this, rebind(getWormholeConfigFields().getLocation2(), instanceWorldName), 2);
        wormholes.add(this);

        // Make sure the manager is initialized
        WormholeManager.getInstance(false);
    }

    /** Instanced wormholes are created per dungeon copy, like instanced NPCs. */
    public static void registerInstancedBlueprint(WormholeConfigFields fields) {
        String blueprintWorld = fields.getLocation1().split(",")[0];
        instancedBlueprints.computeIfAbsent(blueprintWorld, ignored -> new ArrayList<>()).add(fields);
    }

    public static void initializeInstancedWormholes(String blueprintWorldName, World instanceWorld) {
        for (WormholeConfigFields fields : instancedBlueprints.getOrDefault(blueprintWorldName, List.of()))
            instancedWormholes.computeIfAbsent(instanceWorld.getUID(), ignored -> new ArrayList<>())
                    .add(new Wormhole(fields, instanceWorld.getName()));
    }

    public static void removeInstancedWormholes(World instanceWorld) {
        List<Wormhole> removed = instancedWormholes.remove(instanceWorld.getUID());
        if (removed == null) return;
        for (Wormhole wormhole : removed) {
            wormhole.stop();
            wormholes.remove(wormhole);
            WormholeEntry.getWormholeEntries().remove(wormhole.wormholeEntry1);
            if (wormhole.wormholeEntry2 != null) WormholeEntry.getWormholeEntries().remove(wormhole.wormholeEntry2);
        }
    }

    // Moves a blueprint coordinate string into the instance world; package filenames and null pass through.
    private static String rebind(String location, String instanceWorldName) {
        if (instanceWorldName == null || location == null || !location.contains(",")) return location;
        return instanceWorldName + location.substring(location.indexOf(','));
    }

    public static void shutdown() {
        // Shutdown the centralized manager
        WormholeManager.getInstance(true).shutdown();

        // Clean up wormhole entries. Instanced copies are also in wormholes; stopping twice is harmless.
        for (Wormhole wormhole : wormholes) {
            wormhole.stop();
        }
        for (List<Wormhole> copies : instancedWormholes.values()) {
            copies.forEach(Wormhole::stop);
        }

        wormholes.clear();
        instancedWormholes.clear();
        instancedBlueprints.clear();
        WormholeEntry.getWormholeEntries().clear();

    }

    /** Instance copies share their blueprint's config file, so they must never write locations back to it. */
    public boolean isInstanceCopy() {
        return instanceWorldName != null;
    }

    private void stop() {
        wormholeEntry1.stop();
        if (wormholeEntry2 != null) wormholeEntry2.stop();
    }

    public void onDungeonInstall(String dungeonFilename) {
        if (getWormholeEntry1() != null &&
                getWormholeEntry1().getLocationString() != null &&
                getWormholeEntry1().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry1().onDungeonInstall();
        } else if (getWormholeEntry2() != null &&
                getWormholeEntry2().getLocationString() != null &&
                getWormholeEntry2().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry2().onDungeonInstall();
        }
    }

    public void onDungeonUninstall(String dungeonFilename) {
        if (getWormholeEntry1() != null &&
                getWormholeEntry1().getLocationString() != null &&
                getWormholeEntry1().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry1().onDungeonUninstall();
        } else if (getWormholeEntry2() != null &&
                getWormholeEntry2().getLocationString() != null &&
                getWormholeEntry2().getLocationString().equals(dungeonFilename)) {
            getWormholeEntry2().onDungeonUninstall();
        }
    }

    public enum WormholeStyle {
        NONE,
        CRYSTAL,
        ICOSAHEDRON,
        CUBE
    }
}
