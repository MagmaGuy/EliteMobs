package com.magmaguy.elitemobs.presentation.actionbar;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/** Uses the same vanilla glyph providers and advances as the feedback resource-pack font. */
final class CombatHudFeedback {
    private static final short[] METRICS = loadMetrics();

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
        int width = 0;
        for (BaseComponent component : components) {
            int[] points = component.toPlainText().codePoints().toArray();
            for (int point : points) {
                int metric = METRICS[point];
                width += (metric >> 2) + (component.isBold() ? metric & 3 : 0);
            }
        }
        return width;
    }

    private static short[] loadMetrics() {
        short[] metrics = new short[Character.MAX_CODE_POINT + 1];
        Arrays.fill(metrics, (short) ((12 << 2) | 2)); // Vanilla missing-glyph advance.
        try (var resource = CombatHudFeedback.class.getResourceAsStream("/combat-hud-feedback-metrics.bin")) {
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
