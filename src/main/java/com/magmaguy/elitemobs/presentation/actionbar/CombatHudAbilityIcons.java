package com.magmaguy.elitemobs.presentation.actionbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Immutable mapping generated alongside the HUD font providers and 64px artwork. */
final class CombatHudAbilityIcons {
    private static final Map<String, Character> GLYPHS = load();

    private CombatHudAbilityIcons() { }

    static String glyph(String abilityId) {
        Character glyph = GLYPHS.get(abilityId);
        return glyph == null ? "" : glyph.toString();
    }

    private static Map<String, Character> load() {
        Properties properties = new Properties();
        try (InputStream input = CombatHudAbilityIcons.class.getResourceAsStream("/combat-hud-ability-icons.properties")) {
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
