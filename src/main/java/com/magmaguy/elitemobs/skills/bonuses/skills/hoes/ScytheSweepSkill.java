package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Scythe Sweep (PROC) - Attacks have a chance to sweep hit nearby enemies.
 * Deals AoE damage to enemies around the target.
 * Tier 3 unlock.
 */
public class ScytheSweepSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "hoes_scythe_sweep";
    private static final double BASE_PROC_CHANCE = 0.30; // 30% chance
    private static final double SWEEP_RADIUS = 3.0;
    private static final double BASE_SWEEP_MULTIPLIER = 0.50; // 50% of damage

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ScytheSweepSkill() {
        super(SkillType.HOES, 50, "Scythe Sweep",
              "Attacks have a chance to sweep and hit nearby enemies.",
              SkillBonusType.PROC, 3, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.4% per level
        return Math.min(0.6, BASE_PROC_CHANCE + (skillLevel * 0.004));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        double originalDamage = event.getDamage();
        int skillLevel = getPlayerSkillLevel(player);
        double sweepDamage = originalDamage * calculateSweepMultiplier(skillLevel);

        // Visual effect
        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
            target.getLocation().add(0, 0.5, 0), 3, 1, 0.2, 1, 0);

        // Hit nearby enemies
        // Use bypass to prevent recursive skill processing
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.getNearbyEntities(SWEEP_RADIUS, SWEEP_RADIUS, SWEEP_RADIUS).stream()
                .filter(e -> e instanceof LivingEntity && !(e instanceof Player) && !e.equals(target))
                .forEach(e -> ((LivingEntity) e).damage(sweepDamage, player));
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }
    }

    private double calculateSweepMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 50% + 1% per level
        return BASE_SWEEP_MULTIPLIER + (skillLevel * 0.01);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
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
        double procChance = getProcChance(skillLevel) * 100;
        double sweepPercent = calculateSweepMultiplier(skillLevel) * 100;
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Sweep Damage: &f" + String.format("%.0f", sweepPercent) + "% of hit",
                "&7Radius: &f" + String.format("%.1f", SWEEP_RADIUS) + " blocks",
                "&7AoE cleave attack"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateSweepMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.0f%% AoE Sweep", calculateSweepMultiplier(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
