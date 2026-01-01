package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Soul Drain (PROC) - Attacks have a chance to drain health from the target.
 * Heals the player for a percentage of damage dealt.
 * Tier 1 unlock.
 */
public class SoulDrainSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "hoes_soul_drain";
    private static final double BASE_PROC_CHANCE = 0.20; // 20% base chance
    private static final double BASE_DRAIN_MULTIPLIER = 0.30; // 30% of damage

    private static final Set<UUID> activePlayers = new HashSet<>();

    public SoulDrainSkill() {
        super(SkillType.HOES, 10, "Soul Drain",
              "Attacks have a chance to drain health from enemies.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.2% per level
        return Math.min(0.5, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        double damage = event.getDamage();
        int skillLevel = getPlayerSkillLevel(player);
        double drainAmount = damage * calculateDrainMultiplier(skillLevel);

        // Heal player
        double newHealth = Math.min(player.getHealth() + drainAmount, player.getMaxHealth());
        player.setHealth(newHealth);

        // Visual effect
        eliteEntity.getLivingEntity().getWorld().spawnParticle(
            Particle.SOUL,
            eliteEntity.getLivingEntity().getLocation().add(0, 1, 0),
            10, 0.3, 0.5, 0.3, 0.05
        );
    }

    private double calculateDrainMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DRAIN_MULTIPLIER + (skillLevel * 0.005);
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
        double drainPercent = calculateDrainMultiplier(skillLevel) * 100;
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Drain: &f" + String.format("%.1f", drainPercent) + "% of damage",
                "&7Heals you for damage dealt"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateDrainMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% Life Drain", calculateDrainMultiplier(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
