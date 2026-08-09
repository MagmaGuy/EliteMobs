package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Depth Charge (CONDITIONAL) - When target is in water, creates an explosion that damages nearby aquatic enemies.
 * Tier 4 unlock.
 */
public class DepthChargeSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "tridents_depth_charge";
    private static final double AOE_RADIUS = 4.0;
    private static final double BASE_DAMAGE_BONUS = 0.30;
    private static final double DAMAGE_BONUS_PER_LEVEL = 0.0025;
    private static final double BASE_AOE_DAMAGE = 0.10;
    private static final double AOE_DAMAGE_PER_LEVEL = 0.001;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public DepthChargeSkill() {
        super(SkillType.TRIDENTS, 75, "Depth Charge",
              "When target is in water, creates an explosion that damages nearby aquatic enemies.",
              SkillBonusType.CONDITIONAL, 4, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return false;

        // Active when target is in water
        return eliteEntity.getLivingEntity().isInWater();
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // Power budget at the level 100 soft cap: +55% to the primary target plus a 20% secondary hit.
        // At the standard 26.7% conditional trigger rate this totals E = 0.267 * 0.75 = 0.20
        // when one additional aquatic target is caught in the blast.
        double approvedCurve = scaled(BASE_DAMAGE_BONUS, DAMAGE_BONUS_PER_LEVEL, skillLevel);
        if (configFields == null) return approvedCurve;
        double configuredBonus = configFields.calculateValue(skillLevel);
        if (!Double.isFinite(configuredBonus)) return approvedCurve;
        return Math.max(0D, Math.min(configuredBonus, approvedCurve));
    }

    @Override
    public void onConditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (!isActive(player)) return;
        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry
                .getPlayerSkillLevel(player, SkillType.TRIDENTS);
        target.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP,
                target.getLocation(), 50, 2, 2, 2, 0.1);

        double aoeDamage = event.getDamage() * calculateAoeDamage(skillLevel);
        target.getNearbyEntities(AOE_RADIUS, AOE_RADIUS, AOE_RADIUS).stream()
                .filter(entity -> entity instanceof LivingEntity livingEntity
                        && !(entity instanceof Player) && livingEntity.isInWater())
                .forEach(entity -> CombatDamageContext.runPlayerToEliteBypass(
                        () -> ((LivingEntity) entity).damage(aoeDamage, player)));
    }

    private double calculateAoeDamage(int skillLevel) {
        return scaled(BASE_AOE_DAMAGE, AOE_DAMAGE_PER_LEVEL, skillLevel);
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
        double damageBonus = getConditionalBonus(skillLevel) * 100;
        double aoeDamage = calculateAoeDamage(skillLevel) * 100;
        return applyLoreTemplates(Map.of(
                "damageBonus", String.format("%.1f", damageBonus),
                "aoeDamage", String.format("%.1f", aoeDamage),
                "radius", String.valueOf(AOE_RADIUS)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damageBonus", String.format("%.1f", getConditionalBonus(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
