package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Timber (PASSIVE) - Axes deal bonus damage and have increased shield disable.
 * Tier 4 unlock.
 */
public class TimberSkill extends SkillBonus {

    public static final String SKILL_ID = "axes_timber";
    private static final double BASE_DAMAGE_BONUS = 0.15;
    private static final double BASE_SHIELD_DISABLE_BONUS = 0.30;

    private static final Set<UUID> activePlayers = new HashSet<>();

    public TimberSkill() {
        super(SkillType.AXES, 75, "Timber",
              "Increased damage and improved shield breaking.",
              SkillBonusType.PASSIVE, 4, SKILL_ID);
    }

    public static double getDamageBonus(int skillLevel) {
        return BASE_DAMAGE_BONUS + (skillLevel * 0.002);
    }

    public static double getShieldDisableBonus(int skillLevel) {
        return BASE_SHIELD_DISABLE_BONUS + (skillLevel * 0.003);
    }

    public static boolean hasActiveSkill(UUID uuid) { return activePlayers.contains(uuid); }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&7Damage Bonus: &f+" + String.format("%.1f", getDamageBonus(skillLevel) * 100) + "%",
                "&7Shield Disable: &f+" + String.format("%.0f", getShieldDisableBonus(skillLevel) * 100) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getDamageBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.1f%% Damage", getDamageBonus(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
