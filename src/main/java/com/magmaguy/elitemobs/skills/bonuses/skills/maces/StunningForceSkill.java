package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stunning Force (PASSIVE) - Increased knockback, chance to root enemies.
 * Tier 3 unlock.
 */
public class StunningForceSkill extends SkillBonus {

    public static final String SKILL_ID = "maces_stunning_force";
    private static final double BASE_KNOCKBACK_MULTIPLIER = 1.5;
    private static final double BASE_ROOT_CHANCE = 0.15;
    private static final int ROOT_DURATION_TICKS = 40; // 2 seconds

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public StunningForceSkill() {
        super(SkillType.MACES, 50, "Stunning Force",
              "Increased knockback. Chance to root enemies.",
              SkillBonusType.PASSIVE, 3, SKILL_ID);
    }

    public double getKnockbackMultiplier(int skillLevel) {
        return BASE_KNOCKBACK_MULTIPLIER + (skillLevel * 0.02);
    }

    public double getRootChance(int skillLevel) {
        return Math.min(0.35, BASE_ROOT_CHANCE + (skillLevel * 0.003));
    }

    /**
     * Applies stunning force effects on hit.
     */
    public void onHit(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);

        // Apply enhanced knockback
        double knockbackMultiplier = getKnockbackMultiplier(skillLevel);
        Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        direction.setY(0.3); // Add upward component
        target.setVelocity(direction.multiply(knockbackMultiplier));

        // Check for root proc
        if (Math.random() < getRootChance(skillLevel)) {
            applyRoot(player, target);
        }
    }

    private void applyRoot(Player player, LivingEntity target) {
        // Apply slowness 127 (effective root)
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ROOT_DURATION_TICKS, 127));

        // Visual effect
        target.getWorld().spawnParticle(Particle.BLOCK,
            target.getLocation(), 20, 0.3, 0.1, 0.3, 0,
            org.bukkit.Material.IRON_BARS.createBlockData());
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 0.5f);
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
        return applyLoreTemplates(Map.of(
                "knockback", String.format("%.0f", getKnockbackMultiplier(skillLevel) * 100),
                "rootChance", String.format("%.1f", getRootChance(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getKnockbackMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "knockback", String.format("%.0f", getKnockbackMultiplier(skillLevel) * 100),
                "rootChance", String.format("%.1f", getRootChance(skillLevel) * 100)
        ));
    }

    @Override
    public boolean affectsDamage() {
        return false; // This is a utility skill, not a damage skill
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
