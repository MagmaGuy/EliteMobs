package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.DrawLine;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ChunkLocationChecker;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WormholeEntry implements PersistentObject {
    @Getter
    private static final Set<WormholeEntry> wormholeEntries = new HashSet<>();
    private static final float LINE_WIDTH = 0.04f;
    // Map of RGB colors to concrete materials
    private static final Material[] CONCRETE_COLORS_BY_RGB = {
            Material.WHITE_CONCRETE,      // 0xFFFFFF
            Material.ORANGE_CONCRETE,     // 0xFF6A00
            Material.MAGENTA_CONCRETE,    // 0xFF00FF
            Material.LIGHT_BLUE_CONCRETE, // 0x6699FF
            Material.YELLOW_CONCRETE,     // 0xFFFF00
            Material.LIME_CONCRETE,       // 0x80FF00
            Material.PINK_CONCRETE,       // 0xFF69B4
            Material.GRAY_CONCRETE,       // 0x808080
            Material.LIGHT_GRAY_CONCRETE, // 0xC0C0C0
            Material.CYAN_CONCRETE,       // 0x00FFFF
            Material.PURPLE_CONCRETE,     // 0x800080
            Material.BLUE_CONCRETE,       // 0x0000FF
            Material.BROWN_CONCRETE,      // 0x8B4513
            Material.GREEN_CONCRETE,      // 0x00FF00
            Material.RED_CONCRETE,        // 0xFF0000
            Material.BLACK_CONCRETE       // 0x000000
    };
    // RGB values for each concrete color (matching order above)
    private static final int[] CONCRETE_RGB_VALUES = {
            0xFFFFFF, // WHITE
            0xFF6A00, // ORANGE
            0xFF00FF, // MAGENTA
            0x6699FF, // LIGHT_BLUE
            0xFFFF00, // YELLOW
            0x80FF00, // LIME
            0xFF69B4, // PINK
            0x808080, // GRAY
            0xC0C0C0, // LIGHT_GRAY
            0x00FFFF, // CYAN
            0x800080, // PURPLE
            0x0000FF, // BLUE
            0x8B4513, // BROWN
            0x00FF00, // GREEN
            0xFF0000, // RED
            0x000000  // BLACK
    };
    @Getter
    private final Wormhole wormhole;
    private final int wormholeNumber;
    // Line rendering data (similar to HourglassStructure)
    private final List<DrawLine.LineData> lineDataList = new ArrayList<>();
    // Track if we're currently initializing to prevent loops
    private final boolean isInitializing = false;
    @Getter
    private Location location;
    @Getter
    private String locationString;
    @Getter
    private String armorStandText;
    // Track by UUID instead of entity object
    private TextDisplay textDisplay = null;
    private String worldName;
    @Getter
    @Setter
    private String portalMissingMessage = null;
    @Getter
    @Setter
    private String opMessage = null;
    private PersistentObjectHandler persistentObjectHandler = null;
    private long[] cachedEdges = null; // Cache edges as long array (p1 << 32 | p2)
    private Material concreteColor = null; // Cache the concrete color
    private boolean linesInitialized = false;
    private int rotationIndex = 0;
    private static final long LINES_RECREATION_COOLDOWN_MS = 500; // 500ms cooldown after clearing
    private volatile boolean isProcessingChunkEvent = false; // Prevent race conditions during chunk events
    private long lastLinesClearTime = 0; // Cooldown to prevent rapid recreation
    private int particleTickCounter = 0; // Counter for particle spawn rate

    public WormholeEntry(Wormhole wormhole, String locationString, int wormholeNumber) {
        this.wormhole = wormhole;
        this.wormholeNumber = wormholeNumber;
        this.locationString = locationString;
        if (locationString == null) {
            Logger.warn("Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " is missing a wormhole location! Fix this!");
            return;
        }
        setLocationFromConfiguration();
        if (wormholeNumber == 1) this.armorStandText = wormhole.getWormholeConfigFields().getLocation1Text();
        else this.armorStandText = wormhole.getWormholeConfigFields().getLocation2Text();

        persistentObjectHandler = new PersistentObjectHandler(this);
        wormholeEntries.add(this);
    }

    /**
     * Encode two indices as a long: (smaller << 32) | larger
     */
    private static long encodeEdge(int p1, int p2) {
        if (p1 < p2) {
            return ((long) p1 << 32) | p2;
        } else {
            return ((long) p2 << 32) | p1;
        }
    }

    private Location getDungeonLocation() {
        EMPackage emPackage = EMPackage.getEmPackages().get(locationString);
        if (emPackage == null) {
            Logger.warn("Dungeon " + locationString + " is not a valid dungeon packager name! Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " will not lead anywhere.");
            setPortalMissingMessage(WormholesConfig.getDefaultPortalMissingMessage());
            return null;
        }
        if (!emPackage.isDownloaded() || !emPackage.isInstalled()) {
            //Logger.info("Wormhole " + wormhole.getWormholeConfigFields().getFilename() + " will not lead anywhere because the dungeon " + locationString + " is not installed!");
            setPortalMissingMessage(WormholesConfig.getDungeonNotInstalledMessage().replace("$dungeonID", emPackage.getContentPackagesConfigFields().getName()));

            this.opMessage = ChatColorConverter.convert("&8[EliteMobs - OP-only message] &fDownload links are available on &9https://magmaguy.itch.io/ &f" + "(free and premium) and &9https://www.patreon.com/magmaguy &f(premium). You can check the difference " + "between the two and get support here: " + DiscordLinks.mainLink);
        }
        Location teleportLocation = emPackage.getContentPackagesConfigFields().getTeleportLocation();
        if (teleportLocation == null) return null;

        worldName = emPackage.getContentPackagesConfigFields().getWorldName();

        return teleportLocation.clone();
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
        if (persistentObjectHandler != null) persistentObjectHandler.remove();
        persistentObjectHandler = new PersistentObjectHandler(this);
        chunkLoad();
    }

    public void onDungeonUninstall() {
        clearLines();
        location = null;
    }

    @Override
    public void chunkLoad() {
    }

    @Override
    public void chunkUnload() {
        isProcessingChunkEvent = true;
        try {
            clearLines();
            if (textDisplay != null) {
                textDisplay.remove();
                textDisplay = null;
            }
        } finally {
            isProcessingChunkEvent = false;
        }
    }

    @Override
    public void worldLoad(World world) {
        // Recalculate the Location from your config (coords or dungeon)
        setLocationFromConfiguration();

        // Only initialize if we have a valid world & chunk loaded
        if (location != null
                && location.getWorld() != null
                && ChunkLocationChecker.chunkAtLocationIsLoaded(location)) {
            chunkLoad();
        }
    }

    @Override
    public void worldUnload() {
        if (location != null) location.setWorld(null);
        remove();
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
        remove();
    }

    private void remove(){
        if (textDisplay != null) textDisplay.remove();
        clearLines();
    }

    public void updateLocation(Player player) {
        locationString = ConfigurationLocation.deserialize(player.getLocation());
        location = player.getLocation().add(new Vector(0, 1 * wormhole.getWormholeConfigFields().getSizeMultiplier(), 0));
        wormhole.getWormholeConfigFields().setWormholeEntryLocation(location, wormholeNumber);
        if (persistentObjectHandler != null) persistentObjectHandler.remove();
        persistentObjectHandler = new PersistentObjectHandler(this);
    }

    /**
     * Ticks the wormhole entry visuals. Called by WormholeManager with pre-filtered nearby players.
     *
     * @param nearbyPlayers List of players within 30 blocks (already filtered by WormholeManager)
     */
    public void tick(List<Player> nearbyPlayers) {
        // Prevent race condition during chunk load/unload events
        if (isProcessingChunkEvent) {
            return;
        }

        // CRITICAL: Check chunk loading FIRST - do nothing if chunk isn't loaded
        if (location == null || location.getWorld() == null ||
                !ChunkLocationChecker.chunkAtLocationIsLoaded(location)) {
            return;
        }

        // Update text display if needed (always update if players are nearby)
        if (armorStandText != null && (textDisplay == null || !textDisplay.isValid())) initializeTextDisplay();

        // Update visual effects (lines) if enabled and at least one player has line of sight
        if (!WormholesConfig.isNoParticlesMode() &&
                wormhole.getWormholeConfigFields().getStyle() != Wormhole.WormholeStyle.NONE) {
            // Only update visuals if at least one player can see the wormhole
            if (anyPlayerHasLineOfSight(nearbyPlayers)) {
                updateVisualEffects();
            }
        }
    }

    /**
     * Checks if any player in the list has line of sight to the wormhole center.
     * Uses raytrace to detect if blocks are obstructing the view.
     *
     * @param players List of nearby players to check
     * @return true if at least one player can see the wormhole
     */
    private boolean anyPlayerHasLineOfSight(List<Player> players) {
        if (players.isEmpty() || location == null || location.getWorld() == null) {
            return false;
        }

        World world = location.getWorld();

        for (Player player : players) {
            Location playerEyes = player.getEyeLocation();
            Vector direction = location.toVector().subtract(playerEyes.toVector());
            double distance = direction.length();

            if (distance < 0.1) {
                // Player is essentially at the wormhole center
                return true;
            }

            direction.normalize();

            // Raytrace from player eyes to wormhole center
            RayTraceResult result = world.rayTraceBlocks(
                    playerEyes,
                    direction,
                    distance,
                    FluidCollisionMode.NEVER,
                    true // ignore passable blocks like grass, flowers, etc.
            );

            // If no block was hit, the player has line of sight
            if (result == null) {
                return true;
            }
        }

        return false; // All players are blocked by terrain
    }

    /**
     * Initialize text display - only called when chunk is confirmed loaded
     */
    public void initializeTextDisplay() {
        if (armorStandText == null) return;
        if (location == null || location.getWorld() == null) return;

        try {
            // Spawn new display
            textDisplay = location.getWorld().spawn(
                    location.clone().add(new Vector(0, 1.2, 0).multiply(wormhole.getWormholeConfigFields().getSizeMultiplier())),
                    TextDisplay.class,
                    (Consumer<TextDisplay>) textDisplay -> {
                        textDisplay.setBillboard(Display.Billboard.VERTICAL);
                        textDisplay.setText(ChatColorConverter.convert(armorStandText));
                        textDisplay.setPersistent(false);
                    }
            );
        } catch (Exception e) {
            // If entity creation fails, textDisplay remains null and will retry next tick
            textDisplay = null;
        }
    }

    /**
     * Update visual effects using DrawLine (similar to HourglassStructure)
     */
    private void updateVisualEffects() {
        List<List<Vector>> cachedRotations = wormhole.getCachedRotations();
        if (cachedRotations.isEmpty()) return;

        // Update rotation index - cycle through all rotations smoothly
        if (rotationIndex >= cachedRotations.size()) {
            rotationIndex = 0;
        }

        // Update lines every frame (animation runs at tick rate)
        updateLines(cachedRotations.get(rotationIndex));

        // Add minimal particles at key points (only every 5 ticks)
        particleTickCounter++;
        if (particleTickCounter >= 5) {
            spawnMinimalParticles(cachedRotations.get(rotationIndex));
            particleTickCounter = 0;
        }

        // Increment for next frame - this ensures smooth animation
        rotationIndex++;
    }

    /**
     * Clear any lingering display blocks in the area before creating new lines
     */
    private void clearNearbyDisplayBlocks() {
        if (location == null || location.getWorld() == null) return;

        World world = location.getWorld();
        double clearRadius = 5.0 * wormhole.getWormholeConfigFields().getSizeMultiplier();

        // Get all display entities near the wormhole location
        world.getNearbyEntities(location, clearRadius, clearRadius, clearRadius).stream()
                .filter(entity -> entity instanceof Display)
                .filter(entity -> !(entity.equals(textDisplay))) // Don't remove our text display
                .forEach(entity -> {
                    // Optional: Add additional checks here to ensure we're only removing
                    // display blocks (not other displays that might be intentional)
                    Display display = (Display) entity;

                    // Check if it's a block display (DrawLine creates block displays)
                    if (display instanceof org.bukkit.entity.BlockDisplay) {
                        display.remove();
                    }
                });
    }

    /**
     * Update lines for current rotation frame
     */
    private void updateLines(List<Vector> currentFrame) {
        if (currentFrame.size() < 2) return;

        World world = location.getWorld();
        if (world == null) return;

        // Check if lines need to be (re)created
        boolean needsRecreation = !linesInitialized || cachedEdges == null || areLinesDeleted();

        if (needsRecreation) {
            // Enforce cooldown to prevent rapid recreation (which causes UUID conflicts and jitter)
            if (System.currentTimeMillis() - lastLinesClearTime < LINES_RECREATION_COOLDOWN_MS) {
                return;
            }

            // Clear any existing lines first (only if there are any to avoid resetting cooldown)
            if (!lineDataList.isEmpty()) {
                clearLines();
                return; // Wait for next cycle after clearing
            }

            // **NEW: Clear any lingering display blocks in the area**
            clearNearbyDisplayBlocks();

            // Get concrete color that matches the wormhole's particle color (only once)
            concreteColor = getConcreteColorForWormhole();

            // Get edges from shape (connect points based on shape type) - cache this
            cachedEdges = getEdgesForShape(currentFrame);

            // Create lines for each edge
            try {
                for (long edge : cachedEdges) {
                    int p1 = (int) (edge >>> 32);
                    int p2 = (int) (edge & 0xFFFFFFFFL);
                    if (p1 >= currentFrame.size() || p2 >= currentFrame.size()) continue;

                    Vector v1 = currentFrame.get(p1);
                    Vector v2 = currentFrame.get(p2);

                    Location loc1 = location.clone().add(v1);
                    Location loc2 = location.clone().add(v2);

                    DrawLine.LineData line = DrawLine.drawLine(loc1, loc2, LINE_WIDTH, concreteColor, -1);
                    if (line != null) {
                        lineDataList.add(line);
                    }
                }
                linesInitialized = true;
            } catch (Exception e) {
                // If entity creation fails (e.g., UUID conflict), clear and retry next cycle
                clearLines();
                return;
            }
        } else {
            // Update existing line positions instead of recreating
            int lineIndex = 0;
            for (long edge : cachedEdges) {
                int p1 = (int) (edge >>> 32);
                int p2 = (int) (edge & 0xFFFFFFFFL);
                if (p1 >= currentFrame.size() || p2 >= currentFrame.size()) continue;
                if (lineIndex >= lineDataList.size()) break;

                Vector v1 = currentFrame.get(p1);
                Vector v2 = currentFrame.get(p2);

                Location loc1 = location.clone().add(v1);
                Location loc2 = location.clone().add(v2);

                DrawLine.LineData line = lineDataList.get(lineIndex);
                if (line != null) {
                    DrawLine.updateLine(line, loc1, loc2);
                }
                lineIndex++;
            }
        }
    }

    /**
     * Check if lines have been deleted and need to be recreated
     */
    private boolean areLinesDeleted() {
        if (lineDataList.isEmpty()) return true;

        // Add chunk check
        if (location == null || location.getWorld() == null ||
                !ChunkLocationChecker.chunkAtLocationIsLoaded(location)) {
            return true;
        }

        for (DrawLine.LineData line : lineDataList) {
            if (line == null || !line.getDisplay().isValid()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Spawn minimal particles at key points for visual enhancement
     */
    private void spawnMinimalParticles(List<Vector> currentFrame) {
        World world = location.getWorld();
        if (world == null) return;

        // Only spawn particles at a subset of points to keep it minimal
        int particleInterval = Math.max(1, currentFrame.size() / 8); // ~8 particles max

        for (int i = 0; i < currentFrame.size(); i += particleInterval) {
            Vector v = currentFrame.get(i);
            Location particleLoc = location.clone().add(v);

            // Use the wormhole's particle color
            world.spawnParticle(
                    Particle.DUST,
                    particleLoc,
                    1, .25, .25, .25, 0,
                    new Particle.DustOptions(wormhole.getParticleColor(), 0.5f)
            );
        }
    }

    /**
     * Get edges for the current shape type - matches the trace() calls in VisualEffects
     * This draws lines directly between the points that trace() would connect
     * Returns array of longs encoding edge pairs: (p1 << 32) | p2 where p1 < p2
     */
    private long[] getEdgesForShape(List<Vector> allPoints) {
        Wormhole.WormholeStyle style = wormhole.getWormholeConfigFields().getStyle();
        double sizeMultiplier = wormhole.getWormholeConfigFields().getSizeMultiplier();

        // Generate edges based on shape type, matching the trace() calls in VisualEffects
        switch (style) {
            case CUBE:
                return getCubeEdges(allPoints, sizeMultiplier);
            case CRYSTAL:
                return getCrystalEdges(allPoints, sizeMultiplier);
            case ICOSAHEDRON:
                return getIcosahedronEdges(allPoints, sizeMultiplier);
            default:
                return new long[0];
        }
    }

    /**
     * Generate edges for cube - matches trace() calls: top->front, bottom->front, front->side
     * Plus rotated versions for all 4 sides
     */
    private long[] getCubeEdges(List<Vector> allPoints, double sizeMultiplier) {
        Set<Long> edgeSet = new HashSet<>();

        // Find vertices by matching the original vectors
        // top (0, 1, 0), bottom (0, -1, 0), front (1, 0, 0), side (0, 0, 1)
        // Then rotated versions at 90, 180, 270 degrees

        // For each rotation frame, find the vertices and connect them as trace() would
        // We'll find points that match the original vertex positions (within tolerance)
        double tolerance = 0.1 * sizeMultiplier;

        // Find indices for key vertices in current frame
        Integer topIdx = findVertexIndex(allPoints, new Vector(0, 1, 0).multiply(sizeMultiplier), tolerance);
        Integer bottomIdx = findVertexIndex(allPoints, new Vector(0, -1, 0).multiply(sizeMultiplier), tolerance);
        Integer frontIdx = findVertexIndex(allPoints, new Vector(1, 0, 0).multiply(sizeMultiplier), tolerance);
        Integer sideIdx = findVertexIndex(allPoints, new Vector(0, 0, 1).multiply(sizeMultiplier), tolerance);

        // Also find rotated versions
        Integer backIdx = findVertexIndex(allPoints, new Vector(-1, 0, 0).multiply(sizeMultiplier), tolerance);
        Integer leftIdx = findVertexIndex(allPoints, new Vector(0, 0, -1).multiply(sizeMultiplier), tolerance);

        // Connect edges as trace() does: top->front, bottom->front, front->side
        if (topIdx != null && frontIdx != null) edgeSet.add(encodeEdge(topIdx, frontIdx));
        if (bottomIdx != null && frontIdx != null) edgeSet.add(encodeEdge(bottomIdx, frontIdx));
        if (frontIdx != null && sideIdx != null) edgeSet.add(encodeEdge(frontIdx, sideIdx));

        // Connect rotated sides
        if (topIdx != null && backIdx != null) edgeSet.add(encodeEdge(topIdx, backIdx));
        if (bottomIdx != null && backIdx != null) edgeSet.add(encodeEdge(bottomIdx, backIdx));
        if (backIdx != null && leftIdx != null) edgeSet.add(encodeEdge(backIdx, leftIdx));
        if (frontIdx != null && leftIdx != null) edgeSet.add(encodeEdge(frontIdx, leftIdx));
        if (topIdx != null && sideIdx != null) edgeSet.add(encodeEdge(topIdx, sideIdx));
        if (topIdx != null && leftIdx != null) edgeSet.add(encodeEdge(topIdx, leftIdx));
        if (bottomIdx != null && sideIdx != null) edgeSet.add(encodeEdge(bottomIdx, sideIdx));
        if (bottomIdx != null && leftIdx != null) edgeSet.add(encodeEdge(bottomIdx, leftIdx));

        // Connect the side points in a square around the center (missing center edges)
        if (frontIdx != null && backIdx != null) edgeSet.add(encodeEdge(frontIdx, backIdx));
        if (sideIdx != null && leftIdx != null) edgeSet.add(encodeEdge(sideIdx, leftIdx));

        return edgeSet.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Generate edges for crystal - matches trace() calls: top->front, bottom->front, front->side
     */
    private long[] getCrystalEdges(List<Vector> allPoints, double sizeMultiplier) {
        Set<Long> edgeSet = new HashSet<>();

        double tolerance = 0.1 * sizeMultiplier;

        Integer topIdx = findVertexIndex(allPoints, new Vector(0, 1, 0).multiply(sizeMultiplier), tolerance);
        Integer bottomIdx = findVertexIndex(allPoints, new Vector(0, -1, 0).multiply(sizeMultiplier), tolerance);
        Integer frontIdx = findVertexIndex(allPoints, new Vector(0.5, 0, 0).multiply(sizeMultiplier), tolerance);
        Integer sideIdx = findVertexIndex(allPoints, new Vector(0, 0, 0.5).multiply(sizeMultiplier), tolerance);

        // Connect as trace() does
        if (topIdx != null && frontIdx != null) edgeSet.add(encodeEdge(topIdx, frontIdx));
        if (bottomIdx != null && frontIdx != null) edgeSet.add(encodeEdge(bottomIdx, frontIdx));
        if (frontIdx != null && sideIdx != null) edgeSet.add(encodeEdge(frontIdx, sideIdx));

        // Find and connect rotated versions
        Integer backIdx = findVertexIndex(allPoints, new Vector(-0.5, 0, 0).multiply(sizeMultiplier), tolerance);
        Integer leftIdx = findVertexIndex(allPoints, new Vector(0, 0, -0.5).multiply(sizeMultiplier), tolerance);

        if (topIdx != null && backIdx != null) edgeSet.add(encodeEdge(topIdx, backIdx));
        if (bottomIdx != null && backIdx != null) edgeSet.add(encodeEdge(bottomIdx, backIdx));
        if (backIdx != null && leftIdx != null) edgeSet.add(encodeEdge(backIdx, leftIdx));
        if (frontIdx != null && leftIdx != null) edgeSet.add(encodeEdge(frontIdx, leftIdx));
        if (topIdx != null && sideIdx != null) edgeSet.add(encodeEdge(topIdx, sideIdx));
        if (topIdx != null && leftIdx != null) edgeSet.add(encodeEdge(topIdx, leftIdx));
        if (bottomIdx != null && sideIdx != null) edgeSet.add(encodeEdge(bottomIdx, sideIdx));
        if (bottomIdx != null && leftIdx != null) edgeSet.add(encodeEdge(bottomIdx, leftIdx));

        // Connect the side points in a square around the center (missing center edges)
        if (frontIdx != null && backIdx != null) edgeSet.add(encodeEdge(frontIdx, backIdx));
        if (sideIdx != null && leftIdx != null) edgeSet.add(encodeEdge(sideIdx, leftIdx));

        return edgeSet.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Generate edges for icosahedron - matches trace() calls in getPentagonVectors and generateIcosahedron
     */
    private long[] getIcosahedronEdges(List<Vector> allPoints, double sizeMultiplier) {
        Set<Long> edgeSet = new HashSet<>();

        double tolerance = 0.2 * sizeMultiplier;

        // Find top and bottom vertices
        Integer topIdx = findVertexIndex(allPoints, new Vector(0, 1, 0).multiply(sizeMultiplier), tolerance);
        Integer bottomIdx = findVertexIndex(allPoints, new Vector(0, -1, 0).multiply(sizeMultiplier), tolerance);

        // Find pentagon vertices - they're at specific angles around Y axis
        // Top pentagon: rotated around (1, 0.5, 0) at 0, 72, 144, 216, 288 degrees
        // Bottom pentagon: rotated around (1, -0.5, 0) at 36, 108, 180, 252, 324 degrees

        List<Integer> topPentagonIndices = new ArrayList<>();
        List<Integer> bottomPentagonIndices = new ArrayList<>();

        Vector topOffset = new Vector(1, 0.5, 0).multiply(sizeMultiplier);
        Vector bottomOffset = new Vector(1, -0.5, 0).multiply(sizeMultiplier);
        bottomOffset.rotateAroundY(2 * Math.PI / 5D / 2D);

        for (int i = 0; i < 5; i++) {
            Vector topVec = topOffset.clone().rotateAroundY(2 * Math.PI / 5D * i);
            Vector bottomVec = bottomOffset.clone().rotateAroundY(2 * Math.PI / 5D * i);

            Integer topPentIdx = findVertexIndex(allPoints, topVec, tolerance);
            Integer bottomPentIdx = findVertexIndex(allPoints, bottomVec, tolerance);

            if (topPentIdx != null) topPentagonIndices.add(topPentIdx);
            if (bottomPentIdx != null) bottomPentagonIndices.add(bottomPentIdx);
        }

        // Connect top to top pentagon vertices
        if (topIdx != null) {
            for (Integer pentIdx : topPentagonIndices) {
                edgeSet.add(encodeEdge(topIdx, pentIdx));
            }
        }

        // Connect bottom to bottom pentagon vertices
        if (bottomIdx != null) {
            for (Integer pentIdx : bottomPentagonIndices) {
                edgeSet.add(encodeEdge(bottomIdx, pentIdx));
            }
        }

        // Connect top pentagon vertices to each other (pentagon edges)
        for (int i = 0; i < topPentagonIndices.size(); i++) {
            int next = (i + 1) % topPentagonIndices.size();
            edgeSet.add(encodeEdge(topPentagonIndices.get(i), topPentagonIndices.get(next)));
        }

        // Connect bottom pentagon vertices to each other
        for (int i = 0; i < bottomPentagonIndices.size(); i++) {
            int next = (i + 1) % bottomPentagonIndices.size();
            edgeSet.add(encodeEdge(bottomPentagonIndices.get(i), bottomPentagonIndices.get(next)));
        }

        // Connect top pentagon to bottom pentagon (vertical edges)
        for (int i = 0; i < Math.min(topPentagonIndices.size(), bottomPentagonIndices.size()); i++) {
            edgeSet.add(encodeEdge(topPentagonIndices.get(i), bottomPentagonIndices.get(i)));
            // Also connect to next bottom vertex (as trace() does)
            if (i + 1 < topPentagonIndices.size() - 1) {
                edgeSet.add(encodeEdge(topPentagonIndices.get(i + 1), bottomPentagonIndices.get(i)));
            }
        }
        // Connect first top to last bottom
        if (!topPentagonIndices.isEmpty() && !bottomPentagonIndices.isEmpty()) {
            edgeSet.add(encodeEdge(topPentagonIndices.get(0), bottomPentagonIndices.get(4 % bottomPentagonIndices.size())));
        }

        return edgeSet.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Find the index of a vertex in the points list that matches the target vector (within tolerance)
     */
    private Integer findVertexIndex(List<Vector> points, Vector target, double tolerance) {
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).distance(target) <= tolerance) {
                return i;
            }
        }
        return null;
    }

    private void clearLines() {
        for (DrawLine.LineData line : lineDataList) {
            if (line != null) line.remove();
        }
        lineDataList.clear();
        linesInitialized = false;
        cachedEdges = null;
        lastLinesClearTime = System.currentTimeMillis();
    }

    /**
     * Maps the wormhole's particle color (RGB) to the closest matching concrete color
     */
    private Material getConcreteColorForWormhole() {
        org.bukkit.Color wormholeColor = wormhole.getParticleColor();
        int targetRGB = (wormholeColor.getRed() << 16) | (wormholeColor.getGreen() << 8) | wormholeColor.getBlue();

        // Find the closest matching concrete color
        int closestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < CONCRETE_RGB_VALUES.length; i++) {
            int concreteRGB = CONCRETE_RGB_VALUES[i];
            double distance = colorDistance(targetRGB, concreteRGB);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

        return CONCRETE_COLORS_BY_RGB[closestIndex];
    }

    /**
     * Calculate color distance using Euclidean distance in RGB space
     */
    private double colorDistance(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}