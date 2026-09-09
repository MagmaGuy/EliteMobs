package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Objects;

/** Pure decisions shared by passive Bukkit adapters and focused behavioral tests. */
final class PassiveRuntimePolicy {

    private PassiveRuntimePolicy() {
    }

    static int controlDurationTicks(int originalTicks, PassiveMechanics mechanics) {
        Objects.requireNonNull(mechanics, "mechanics");
        if (originalTicks == PotionEffect.INFINITE_DURATION || originalTicks <= 0) return originalTicks;
        return Math.max(1, (int) Math.round(originalTicks * knockbackMultiplier(mechanics)));
    }

    static double knockbackMultiplier(PassiveMechanics mechanics) {
        Objects.requireNonNull(mechanics, "mechanics");
        return 1D - mechanics.controlResistanceFraction();
    }

    static boolean isMagicWeaponSkill(SkillType progressionSkill) {
        return progressionSkill == SkillType.WANDS || progressionSkill == SkillType.STAVES;
    }

    static double partyMovementAdjustment(
            Collection<PassiveMechanics> nearbyPartyMechanics,
            boolean grouped) {
        Objects.requireNonNull(nearbyPartyMechanics, "nearbyPartyMechanics");
        if (!grouped) return 0D;
        return nearbyPartyMechanics.stream()
                .filter(Objects::nonNull)
                .mapToDouble(PassiveMechanics::partyMovementSpeedAdjustment)
                .max()
                .orElse(0D);
    }
}
