package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.config.SkillsConfig;
import org.bukkit.permissions.Permissible;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** mcMMO-style permission perks: custom overrides fixed perks; otherwise the largest wins. */
final class SkillXpPerks {
    private static final String[] PERKS = {
            "customboost", "quadruple", "triple", "150percentboost", "double",
            "50percentboost", "25percentboost", "10percentboost"
    };
    private static final int[] PERCENTAGES = {0, 400, 300, 250, 200, 150, 125, 110};
    private static final Map<SkillType, String[]> NODES = new EnumMap<>(SkillType.class);
    private static final BigDecimal MAX_XP = BigDecimal.valueOf(Long.MAX_VALUE);

    static {
        for (SkillType skill : SkillType.values()) {
            String[] nodes = new String[PERKS.length];
            for (int i = 0; i < PERKS.length; i++)
                nodes[i] = "elitemobs.perks.xp." + PERKS[i] + "." + skill.name().toLowerCase(Locale.ROOT);
            NODES.put(skill, nodes);
        }
    }

    private SkillXpPerks() { }

    static long apply(Permissible player, SkillType skill, long xp) {
        if (xp <= 0) return 0;
        String[] nodes = NODES.get(skill);
        // Query effective leaf permissions so explicit per-skill denials can override .all.
        for (int i = 0; i < nodes.length; i++) {
            if (!player.hasPermission(nodes[i])) continue;
            if (i == 0) {
                double multiplier = SkillsConfig.getCustomXpPerkMultiplier();
                if (multiplier == 1D) return xp;
                return BigDecimal.valueOf(xp).multiply(BigDecimal.valueOf(multiplier)).min(MAX_XP).longValue();
            }
            // Exact integer scaling, rounded down, without overflowing the intermediate product.
            int percent = PERCENTAGES[i];
            long whole = xp / 100;
            long fraction = xp % 100 * percent / 100;
            if (whole > (Long.MAX_VALUE - fraction) / percent) return Long.MAX_VALUE;
            return whole * percent + fraction;
        }
        return xp;
    }
}
