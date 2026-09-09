package com.magmaguy.elitemobs.presentation.actionbar;

import net.md_5.bungee.api.chat.TextComponent;

import java.awt.Color;

/** Session-only bitmap calibration. Assets live in design/combat-hud-probe/mods. */
public final class CombatHudProbe {
    public static final int MAX_X_OFFSET = 64;
    public static final int MAX_Y_OFFSET = 16;
    private static final String FONT = "elitemobs:combat_hud_probe";
    private final String gray;
    private final String red;

    public CombatHudProbe(int x, int y) {
        if (Math.abs((long) x) > MAX_X_OFFSET || Math.abs((long) y) > MAX_Y_OFFSET)
            throw new IllegalArgumentException("HUD offsets must be x=-64..64 and y=-16..16 GUI pixels.");
        int glyph = 0xE000 + (y + MAX_Y_OFFSET) * 2;
        // Compensate the bitmap provider's extra pixel of advance, preserving a centered 190px line.
        gray = spacing(x) + (char) glyph + spacing(-x - 1);
        red = spacing(x) + (char) (glyph + 1) + spacing(-x - 1);
    }

    public String text(boolean active) {
        return active ? red : gray;
    }

    public TextComponent component(String text) {
        TextComponent component = new TextComponent(text);
        component.setFont(FONT);
        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        component.setShadowColor(new Color(0, true));
        return component;
    }

    private static String spacing(int pixels) {
        return String.valueOf(pixels < 0 ? '\uE101' : '\uE100').repeat(Math.abs(pixels));
    }
}
