package com.magmaguy.elitemobs.presentation.actionbar;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Shared slot icons, generated alongside the HUD font providers and ability artwork. */
final class CombatHudAbilityIcons {
    private static final Map<String, Character> GLYPHS = load("/combat-hud-ability-icons.properties");

    private CombatHudAbilityIcons() { }

    /** The slot's icon, or its disabled variant when the ability cannot be cast; falls back to the normal icon. */
    static String glyph(AbilitySlot slot, boolean usable) {
        Character glyph = usable ? null : GLYPHS.get(slot.name() + "_DISABLED");
        if (glyph == null) glyph = GLYPHS.get(slot.name());
        return glyph == null ? "" : glyph.toString();
    }

    private static Map<String, Character> load(String resource) {
        Properties properties = new Properties();
        try (InputStream input = CombatHudAbilityIcons.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Missing HUD ability icon mapping");
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load HUD ability icon mapping", exception);
        }
        Map<String, Character> result = new HashMap<>();
        properties.forEach((id, value) -> result.put(id.toString(), (char) Integer.parseInt(value.toString(), 16)));
        return Map.copyOf(result);
    }
}
