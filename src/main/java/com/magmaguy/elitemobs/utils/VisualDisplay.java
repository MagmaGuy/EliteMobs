package com.magmaguy.elitemobs.utils;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Consumer;

import java.util.Collection;


public class VisualDisplay {

    public static ArmorStand generateTemporaryArmorStand(Location location, String customName) {
        ArmorStand visualArmorStand = location.getWorld().spawn(location, ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand armorStand) {
                armorStand.setVisible(false);
                armorStand.setMarker(true);
                armorStand.setCustomName(ChatColorConverter.convert(customName));
                armorStand.setCustomNameVisible(true);
                armorStand.setGravity(false);
                armorStand.setRemoveWhenFarAway(true);
                armorStand.setPersistent(false);
            }
        });
        EntityTracker.registerVisualEffects(visualArmorStand);
        return visualArmorStand;
    }

    public static TextDisplay generateTemporaryTextDisplay(Location location, String customName) {
        TextDisplay visualArmorStand = location.getWorld().spawn(location, TextDisplay.class, new Consumer<TextDisplay>() {
            @Override
            public void accept(TextDisplay textDisplay) {
                textDisplay.setText(ChatColorConverter.convert(customName));
                textDisplay.setPersistent(false);
                textDisplay.setInterpolationDelay(0);
                textDisplay.setInterpolationDuration(0);
                textDisplay.setBillboard(Display.Billboard.VERTICAL);
                textDisplay.setShadowed(false);
            }
        });
        EntityTracker.registerVisualEffects(visualArmorStand);
        return visualArmorStand;
    }

    /**
     * Creates a packet-based FakeText display visible to all nearby players.
     * Uses EasyMinecraftGoals' FakeText system for better performance.
     *
     * @param location   The location to spawn the text
     * @param text       The text to display (supports color codes)
     * @param viewRange  The range in blocks for visibility (players within this range will see it)
     * @return The FakeText instance, or null if NMSManager is not available
     */
    public static FakeText generateFakeText(Location location, String text, double viewRange) {
        if (NMSManager.getAdapter() == null) return null;
        if (location == null || location.getWorld() == null) return null;

        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(false)
                .seeThrough(false)
                .build(location);

        // Show to all players within range
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= viewRange * viewRange) {
                fakeText.displayTo(player);
            }
        }

        return fakeText;
    }

    /**
     * Creates a packet-based FakeText display visible to specific players.
     *
     * @param location The location to spawn the text
     * @param text     The text to display (supports color codes)
     * @param viewers  The players who should see the text
     * @return The FakeText instance, or null if NMSManager is not available
     */
    public static FakeText generateFakeText(Location location, String text, Collection<Player> viewers) {
        if (NMSManager.getAdapter() == null) return null;
        if (location == null || location.getWorld() == null) return null;

        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(false)
                .seeThrough(false)
                .build(location);

        for (Player player : viewers) {
            fakeText.displayTo(player);
        }

        return fakeText;
    }

    /**
     * Creates a packet-based FakeText display visible to a single player.
     *
     * @param location The location to spawn the text
     * @param text     The text to display (supports color codes)
     * @param viewer   The player who should see the text
     * @return The FakeText instance, or null if NMSManager is not available
     */
    public static FakeText generateFakeText(Location location, String text, Player viewer) {
        if (NMSManager.getAdapter() == null) return null;
        if (location == null || location.getWorld() == null) return null;

        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(false)
                .seeThrough(false)
                .build(location);

        fakeText.displayTo(viewer);

        return fakeText;
    }

    /**
     * Creates a packet-based FakeText display visible to all online players.
     *
     * @param location The location to spawn the text
     * @param text     The text to display (supports color codes)
     * @return The FakeText instance, or null if NMSManager is not available
     */
    public static FakeText generateFakeTextGlobal(Location location, String text) {
        if (NMSManager.getAdapter() == null) return null;
        if (location == null || location.getWorld() == null) return null;

        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(false)
                .seeThrough(false)
                .build(location);

        for (Player player : Bukkit.getOnlinePlayers()) {
            fakeText.displayTo(player);
        }

        return fakeText;
    }

    /**
     * Creates a styled packet-based FakeText display with custom background and styling.
     * Useful for health bars and popup displays.
     *
     * @param location        The location to spawn the text
     * @param text            The text to display (supports color codes)
     * @param backgroundColor The background color (ARGB format)
     * @param shadowed        Whether to enable shadow
     * @param scale           The scale multiplier (1.0 = normal)
     * @param viewRange       The range in blocks for visibility
     * @return The FakeText instance, or null if NMSManager is not available
     */
    public static FakeText generateStyledFakeText(Location location, String text,
                                                   Color backgroundColor, boolean shadowed,
                                                   float scale, double viewRange) {
        if (NMSManager.getAdapter() == null) return null;
        if (location == null || location.getWorld() == null) return null;

        FakeText.Builder builder = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(shadowed)
                .seeThrough(false);

        if (backgroundColor != null) {
            builder.backgroundColor(backgroundColor);
        }

        if (scale != 1.0f) {
            builder.scale(scale);
        }

        FakeText fakeText = builder.build(location);

        // Show to all players within range
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= viewRange * viewRange) {
                fakeText.displayTo(player);
            }
        }

        return fakeText;
    }

    /**
     * Creates a styled packet-based FakeText display for popup animations.
     * Similar to generateStyledFakeText but returns the FakeText without showing to anyone,
     * allowing the caller to handle visibility.
     *
     * @param location        The location to spawn the text
     * @param text            The text to display (supports color codes)
     * @param backgroundColor The background color (ARGB format)
     * @param shadowed        Whether to enable shadow
     * @param scale           The scale multiplier (1.0 = normal)
     * @return The FakeText instance, or null if NMSManager is not available
     */
    public static FakeText createStyledFakeText(Location location, String text,
                                                 Color backgroundColor, boolean shadowed,
                                                 float scale) {
        if (NMSManager.getAdapter() == null) return null;
        if (location == null || location.getWorld() == null) return null;

        FakeText.Builder builder = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(text))
                .billboard(Display.Billboard.CENTER)
                .shadow(shadowed)
                .seeThrough(false);

        if (backgroundColor != null) {
            builder.backgroundColor(backgroundColor);
        }

        if (scale != 1.0f) {
            builder.scale(scale);
        }

        return builder.build(location);
    }

}
