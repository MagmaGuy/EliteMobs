package com.magmaguy.elitemobs.experimentalcombat.classes;

import com.magmaguy.elitemobs.skills.SkillType;

import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

/**
 * Immutable definition for either a root class or one specialization form.
 */
public record ClassFormDefinition(
        String id,
        String displayName,
        ClassBand band,
        String parentId,
        FoundationSkillPair foundationSkills,
        RootClassKit rootKit,
        AbilityDefinition signature,
        AbilityDefinition utility,
        PassiveDefinition passive,
        List<SkillType> weaponAffinities) {
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9_]*");
    private static final int PERSISTED_ID_MAX_LENGTH = 64;

    public ClassFormDefinition {
        id = requireId(id);
        displayName = requireText(displayName, "displayName");
        band = Objects.requireNonNull(band, "band");
        foundationSkills = Objects.requireNonNull(foundationSkills, "foundationSkills");
        signature = requireSlot(signature, AbilitySlot.SIGNATURE);
        utility = requireSlot(utility, AbilitySlot.UTILITY);
        passive = Objects.requireNonNull(passive, "passive");
        weaponAffinities = List.copyOf(weaponAffinities);
        if (weaponAffinities.isEmpty() || weaponAffinities.contains(SkillType.ARMOR)
                || weaponAffinities.stream().distinct().count() != weaponAffinities.size())
            throw new IllegalArgumentException("Weapon affinities require distinct weapon skills");

        if (band.isRoot()) {
            if (parentId != null) throw new IllegalArgumentException("Root form " + id + " must not have a parent");
            rootKit = Objects.requireNonNull(rootKit, "rootKit");
        } else {
            parentId = requireId(parentId);
            if (rootKit != null) throw new IllegalArgumentException("Specialization " + id + " must inherit its root kit");
        }
    }

    public static ClassFormDefinition root(
            String id,
            String displayName,
            FoundationSkillPair foundationSkills,
            RootClassKit rootKit,
            AbilityDefinition signature,
            AbilityDefinition utility,
            PassiveDefinition passive) {
        return new ClassFormDefinition(id, displayName, ClassBand.ROOT, null, foundationSkills, rootKit,
                signature, utility, passive, foundationWeapons(foundationSkills));
    }

    public static ClassFormDefinition specialization(
            String id,
            String displayName,
            ClassBand band,
            String parentId,
            FoundationSkillPair foundationSkills,
            AbilityDefinition signature,
            AbilityDefinition utility,
            PassiveDefinition passive) {
        if (band.isRoot()) throw new IllegalArgumentException("A specialization cannot use the root band");
        return new ClassFormDefinition(id, displayName, band, parentId, foundationSkills, null,
                signature, utility, passive, foundationWeapons(foundationSkills));
    }

    /** Explicit weapon bonuses may differ from the skills used to level this form. */
    public ClassFormDefinition withWeaponAffinities(SkillType... weapons) {
        return new ClassFormDefinition(id, displayName, band, parentId, foundationSkills, rootKit,
                signature, utility, passive, List.of(weapons));
    }

    private static List<SkillType> foundationWeapons(FoundationSkillPair skills) {
        return skills.asList().stream().filter(skill -> skill != SkillType.ARMOR).toList();
    }

    public Optional<String> optionalParentId() {
        return Optional.ofNullable(parentId);
    }

    public Optional<RootClassKit> optionalRootKit() {
        return Optional.ofNullable(rootKit);
    }

    public int requiredFoundationSkillLevel() {
        return band.skillUnlockLevel();
    }

    public boolean meetsFoundationRequirement(ToIntFunction<SkillType> levelProvider) {
        return effectiveSkillCap(levelProvider) >= requiredFoundationSkillLevel();
    }

    /** The uncapped effective-level ceiling imposed by this form's own two foundation skills. */
    public int effectiveSkillCap(ToIntFunction<SkillType> levelProvider) {
        return foundationSkills.limitingLevel(levelProvider);
    }

    /** The effective level this form can store, including its branching-band ceiling. */
    public int effectiveProgressionCap(ToIntFunction<SkillType> levelProvider) {
        return Math.min(effectiveSkillCap(levelProvider), band.effectiveEnd());
    }

    /** Returns zero while locked, otherwise the number of local levels this form may earn. */
    public int localProgressionCap(ToIntFunction<SkillType> levelProvider) {
        int effectiveCap = effectiveProgressionCap(levelProvider);
        if (effectiveCap < band.effectiveStart()) return 0;
        return band.toLocalLevel(effectiveCap);
    }

    private static AbilityDefinition requireSlot(AbilityDefinition ability, AbilitySlot slot) {
        Objects.requireNonNull(ability, slot.name().toLowerCase());
        if (ability.slot() != slot)
            throw new IllegalArgumentException("Expected a " + slot + " ability, got " + ability.slot());
        return ability;
    }

    private static String requireId(String id) {
        String value = requireText(id, "id");
        if (value.length() > PERSISTED_ID_MAX_LENGTH)
            throw new IllegalArgumentException("Form id must not exceed "
                    + PERSISTED_ID_MAX_LENGTH + " characters: " + value);
        if (!ID_PATTERN.matcher(value).matches())
            throw new IllegalArgumentException("Form id must be a stable lowercase identifier: " + value);
        return value;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        if (!value.equals(value.strip())) throw new IllegalArgumentException(field + " must not have surrounding whitespace");
        return value;
    }
}
