package com.magmaguy.elitemobs.presentation.actionbar;

import net.md_5.bungee.api.chat.TextComponent;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.awt.Color;

/** Resource-pack HUD rendered automatically by the managed-world combat lifecycle. */
public final class CombatHud {
    private static final String FONT = "elitemobs:combat_hud_concept_16";

    public record Frame(String text, String className) { }

    public Frame frame(Player player, boolean active) {
        int animationFrame = (int) Math.floorMod(MonotonicTickClock.currentTick() / 4L, 4L);
        StringBuilder line = new StringBuilder();
        line.append(active ? '\uE001' : '\uE000').append(spacing(-191));
        double health = player.getHealth();
        var maximumAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        double maximum = maximumAttribute == null ? health : maximumAttribute.getValue();
        String healthText = number(health) + "/" + number(maximum);
        overlay(line, 25, healthText, healthText.length() * 6);
        bar(line, 25, '\uE800', health, maximum, 63, false, animationFrame);
        var resource = ExperimentalCombatModule.resourceSnapshot(player.getUniqueId()).orElse(null);
        if (resource != null) overlay(line, 171, String.valueOf(resourceIcon(resource.type())), 11);
        String resourceText = resource == null ? "0/0" : number(resource.amount()) + "/" + number(resource.maximum());
        rightAlignedText(line, 165, resourceText, resourceText.length() * 6);
        bar(line, 102, resource == null ? '\uE940' : resourceBar(resource.type()), resource == null ? 0 : resource.amount(),
                resource == null ? 0 : resource.maximum(), 63, true, animationFrame);
        bar(line, 5, '\uE980', player.getExp(), 1, 180, false, animationFrame);
        classDiamond(line, player, animationFrame, active);
        String className = ExperimentalCombatModule.activeClassLineageSnapshot(player.getUniqueId())
                .map(lineage -> lineage.activeForm().displayName()).orElse("");
        if (active) abilityIcons(line, player);
        // All overlays return to the panel origin. Keep the total advance at 190 GUI pixels.
        return new Frame(line.append(spacing(190)).toString(), className);
    }

    public TextComponent component(String text) {
        TextComponent component = new TextComponent(text);
        component.setFont(FONT);
        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        component.setShadowColor(new Color(0, true));
        return component;
    }

    public TextComponent component(Frame frame, String feedback, boolean legacy) {
        TextComponent component = component(frame.text());
        if (!frame.className().isBlank()) {
            var label = CombatHudFeedback.className(frame.className());
            int width = CombatHudFeedback.classNameWidthInHalfPixels(label);
            int badgeWidth = Math.max(74, (width + 1) / 2 + 8);
            int left = 78 - badgeWidth;
            // Keep the right edge clear of the diamond; longer translations expand left.
            String border = "\uE600\uE101" + "\uE601\uE101".repeat(badgeWidth - 6) + "\uE602\uE101";
            component.addExtra(halfPixelSpacing(left * 2 - 380));
            component.addExtra(border);
            component.addExtra(halfPixelSpacing(380 - (left + badgeWidth) * 2));
            appendText(component, label, left * 2 + (badgeWidth * 2 - width) / 2, width);
        }
        if (feedback == null || feedback.isBlank()) return component;
        var message = CombatHudFeedback.components(feedback, legacy);
        int width = CombatHudFeedback.widthInHalfPixels(message);
        // The HUD advances 190px. The text overlay must have zero net advance or Minecraft
        // recenters the entire action bar when feedback changes length.
        int start = (380 - width) / 2;
        appendText(component, message, start, width);
        return component;
    }

    private static void appendText(TextComponent component, net.md_5.bungee.api.chat.BaseComponent[] message,
                                   int start, int width) {
        component.addExtra(halfPixelSpacing(start - 380));
        for (var part : message) component.addExtra(part);
        component.addExtra(halfPixelSpacing(380 - start - width));
    }

    private static String halfPixelSpacing(int halfPixels) {
        return spacing(halfPixels / 2) + (halfPixels % 2 == 0 ? ""
                : halfPixels < 0 ? "\uE102" : "\uE103");
    }

    private static void abilityIcons(StringBuilder line, Player player) {
        ExperimentalCombatModule.activeClassLineageSnapshot(player.getUniqueId()).ifPresent(lineage -> {
            var slots = java.util.List.of(AbilitySlot.SIGNATURE, AbilitySlot.UTILITY, AbilitySlot.MOBILITY);
            for (int index = 0; index < slots.size(); index++) {
                var state = ExperimentalCombatModule.abilityResourceSnapshot(player, slots.get(index)).orElse(null);
                if (state == null) continue;
                int left = 4 + index * 61;
                // Replace only this card's surface, before drawing its icon and live cost.
                if (!state.affordable()) overlay(line, left, String.valueOf((char) (0xE680 + index)), 61);
                String glyph = CombatHudAbilityIcons.glyph(state.abilityId());
                if (!glyph.isEmpty()) overlay(line, left + 3, glyph, 14);
                String digits = Long.toString((long) Math.ceil(state.cost()));
                int start = left + 38 - (digits.length() * 4 + 7) / 2;
                overlay(line, start, String.valueOf((char) (0xE540 + resourceIcon(lineage.resourceType()) - 0xE500)), 6);
                StringBuilder cost = new StringBuilder();
                for (char digit : digits.toCharArray())
                    cost.append((char) ((state.affordable() ? 0xE700 : 0xE710) + digit - '0'));
                overlay(line, start + 8, cost.toString(), digits.length() * 4);
            }
        });
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
                            boolean rightAligned, int animationFrame) {
        int pixels = maximum <= 0 ? 0 : (int) Math.round(width * Math.max(0, Math.min(1, amount / maximum)));
        int start = rightAligned ? width - pixels : 0;
        StringBuilder fill = new StringBuilder(pixels * 2);
        // Anchor each hand-drawn liquid frame to the trough. Mirroring the
        // right-hand resource reverses its motion as well as its fill direction.
        for (int column = start; column < start + pixels; column++) {
            int textureColumn = (rightAligned ? width - 1 - column : column) % 16;
            fill.append((char) (glyph + animationFrame * 16 + textureColumn)).append('\uE101');
        }
        overlay(line, offset + start, fill.toString(), pixels);
    }

    private static void classDiamond(StringBuilder line, Player player, int animationFrame, boolean active) {
        var progress = ExperimentalCombatModule.classProgressSnapshot(player.getUniqueId()).orElse(null);
        if (progress == null || !progress.unlocked()) return;
        int level = progress.effectiveLevel();
        int bandStart = level - progress.localLevel() + 1;
        long currentThreshold = SkillXPCalculator.totalXPForLevel(level)
                - SkillXPCalculator.totalXPForLevel(bandStart);
        double fraction = progress.xp() >= progress.xpAtCap() ? 1
                : (double) (progress.xp() - currentThreshold) / SkillXPCalculator.xpToNextLevel(level);
        int rows = (int) Math.round(28 * Math.max(0, Math.min(1, fraction)));
        overlay(line, 79, String.valueOf((char) (0xE400 + animationFrame * 32 + rows)), 33);
        overlay(line, 91, String.valueOf((char) (0xE520 + (active ? 4 : 0) + animationFrame)), 9);
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

    private static char resourceBar(ClassResourceType type) {
        return switch (type) {
            case RESOLVE -> '\uE840';
            case FURY -> '\uE880';
            case FOCUS -> '\uE8C0';
            case GRACE -> '\uE900';
            case MANA -> '\uE940';
        };
    }
}
