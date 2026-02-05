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
 * Long Reach (PASSIVE) - Increased attack range with spears.
 * Tier 1 unlock.
 *
 * Note: Actual range increase is handled by attribute modifiers in the combat system.
 * This skill class tracks activation state and provides display information.
 */
public class LongReachSkill extends SkillBonus {

    public static final String SKILL_ID = "spears_long_reach";
    private static final double BASE_REACH_BONUS = 0.5; // 0.5 block extra reach

    private static final Set<UUID> activePlayers = new HashSet<>();

    public LongReachSkill() {
        super(SkillType.SPEARS, 10, "Long Reach",
              "Attacks have slightly increased range.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    public double getReachBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_REACH_BONUS + (skillLevel * 0.01);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
        // Attribute-based reach bonus would be applied through
        // Player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)
        // This is handled at the combat system level
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
            "&7Extra Reach: &f+" + String.format("%.1f", getReachBonus(skillLevel)) + " blocks"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getReachBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f Block Reach", getReachBonus(skillLevel));
    }

    @Override
    public boolean affectsDamage() {
        return false; // This is a utility skill
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
