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
 * First Blood (CONDITIONAL) - Massive bonus damage on first hit.
 * Tier 4 unlock.
 */
public class FirstBloodSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "crossbows_first_blood";
    private static final double HEALTH_THRESHOLD = 0.99; // Target must be at 99%+ health
    private static final double BASE_BONUS = 0.50; // 50% bonus

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public FirstBloodSkill() {
        super(SkillType.CROSSBOWS, 75, "First Blood",
              "Massive bonus damage on first hit to full health targets.",
              SkillBonusType.CONDITIONAL, 4, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;
        if (event.getEliteMobEntity().getLivingEntity() == null) return false;
        double healthPercent = event.getEliteMobEntity().getLivingEntity().getHealth() /
                event.getEliteMobEntity().getLivingEntity().getMaxHealth();
        return healthPercent >= HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return BASE_BONUS + (skillLevel * 0.005); // 50% base + 0.5% per level
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
    public TestStrategy getTestStrategy() { return TestStrategy.CONDITION_SETUP; }

    @Override
    public void shutdown() { activePlayers.clear(); }
}
