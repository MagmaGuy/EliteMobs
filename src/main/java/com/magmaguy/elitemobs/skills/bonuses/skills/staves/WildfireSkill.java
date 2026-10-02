package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wildfire (CONDITIONAL) - Fireballs hit harder the more enemies crowd around the target.
 * Tier 2 unlock.
 * <p>
 * Counts other enemies within 4 blocks of each struck target. Fireball damage gains +15% per enemy
 * at level 50, up to four. Power budget: blasts in pack content average about 1.3 other enemies,
 * so E = 1.3 * 0.15 = 0.20; a lone target gains nothing.
 */
public class WildfireSkill extends MagicWeaponSkill implements ConditionalSkill {

    public static final String SKILL_ID = "staves_wildfire";
    private static final double CROWD_RADIUS = 4.0;
    private static final int MAX_CROWD = 4;

    private final Map<UUID, Integer> crowd = new ConcurrentHashMap<>();

    public WildfireSkill() {
        super(SkillType.STAVES, 2, "Wildfire", "Fireballs hit harder when enemies crowd together.",
                SkillBonusType.CONDITIONAL, SKILL_ID);
    }

    private double bonusPerEnemy(int skillLevel) {
        return scaled(0.10, 0.001, skillLevel); // 15% at level 50
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return false;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        int others = Math.min(MAX_CROWD, enemiesNear(player, target.getLocation(), CROWD_RADIUS, target).size());
        if (others == 0) return false;
        crowd.put(player.getUniqueId(), others);
        return true;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return bonusPerEnemy(skillLevel) * MAX_CROWD;
    }

    @Override
    public double getConditionalBonus(Player player, int skillLevel) {
        return bonusPerEnemy(skillLevel) * crowd.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    protected void clearPlayer(UUID playerId) {
        crowd.remove(playerId);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bonusPerEnemy", String.format("%.1f", bonusPerEnemy(skillLevel) * 100),
                "maxBonus", String.format("%.0f", getConditionalBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("maxBonus", String.format("%.0f", getConditionalBonus(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        crowd.clear();
        super.shutdown();
    }
}
