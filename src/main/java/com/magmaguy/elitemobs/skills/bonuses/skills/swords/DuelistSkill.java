package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
 * Duelist (CONDITIONAL) - Deals bonus damage when fighting a single enemy.
 * Condition: No other elite mobs within 10 blocks.
 * Tier 3 unlock.
 */
public class DuelistSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "swords_duelist";
    private static final double BASE_DAMAGE_BONUS = 0.45; // 45% bonus
    private static final double DETECTION_RADIUS = 10.0;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public DuelistSkill() {
        super(SkillType.SWORDS, 50, "Duelist",
              "Deal bonus damage when fighting a single enemy.",
              SkillBonusType.CONDITIONAL, 3, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;

        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return false;

        // Count elite mobs within detection radius
        long nearbyElites = target.getLivingEntity().getNearbyEntities(DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS)
                .stream()
                .filter(e -> com.magmaguy.elitemobs.entitytracker.EntityTracker.isEliteMob(e))
                .filter(e -> !e.getUniqueId().equals(target.getLivingEntity().getUniqueId()))
                .count();

        return nearbyElites == 0;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // Power budget: standard conditional band - a 1.75x hit at level 50
        // (E = 0.267 * 0.75 = 0.20). Base 45% + 0.6% per level.
        return scaled(BASE_DAMAGE_BONUS, 0.006, skillLevel);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonus = getConditionalBonus(skillLevel) * 100;
        return applyLoreTemplates(Map.of("value", String.format("%.1f", bonus)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getConditionalBonus(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
