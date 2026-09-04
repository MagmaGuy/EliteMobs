package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Reusable class-fantasy themes. Mechanics bind to a theme, not to bespoke entity scripts. */
public enum ClassMinionTheme {
    UNDEAD(false, 24D, List.of(
            carrier(EntityType.ZOMBIE, EliteMindBodyLocomotion.GROUNDED),
            carrier(EntityType.SKELETON, EliteMindBodyLocomotion.GROUNDED),
            carrier(EntityType.HUSK, EliteMindBodyLocomotion.GROUNDED))),
    ANIMAL(false, 20D, List.of(
            carrier(EntityType.WOLF, EliteMindBodyLocomotion.GROUNDED),
            carrier(EntityType.FOX, EliteMindBodyLocomotion.GROUNDED),
            carrier(EntityType.POLAR_BEAR, EliteMindBodyLocomotion.GROUNDED))),
    NETHER(false, 30D, List.of(
            carrier(EntityType.PIGLIN_BRUTE, EliteMindBodyLocomotion.GROUNDED),
            carrier(EntityType.HOGLIN, EliteMindBodyLocomotion.GROUNDED),
            carrier(EntityType.BLAZE, EliteMindBodyLocomotion.FLYING))),
    SPIRIT(true, 18D, List.of(
            carrier(EntityType.VEX, EliteMindBodyLocomotion.FLYING)));

    private final boolean supportRole;
    private final double baseHealth;
    private final List<Carrier> carriers;

    ClassMinionTheme(boolean supportRole, double baseHealth, List<Carrier> carriers) {
        this.supportRole = supportRole;
        this.baseHealth = baseHealth;
        this.carriers = List.copyOf(carriers);
    }

    public boolean supportRole() {
        return supportRole;
    }

    public double baseHealth() {
        return baseHealth;
    }

    public List<Carrier> carriers() {
        return carriers;
    }

    public Carrier carrierFor(int sequence) {
        return carriers.get(Math.floorMod(sequence, carriers.size()));
    }

    public static Optional<ClassMinionTheme> forAbility(String abilityId) {
        if (abilityId == null) return Optional.empty();
        String normalized = abilityId.toLowerCase(Locale.ROOT);
        if (normalized.equals("necromancer.signature")
                || normalized.equals("lich.signature")
                || normalized.equals("plaguebringer.signature")) return Optional.of(UNDEAD);
        if (normalized.equals("summoner.signature")) return Optional.of(ANIMAL);
        if (normalized.equals("demonologist.signature")) return Optional.of(NETHER);
        if (normalized.equals("spiritbinder.signature")) return Optional.of(SPIRIT);
        return Optional.empty();
    }

    private static Carrier carrier(EntityType entityType, EliteMindBodyLocomotion locomotion) {
        return new Carrier(entityType, locomotion);
    }

    public record Carrier(EntityType entityType, EliteMindBodyLocomotion locomotion) {
        public Carrier {
            if (entityType == null || locomotion == null) throw new IllegalArgumentException("Carrier is incomplete");
            if (!entityType.isAlive() || !entityType.isSpawnable()) {
                throw new IllegalArgumentException("Carrier must be a spawnable living entity: " + entityType);
            }
        }
    }
}
