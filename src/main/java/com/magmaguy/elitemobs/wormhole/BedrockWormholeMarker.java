package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.customentity.BedrockCustomEntityBridgeRegistry;
import com.magmaguy.easyminecraftgoals.customentity.CustomEntityPropertySchema;
import com.magmaguy.easyminecraftgoals.customentity.FakeCustomEntity;
import com.magmaguy.elitemobs.thirdparty.geyser.GeyserDetector;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class BedrockWormholeMarker {
    private static final String IDENTIFIER = "elitemobs:wormhole_marker";

    private FakeCustomEntity marker;
    private Location lastLocation;
    private float lastScale = Float.NaN;
    private int lastColor = Integer.MIN_VALUE;
    private boolean failedInitialization;

    void tick(Wormhole wormhole, Location location, Collection<Player> nearbyPlayers) {
        if (failedInitialization || !available() || wormhole == null || location == null || location.getWorld() == null) {
            remove();
            return;
        }

        if (marker == null) {
            try {
                marker = NMSManager.getAdapter().fakeCustomEntityBuilder()
                        .identifier(IDENTIFIER)
                        .carrierEntityType(EntityType.PIG)
                        .dimensions(0.9f, 0.9f)
                        .scale(scale(wormhole))
                        .color(color(wormhole))
                        .tracked(false)
                        .propertySchema(CustomEntityPropertySchema.empty())
                        .build(location);
            } catch (RuntimeException e) {
                failedInitialization = true;
                Logger.warn("Failed to initialize Bedrock wormhole marker; disabling marker for this wormhole: "
                        + e.getMessage());
                remove();
                return;
            }
        }

        syncLocation(location);
        syncScale(wormhole);
        syncColor(wormhole);
        syncViewers(nearbyPlayers);
    }

    void remove() {
        if (marker == null) {
            return;
        }
        marker.remove();
        marker = null;
        lastLocation = null;
        lastScale = Float.NaN;
        lastColor = Integer.MIN_VALUE;
    }

    private static boolean available() {
        return NMSManager.getAdapter() != null && BedrockCustomEntityBridgeRegistry.isAvailable();
    }

    private void syncLocation(Location location) {
        if (lastLocation == null || lastLocation.getWorld() != location.getWorld()
                || lastLocation.distanceSquared(location) > 0.0001) {
            marker.teleport(location);
            lastLocation = location.clone();
        }
    }

    private void syncScale(Wormhole wormhole) {
        float scale = scale(wormhole);
        if (Float.compare(scale, lastScale) == 0) {
            return;
        }
        marker.setScale(scale);
        lastScale = scale;
    }

    private void syncColor(Wormhole wormhole) {
        int color = color(wormhole);
        if (color == lastColor) {
            return;
        }
        marker.setColor(color);
        lastColor = color;
    }

    private void syncViewers(Collection<Player> nearbyPlayers) {
        Set<UUID> shouldSee = new HashSet<>();
        for (Player player : nearbyPlayers) {
            if (player == null || !player.isOnline() || !GeyserDetector.bedrockPlayer(player)) {
                continue;
            }
            shouldSee.add(player.getUniqueId());
            if (!marker.getViewers().contains(player.getUniqueId())) {
                marker.displayTo(player);
            }
        }

        for (UUID viewer : marker.getViewers()) {
            if (!shouldSee.contains(viewer)) {
                marker.hideFrom(viewer);
            }
        }
    }

    private static float scale(Wormhole wormhole) {
        return (float) Math.max(0.5, wormhole.getWormholeConfigFields().getSizeMultiplier());
    }

    private static int color(Wormhole wormhole) {
        Color color = wormhole.getParticleColor();
        return color == null ? 0xFFFFFF : color.asRGB();
    }
}
