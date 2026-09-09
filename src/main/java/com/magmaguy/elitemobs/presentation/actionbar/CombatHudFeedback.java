package com.magmaguy.elitemobs.presentation.actionbar;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/** Uses the same vanilla glyph providers and advances as the feedback resource-pack font. */
final class CombatHudFeedback {
    private static final short[] METRICS = loadMetrics("/combat-hud-feedback-metrics.bin");
    private static final short[] CLASS_NAME_METRICS = loadMetrics("/combat-hud-class-name-metrics.bin");

    private CombatHudFeedback() { }

    static BaseComponent[] components(String message, boolean legacy) {
        String singleLine = message.replace('\n', ' ').replace('\r', ' ');
        BaseComponent[] components = legacy ? TextComponent.fromLegacyText(singleLine)
                : new BaseComponent[]{new TextComponent(singleLine)};
        for (BaseComponent component : components) {
            component.setFont("elitemobs:combat_hud_feedback");
            component.setShadowColor(new Color(0, 0, 0, 190));
        }
        return components;
    }

    static int widthInHalfPixels(BaseComponent[] components) {
        return widthInHalfPixels(components, METRICS);
    }

    static int classNameWidthInHalfPixels(BaseComponent[] components) {
        return widthInHalfPixels(components, CLASS_NAME_METRICS);
    }

    /** Use ordinary characters, not class-specific glyphs. Bound unusually long translations. */
    static BaseComponent[] className(String name) {
        String plain = org.bukkit.ChatColor.stripColor(org.bukkit.ChatColor.translateAlternateColorCodes('&', name))
                .replace('\n', ' ').replace('\r', ' ');
        int[] points = plain.codePoints().toArray();
        int width = 0;
        for (int point : points) width += CLASS_NAME_METRICS[point] >> 2;
        if (width > 268) {
            int suffixWidth = 3 * (CLASS_NAME_METRICS['.'] >> 2);
            StringBuilder fitted = new StringBuilder();
            int used = 0;
            for (int point : points) {
                int advance = CLASS_NAME_METRICS[point] >> 2;
                if (used + advance + suffixWidth > 268) break;
                fitted.appendCodePoint(point);
                used += advance;
            }
            plain = fitted.append("...").toString();
        }
        TextComponent label = new TextComponent(plain);
        label.setFont("elitemobs:combat_hud_class_name");
        label.setColor(net.md_5.bungee.api.ChatColor.of("#fff0be"));
        label.setShadowColor(new Color(0, true));
        return new BaseComponent[]{label};
    }

    private static int widthInHalfPixels(BaseComponent[] components, short[] metrics) {
        int width = 0;
        for (BaseComponent component : components) {
            int[] points = component.toPlainText().codePoints().toArray();
            for (int point : points) {
                int metric = metrics[point];
                width += (metric >> 2) + (component.isBold() ? metric & 3 : 0);
            }
        }
        return width;
    }

    private static short[] loadMetrics(String path) {
        short[] metrics = new short[Character.MAX_CODE_POINT + 1];
        Arrays.fill(metrics, (short) ((12 << 2) | 2)); // Vanilla missing-glyph advance.
        try (var resource = CombatHudFeedback.class.getResourceAsStream(path)) {
            if (resource == null) throw new IllegalStateException("Missing HUD feedback font metrics");
            try (DataInputStream input = new DataInputStream(resource)) {
                if (input.readInt() != 1) throw new IOException("Unsupported HUD font metrics version");
                int count = input.readInt();
                if (count < 0 || count > metrics.length) throw new IOException("Invalid HUD glyph count");
                for (int i = 0; i < count; i++) {
                    int point = input.readInt();
                    if (!Character.isValidCodePoint(point)) throw new IOException("Invalid HUD glyph");
                    metrics[point] = (short) ((input.readUnsignedByte() << 2) | input.readUnsignedByte());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load HUD feedback font metrics", exception);
        }
        return metrics;
    }
}
