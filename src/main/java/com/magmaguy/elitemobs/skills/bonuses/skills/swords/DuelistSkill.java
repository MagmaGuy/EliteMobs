package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Duelist (CONDITIONAL) - Deals bonus damage when fighting a single enemy.
 * Condition: No other elite mobs within 10 blocks.
 * Tier 3 unlock.
 */
public class DuelistSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "swords_duelist";
    private static final double BASE_DAMAGE_BONUS = 0.25; // 25% bonus
    private static final double DETECTION_RADIUS = 10.0;

    private static final Set<UUID> activePlayers = new HashSet<>();

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
        // Base 25% + 0.5% per level
        return BASE_DAMAGE_BONUS + (skillLevel * 0.005);
    }

    /**
     * Applies the duelist bonus to damage if conditions are met.
     */
    public static double applyDuelistBonus(Player player, EliteMobDamagedByPlayerEvent event, double currentDamage) {
        if (!activePlayers.contains(player.getUniqueId())) return currentDamage;

        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return currentDamage;

        // Check condition
        long nearbyElites = target.getLivingEntity().getNearbyEntities(DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS)
                .stream()
                .filter(e -> com.magmaguy.elitemobs.entitytracker.EntityTracker.isEliteMob(e))
                .filter(e -> !e.getUniqueId().equals(target.getLivingEntity().getUniqueId()))
                .count();

        if (nearbyElites > 0) return currentDamage;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double bonus = BASE_DAMAGE_BONUS + (skillLevel * 0.005);

        return currentDamage * (1 + bonus);
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
        return List.of(
                "&7Damage Bonus: &f+" + String.format("%.1f", bonus) + "%",
                "&7Condition: No other elites within 10 blocks",
                "&7Active only in 1v1 situations"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% 1v1 Damage", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
