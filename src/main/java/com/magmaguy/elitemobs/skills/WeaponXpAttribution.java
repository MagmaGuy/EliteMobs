package com.magmaguy.elitemobs.skills;

import java.util.EnumMap;
import java.util.Map;

/** Pure conversion from hit-time weapon damage attribution to kill-time XP shares. */
final class WeaponXpAttribution {
    private WeaponXpAttribution() {
    }

    static Map<SkillType, Long> distribute(
            long earnedXp,
            double totalPlayerDamage,
            Map<SkillType, Double> contributions) {
        if (earnedXp <= 0L || !Double.isFinite(totalPlayerDamage)
                || totalPlayerDamage <= 0D || contributions == null || contributions.isEmpty())
            return Map.of();

        EnumMap<SkillType, Long> shares = new EnumMap<>(SkillType.class);
        for (Map.Entry<SkillType, Double> entry : contributions.entrySet()) {
            SkillType skill = entry.getKey();
            Double attributed = entry.getValue();
            if (skill == null || !skill.isWeaponSkill() || attributed == null
                    || !Double.isFinite(attributed) || attributed <= 0D)
                continue;
            long share = (long) Math.floor(earnedXp * Math.min(1D, attributed / totalPlayerDamage));
            if (share > 0L) shares.put(skill, share);
        }
        return Map.copyOf(shares);
    }
}
