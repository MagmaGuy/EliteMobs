package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Phalanx (PASSIVE) - Reduced damage from frontal attacks.
 * Tier 2 unlock.
 */
public class PhalanxSkill extends SkillBonus {

    public static final String SKILL_ID = "spears_phalanx";
    private static final double BASE_DAMAGE_REDUCTION = 0.15; // 15% reduction

    private static final Set<UUID> activePlayers = new HashSet<>();

    public PhalanxSkill() {
        super(SkillType.SPEARS, 25, "Phalanx",
              "Reduced damage from frontal attacks.",
              SkillBonusType.PASSIVE, 2, SKILL_ID);
    }

    /**
     * Calculates the damage reduction for a frontal attack.
     *
     * @param player The player being attacked
     * @param attacker The attacking entity
     * @return The damage multiplier (e.g., 0.85 for 15% reduction)
     */
    public double getDamageMultiplier(Player player, LivingEntity attacker) {
        if (!isActive(player)) return 1.0;

        // Check if attack is from the front (within ~90 degree arc)
        if (!isFrontalAttack(player, attacker)) return 1.0;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double reduction = getDamageReduction(skillLevel);

        return 1.0 - reduction;
    }

    /**
     * Determines if an attack is coming from the player's front.
     */
    private boolean isFrontalAttack(Player player, LivingEntity attacker) {
        Location playerLoc = player.getLocation();
        Location attackerLoc = attacker.getLocation();

        // Get direction player is facing
        Vector playerDirection = playerLoc.getDirection().setY(0).normalize();

        // Get direction from player to attacker
        Vector toAttacker = attackerLoc.toVector().subtract(playerLoc.toVector()).setY(0).normalize();

        // Calculate dot product (1 = same direction, -1 = opposite)
        double dot = playerDirection.dot(toAttacker);

        // Frontal is roughly within 90 degrees (dot > 0)
        return dot > 0;
    }

    public double getDamageReduction(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return Math.min(0.35, BASE_DAMAGE_REDUCTION + (skillLevel * 0.002));
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
        return List.of(
            "&7Frontal Damage Reduction: &f" + String.format("%.0f", getDamageReduction(skillLevel) * 100) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("-%.0f%% Frontal Damage", getDamageReduction(skillLevel) * 100);
    }

    @Override
    public boolean affectsDamage() {
        return false; // Defensive skill
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
