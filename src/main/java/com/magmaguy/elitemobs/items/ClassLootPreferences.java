package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import java.util.Set;

/** Equipment preferences supplement the class catalog's existing weapon affinities. */
public record ClassLootPreferences(Armor armor, boolean shields) {
    public enum Armor { DPS, TANK, BOTH }

    // Explicit specialization choices; an ARMOR foundation skill alone does not make a tank.
    private static final Set<String> TANKS = Set.of("paladin", "guardian", "aegis", "bulwark",
            "shieldbearer", "templar", "bannerlord", "deathless", "dreadnought", "colossus", "arcane_knight");
    private static final Set<String> HYBRIDS = Set.of("justicar", "marshal", "juggernaut", "warmonger",
            "battlemage", "cryomancer");
    private static final Set<String> SHIELD_USERS = Set.of("paladin", "guardian", "aegis", "bulwark",
            "shieldbearer", "justicar", "templar", "marshal", "bannerlord");

    public static ClassLootPreferences defaults(String classId) {
        return new ClassLootPreferences(TANKS.contains(classId) ? Armor.TANK
                : HYBRIDS.contains(classId) ? Armor.BOTH : Armor.DPS, SHIELD_USERS.contains(classId));
    }

    public boolean relevant(ClassFormDefinition form, ClassLootFamily family) {
        if (family.isWeapon()) return form.weaponAffinities().contains(family.skill());
        if (family == ClassLootFamily.SHIELDS) return shields;
        return armor == Armor.BOTH || family.name().startsWith(armor.name() + "_");
    }
}
