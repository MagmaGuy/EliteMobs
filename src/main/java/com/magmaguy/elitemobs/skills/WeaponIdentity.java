package com.magmaguy.elitemobs.skills;

import org.bukkit.NamespacedKey;

import java.util.Objects;

/** Stable gameplay identity for a weapon, independent of the material used to render it. */
public record WeaponIdentity(NamespacedKey id, SkillType progressionSkill) {
    public WeaponIdentity {
        id = Objects.requireNonNull(id, "id");
        progressionSkill = Objects.requireNonNull(progressionSkill, "progressionSkill");
        if (!progressionSkill.isWeaponSkill())
            throw new IllegalArgumentException("A weapon identity must use a weapon skill");
    }
}
