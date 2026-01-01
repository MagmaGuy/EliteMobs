package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusEventHandler;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ToggleSkill;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Blade Dance (TOGGLE) - Gain dodge chance but deal reduced damage.
 * Can be toggled on/off.
 * Tier 4 unlock.
 */
public class BladeDanceSkill extends SkillBonus implements ToggleSkill {

    public static final String SKILL_ID = "swords_blade_dance";
    private static final double BASE_DODGE_CHANCE = 0.15; // 15% dodge
    private static final double BASE_DAMAGE_PENALTY = 0.20; // 20% less damage

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Set<UUID> toggledPlayers = new HashSet<>();

    public BladeDanceSkill() {
        super(SkillType.SWORDS, 75, "Blade Dance",
              "Toggle: Gain dodge chance but deal less damage.",
              SkillBonusType.TOGGLE, 4, SKILL_ID);
    }

    @Override
    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (toggledPlayers.contains(uuid)) {
            disable(player);
        } else {
            enable(player);
        }
    }

    @Override
    public boolean isToggled(Player player) {
        return toggledPlayers.contains(player.getUniqueId());
    }

    @Override
    public void enable(Player player) {
        toggledPlayers.add(player.getUniqueId());
        SkillBonusEventHandler.setToggle(player.getUniqueId(), SKILL_ID, true);
        player.sendMessage("\u00A76Blade Dance \u00A7aenabled\u00A77 - Dodge increased, damage reduced");
    }

    @Override
    public void disable(Player player) {
        toggledPlayers.remove(player.getUniqueId());
        SkillBonusEventHandler.setToggle(player.getUniqueId(), SKILL_ID, false);
        player.sendMessage("\u00A76Blade Dance \u00A7cdisabled");
    }

    @Override
    public double getPositiveBonus(int skillLevel) {
        // Dodge chance: 15% + 0.3% per level
        return Math.min(0.40, BASE_DODGE_CHANCE + (skillLevel * 0.003));
    }

    @Override
    public double getNegativeEffect(int skillLevel) {
        // Damage penalty: 20% - 0.2% per level (penalty decreases with level)
        return Math.max(0.05, BASE_DAMAGE_PENALTY - (skillLevel * 0.002));
    }

    /**
     * Gets the dodge chance for a player with this skill active.
     */
    public static double getDodgeChance(Player player, int skillLevel) {
        if (!toggledPlayers.contains(player.getUniqueId())) return 0;
        return Math.min(0.40, BASE_DODGE_CHANCE + (skillLevel * 0.003));
    }

    /**
     * Gets the damage multiplier (reduction) for a player with this skill active.
     */
    public static double getDamageMultiplier(Player player, int skillLevel) {
        if (!toggledPlayers.contains(player.getUniqueId())) return 1.0;
        double penalty = Math.max(0.05, BASE_DAMAGE_PENALTY - (skillLevel * 0.002));
        return 1.0 - penalty;
    }

    /**
     * Checks if a player has this skill toggled on.
     */
    public static boolean hasActiveToggle(UUID playerUUID) {
        return toggledPlayers.contains(playerUUID);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        toggledPlayers.remove(uuid);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double dodge = getPositiveBonus(skillLevel) * 100;
        double penalty = getNegativeEffect(skillLevel) * 100;
        return List.of(
                "&aPositive: &f+" + String.format("%.1f", dodge) + "% Dodge Chance",
                "&cNegative: &f-" + String.format("%.1f", penalty) + "% Damage",
                "&7Right-click with sword to toggle"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getPositiveBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Dodge (toggle)", getPositiveBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        toggledPlayers.clear();
    }
}
