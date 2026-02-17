package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tier 2 ARMOR skill - Retaliation
 * Chance to reflect damage back to attackers
 */
public class RetaliationSkill extends SkillBonus implements ProcSkill {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public RetaliationSkill() {
        super(
            SkillType.ARMOR,
            25,
            "Retaliation",
            "Chance to reflect damage back to attackers",
            SkillBonusType.PROC,
            2,
            "armor_retaliation"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        // No persistent bonus to remove
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
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "reflectDamage", String.format("%.1f", getReflectPercent(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getProcChance(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "reflectDamage", String.format("%.1f", getReflectPercent(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }

    // ProcSkill interface methods

    @Override
    public double getProcChance(int skillLevel) {
        // Base 25% chance
        return 0.25;
    }

    @Override
    public void onProc(Player player, Object context) {
        // Context should be the attacker entity and damage amount
        if (context instanceof Object[] params && params.length >= 2) {
            if (params[0] instanceof LivingEntity attacker && params[1] instanceof Double damage) {
                // Reflect damage back to attacker
                int skillLevel = getPlayerSkillLevel(player);
                double reflectDamage = damage * getReflectPercent(skillLevel);
                // Use bypass to prevent recursive skill processing
                EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
                try {
                    attacker.damage(reflectDamage, player);
                } finally {
                    EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
                }

                // Visual effect
                player.getWorld().spawnParticle(Particle.CRIT,
                    attacker.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0);
            }
        }
    }

    /**
     * Gets the percentage of damage to reflect.
     *
     * @param skillLevel The player's skill level
     * @return The reflect percentage (0.0 to 1.0)
     */
    private double getReflectPercent(int skillLevel) {
        // Base 30% + scaled value, capped at 50%
        return Math.min(0.50, 0.30 * getScaledValue(skillLevel));
    }

    /**
     * Attempts to trigger retaliation when player takes damage.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param attacker The entity dealing damage
     * @param damage The damage amount
     */
    public void onDamageTaken(Player player, LivingEntity attacker, double damage) {
        if (!isActive(player)) {
            return;
        }

        int skillLevel = getPlayerSkillLevel(player);
        if (ThreadLocalRandom.current().nextDouble() < getProcChance(skillLevel)) {
            onProc(player, new Object[]{attacker, damage});
            incrementProcCount(player);
        }
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
