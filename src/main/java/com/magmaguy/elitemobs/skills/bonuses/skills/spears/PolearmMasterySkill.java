package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Polearm Mastery (PASSIVE) - Significant attack speed and damage increase.
 * Tier 4 unlock.
 */
public class PolearmMasterySkill extends SkillBonus {

    public static final String SKILL_ID = "spears_polearm_mastery";
    private static final double BASE_DAMAGE_BONUS = 0.20; // 20% damage bonus
    private static final double BASE_ATTACK_SPEED_BONUS = 0.15; // 15% attack speed

    private static final Set<UUID> activePlayers = new HashSet<>();

    public PolearmMasterySkill() {
        super(SkillType.SPEARS, 75, "Polearm Mastery",
              "Significant attack speed and damage increase.",
              SkillBonusType.PASSIVE, 4, SKILL_ID);
    }

    public double getDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BONUS + (skillLevel * 0.003);
    }

    public double getAttackSpeedBonus(int skillLevel) {
        return BASE_ATTACK_SPEED_BONUS + (skillLevel * 0.002);
    }

    /**
     * Gets the damage multiplier for this passive.
     * Applied to all spear attacks when active.
     */
    public double getDamageMultiplier(int skillLevel) {
        if (!activePlayers.isEmpty()) {
            return 1.0 + getDamageBonus(skillLevel);
        }
        return 1.0;
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
        // Attack speed bonus would be applied through attribute modifiers
        // This is handled at the combat/item system level
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
            "&7Damage: &f+" + String.format("%.0f", getDamageBonus(skillLevel) * 100) + "%",
            "&7Attack Speed: &f+" + String.format("%.0f", getAttackSpeedBonus(skillLevel) * 100) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% Damage, +%.0f%% Speed",
            getDamageBonus(skillLevel) * 100, getAttackSpeedBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
