package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;

import java.util.Objects;

/** Observable summon balance levers shared by runtime execution and static analysis. */
public record ClassMinionBalanceContract(
        int summonCount,
        int ownerCap,
        int uptimeTicks,
        double maxHealth,
        double damageMultiplierPerHit,
        double scaledDamageMultiplierPerHit,
        int attackPeriodTicks,
        double resourceCost) {
    public static final int DEFAULT_OWNER_CAP = 3;

    public ClassMinionBalanceContract {
        if (summonCount < 1 || ownerCap < 1 || summonCount > ownerCap) {
            throw new IllegalArgumentException("Summon counts must fit within the owner cap");
        }
        if (uptimeTicks < 1 || maxHealth <= 0D || attackPeriodTicks < 1
                || damageMultiplierPerHit < 0D || scaledDamageMultiplierPerHit < 0D
                || resourceCost <= 0D) {
            throw new IllegalArgumentException("Summon balance values must be positive");
        }
    }

    public static ClassMinionBalanceContract from(
            FixedAbilitySpec spec,
            ClassMinionTheme theme,
            int effectiveLevel) {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(theme, "theme");
        if (spec.family() != AbilityFamily.SUMMON) {
            throw new IllegalArgumentException("Only summon abilities have minion contracts: " + spec.id());
        }
        int boundedLevel = Math.max(1, effectiveLevel);
        double levelScale = AbilityLevelScaling.multiplier(boundedLevel);
        int uptime = Math.max(20, spec.tuning().durationTicks());
        int repetitions = Math.max(1, spec.tuning().repetitions());
        int count = Math.min(DEFAULT_OWNER_CAP, Math.max(1, spec.tuning().projectileCount()));
        double damage = spec.tuning().damageMultiplier();
        return new ClassMinionBalanceContract(
                count,
                DEFAULT_OWNER_CAP,
                uptime,
                theme.baseHealth() * levelScale,
                damage,
                damage * levelScale,
                Math.max(1, uptime / repetitions),
                spec.resourceCost());
    }
}
