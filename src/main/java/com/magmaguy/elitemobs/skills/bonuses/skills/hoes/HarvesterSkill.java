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
 * Harvester (PASSIVE) - Passive damage and loot bonus.
 * Increases base damage and improves loot quality.
 * Tier 2 unlock.
 */
public class HarvesterSkill extends SkillBonus {

    public static final String SKILL_ID = "hoes_harvester";
    private static final double BASE_DAMAGE_BONUS = 0.10; // 10% damage
    private static final double BASE_LOOT_BONUS = 0.15; // 15% loot quality

    private static final Set<UUID> activePlayers = new HashSet<>();

    public HarvesterSkill() {
        super(SkillType.HOES, 25, "Harvester",
              "Reap better rewards from your enemies.",
              SkillBonusType.PASSIVE, 2, SKILL_ID);
    }

    public double getDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 10% + 0.2% per level
        return BASE_DAMAGE_BONUS + (skillLevel * 0.002);
    }

    public double getLootBonus(int skillLevel) {
        if (configFields != null) {
            // Use a secondary value or same value for loot
            return configFields.calculateValue(skillLevel) * 1.5;
        }
        // Base 15% + 0.3% per level
        return BASE_LOOT_BONUS + (skillLevel * 0.003);
    }

    /**
     * Gets the loot quality multiplier for a player.
     * Called from loot drop events.
     */
    public static double getLootMultiplier(Player player) {
        if (!activePlayers.contains(player.getUniqueId())) return 1.0;

        // Get the instance from registry
        SkillBonus skill = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getSkillById(SKILL_ID);
        if (!(skill instanceof HarvesterSkill harvester)) return 1.0;

        int skillLevel = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry
            .getPlayerSkillLevel(player, SkillType.HOES);
        return 1.0 + harvester.getLootBonus(skillLevel);
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
        double damagePercent = getDamageBonus(skillLevel) * 100;
        double lootPercent = getLootBonus(skillLevel) * 100;
        return List.of(
                "&7Damage Bonus: &f+" + String.format("%.1f", damagePercent) + "%",
                "&7Loot Quality: &f+" + String.format("%.1f", lootPercent) + "%",
                "&7Passive harvest benefits"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Damage & Loot", getDamageBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
