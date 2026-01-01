package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Grim Reach (PASSIVE) - Passive damage bonus simulating extended reach.
 * Increases base damage with hoes.
 * Tier 1 unlock.
 */
public class GrimReachSkill extends SkillBonus {

    public static final String SKILL_ID = "hoes_grim_reach";
    private static final double BASE_DAMAGE_BONUS = 0.15; // 15% bonus

    private static final Set<UUID> activePlayers = new HashSet<>();

    public GrimReachSkill() {
        super(SkillType.HOES, 10, "Grim Reach",
              "Extended reach allows you to deal more damage.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 15% + 0.3% per level
        return BASE_DAMAGE_BONUS + (skillLevel * 0.003);
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
        double bonusPercent = getDamageMultiplier(skillLevel) * 100;
        return List.of(
                "&7Damage Bonus: &f+" + String.format("%.1f", bonusPercent) + "%",
                "&7Extended attack range",
                "&7Always active"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Damage", getDamageMultiplier(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
