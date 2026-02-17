package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hunter's Prey (CONDITIONAL) - Bonus damage to low health targets.
 * Tier 2 unlock.
 */
public class HuntersPreySkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "crossbows_hunters_prey";
    private static final double HEALTH_THRESHOLD = 0.50; // 50% health
    private static final double BASE_BONUS = 0.25; // 25% bonus

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public HuntersPreySkill() {
        super(SkillType.CROSSBOWS, 25, "Hunter's Prey",
              "Deal bonus damage to targets below 50% health.",
              SkillBonusType.CONDITIONAL, 2, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;
        if (event.getEliteMobEntity().getLivingEntity() == null) return false;
        double healthPercent = event.getEliteMobEntity().getLivingEntity().getHealth() /
                event.getEliteMobEntity().getLivingEntity().getMaxHealth();
        return healthPercent < HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return BASE_BONUS + (skillLevel * 0.004); // 25% base + 0.4% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("value", String.format("%.1f", getConditionalBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getConditionalBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getConditionalBonus(skillLevel) * 100))); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
