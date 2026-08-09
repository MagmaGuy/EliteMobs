package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/** Owns player-scoped XP popup creation, gradient animation, and cleanup. */
final class XpPopupManager {

    private static final int DURATION_TICKS = 50;
    private static final java.awt.Color[] GRADIENT_COLORS = {
            new java.awt.Color(0xFF, 0x6B, 0x00),
            new java.awt.Color(0xFF, 0xD7, 0x00),
            new java.awt.Color(0xFF, 0xFF, 0x00),
            new java.awt.Color(0xFF, 0xD7, 0x00),
            new java.awt.Color(0xFF, 0x6B, 0x00)
    };
    private static final List<XpPopup> activePopups = new ArrayList<>();

    private XpPopupManager() {
    }

    static void create(Location location, Player player, long xpAmount) {
        if (location == null || location.getWorld() == null || player == null || !player.isOnline()) return;
        Vector offset = new Vector(
                ThreadLocalRandom.current().nextDouble(-0.5, 0.5),
                ThreadLocalRandom.current().nextDouble(0.5, 1.0),
                ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        Location popupLocation = location.clone().add(offset);
        String text = MobCombatSettingsConfig.getXpPopupFormat()
                .replace("$amount", DisplayTextFormatter.number(xpAmount));
        String initialText = ChatColorConverter.convert(
                "<gradient:#FF6B00:#FFD700:#FFFF00>" + text + "</gradient>");
        FakeText display = VisualDisplay.createStyledFakeText(
                popupLocation, initialText, Color.fromARGB(120, 80, 60, 0), true, 0.85f);
        if (display == null) return;

        display.displayTo(player);
        activePopups.add(new XpPopup(display, popupLocation, text, 0.85f));
    }

    static void update() {
        Iterator<XpPopup> iterator = activePopups.iterator();
        while (iterator.hasNext()) {
            XpPopup popup = iterator.next();
            boolean keep = false;
            try {
                keep = popup.update();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Discarding a failed XP popup", exception);
            }
            if (keep) continue;
            try {
                popup.cleanup();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean an XP popup", exception);
            } finally {
                iterator.remove();
            }
        }
    }

    static void shutdown() {
        activePopups.forEach(popup -> {
            try {
                popup.cleanup();
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean an XP popup during shutdown", exception);
            }
        });
        activePopups.clear();
    }

    private static String shiftedGradient(String text, float shift) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        int colorCount = GRADIENT_COLORS.length;
        for (int index = 0; index < text.length(); index++) {
            float position = ((float) index / text.length() + shift) % 1.0f;
            float scaledPosition = position * (colorCount - 1);
            int firstIndex = (int) scaledPosition;
            int secondIndex = (firstIndex + 1) % colorCount;
            java.awt.Color color = interpolate(
                    GRADIENT_COLORS[firstIndex], GRADIENT_COLORS[secondIndex], scaledPosition - firstIndex);
            String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            try {
                result.append(net.md_5.bungee.api.ChatColor.of(hex)).append(text.charAt(index));
            } catch (IllegalArgumentException ignored) {
                result.append(ChatColor.GOLD).append(text.charAt(index));
            }
        }
        return result.toString();
    }

    private static java.awt.Color interpolate(java.awt.Color first, java.awt.Color second, float ratio) {
        int red = (int) (first.getRed() + ratio * (second.getRed() - first.getRed()));
        int green = (int) (first.getGreen() + ratio * (second.getGreen() - first.getGreen()));
        int blue = (int) (first.getBlue() + ratio * (second.getBlue() - first.getBlue()));
        return new java.awt.Color(red, green, blue);
    }

    private static final class XpPopup {
        private final FakeText display;
        private final Location startLocation;
        private final String text;
        private final float baseScale;
        private int ticksAlive;

        private XpPopup(FakeText display, Location startLocation, String text, float baseScale) {
            this.display = display;
            this.startLocation = startLocation.clone();
            this.text = text;
            this.baseScale = baseScale;
        }

        private boolean update() {
            ticksAlive++;
            if (ticksAlive >= DURATION_TICKS || display == null) return false;

            float progress = (float) ticksAlive / DURATION_TICKS;
            display.teleport(startLocation.clone().add(0, progress * 1.2, 0));
            display.setText(shiftedGradient(text, (ticksAlive % 20) / 20f));
            float pulse = (float) Math.sin(ticksAlive * 0.15) * 0.1f;
            float scaleMultiplier;
            if (progress < 0.1f)
                scaleMultiplier = 1.0f + (progress / 0.1f) * 0.2f;
            else if (progress > 0.8f)
                scaleMultiplier = 1.2f - ((progress - 0.8f) / 0.2f) * 0.4f;
            else
                scaleMultiplier = 1.2f + pulse;
            display.setScale(baseScale * scaleMultiplier);
            if (progress > 0.75f)
                display.setTextOpacity((byte) ((1.0f - ((progress - 0.75f) / 0.25f)) * 255));
            return true;
        }

        private void cleanup() {
            if (display != null) display.remove();
        }
    }
}
