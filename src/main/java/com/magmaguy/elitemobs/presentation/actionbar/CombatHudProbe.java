package com.magmaguy.elitemobs.presentation.actionbar;

import net.md_5.bungee.api.chat.TextComponent;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.awt.Color;

/** Session-only HUD concept. Assets live in design/combat-hud-probe/mods. */
public final class CombatHudProbe {
    public static final int MAX_X_OFFSET = 64;
    public static final int MAX_Y_OFFSET = 16;
    private final String font;
    private final int x;

    public CombatHudProbe(int x, int y) {
        if (Math.abs((long) x) > MAX_X_OFFSET || Math.abs((long) y) > MAX_Y_OFFSET)
            throw new IllegalArgumentException("HUD offsets must be x=-64..64 and y=-16..16 GUI pixels.");
        this.x = x;
        font = "elitemobs:combat_hud_concept_" + (y + MAX_Y_OFFSET);
    }

    public String text(Player player, boolean active) {
        StringBuilder line = new StringBuilder(spacing(x));
        line.append(active ? '\uE001' : '\uE000').append(spacing(-191));
        double health = player.getHealth();
        var maximumAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        double maximum = maximumAttribute == null ? health : maximumAttribute.getValue();
        String healthText = number(health) + "/" + number(maximum);
        overlay(line, 25, healthText, healthText.length() * 6);
        bar(line, 25, '\uE110', health, maximum, 63, false);
        var resource = ExperimentalCombatModule.resourceSnapshot(player.getUniqueId()).orElse(null);
        if (resource != null) overlay(line, 171, String.valueOf(resourceIcon(resource.type())), 11);
        String resourceText = resource == null ? "0/0" : number(resource.amount()) + "/" + number(resource.maximum());
        rightAlignedText(line, 165, resourceText, resourceText.length() * 6);
        bar(line, 102, '\uE111', resource == null ? 0 : resource.amount(),
                resource == null ? 0 : resource.maximum(), 63, true);
        bar(line, 5, '\uE112', player.getExp(), 1, 180, false);
        classDiamond(line, player);
        // All overlays return to the panel origin. Keep the total advance at 190 GUI pixels.
        return line.append(spacing(190 - x)).toString();
    }

    public TextComponent component(String text) {
        TextComponent component = new TextComponent(text);
        component.setFont(font);
        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        component.setShadowColor(new Color(0, true));
        return component;
    }

    private static String spacing(int pixels) {
        return String.valueOf(pixels < 0 ? '\uE101' : '\uE100').repeat(Math.abs(pixels));
    }

    private static void overlay(StringBuilder line, int offset, String text, int advance) {
        line.append(spacing(offset)).append(text).append(spacing(-offset - advance));
    }

    private static void rightAlignedText(StringBuilder line, int rightEdge, String text, int advance) {
        // Each fixed-width glyph has one trailing spacing pixel outside its visible bounds.
        overlay(line, rightEdge - (advance - 1), text, advance);
    }

    private static void bar(StringBuilder line, int offset, char glyph, double amount, double maximum, int width,
                            boolean rightAligned) {
        int pixels = maximum <= 0 ? 0 : (int) Math.round(width * Math.max(0, Math.min(1, amount / maximum)));
        overlay(line, offset + (rightAligned ? width - pixels : 0),
                (String.valueOf(glyph) + '\uE101').repeat(pixels), pixels);
    }

    private static void classDiamond(StringBuilder line, Player player) {
        var progress = ExperimentalCombatModule.classProgressSnapshot(player.getUniqueId()).orElse(null);
        if (progress == null || !progress.unlocked()) return;
        int level = progress.effectiveLevel();
        int bandStart = level - progress.localLevel() + 1;
        long currentThreshold = SkillXPCalculator.totalXPForLevel(level)
                - SkillXPCalculator.totalXPForLevel(bandStart);
        double fraction = progress.xp() >= progress.xpAtCap() ? 1
                : (double) (progress.xp() - currentThreshold) / SkillXPCalculator.xpToNextLevel(level);
        int rows = (int) Math.round(28 * Math.max(0, Math.min(1, fraction)));
        overlay(line, 79, String.valueOf((char) (0xE400 + rows)), 33);
        String digits = Integer.toString(level);
        StringBuilder glyphs = new StringBuilder();
        for (char digit : digits.toCharArray()) glyphs.append((char) (0xE300 + digit - '0'));
        int advance = digits.length() * 6;
        overlay(line, 95 - advance / 2, glyphs.toString(), advance);
    }

    private static String number(double value) {
        long rounded = Math.round(Math.max(0, value));
        if (rounded >= 1_000_000) return Math.round(rounded / 1_000_000D) + "M";
        if (rounded >= 10_000) return Math.round(rounded / 1_000D) + "K";
        return Long.toString(rounded);
    }

    private static char resourceIcon(ClassResourceType type) {
        return switch (type) {
            case RESOLVE -> '\uE500';
            case FURY -> '\uE501';
            case FOCUS -> '\uE502';
            case GRACE -> '\uE503';
            case MANA -> '\uE504';
        };
    }
}
