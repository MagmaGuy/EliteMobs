package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swift Strikes (PASSIVE) - Increases movement speed while wielding a sword.
 * Always active when selected.
 * Tier 1 unlock.
 */
public class SwiftStrikesSkill extends SkillBonus {

    public static final String SKILL_ID = "swords_swift_strikes";
    private static final double BASE_SPEED_BONUS = 0.05; // 5% movement speed

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public SwiftStrikesSkill() {
        super(SkillType.SWORDS, 10, "Swift Strikes",
              "Move faster while wielding a sword.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    /**
     * Gets the movement speed bonus for a skill level.
     */
    public static double getSpeedBonus(int skillLevel) {
        // Base 5% + 0.1% per level, max 15%
        return Math.min(0.15, BASE_SPEED_BONUS + (skillLevel * 0.001));
    }

    /**
     * Checks if a player has this skill active.
     */
    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    /**
     * Applies the speed bonus to the player's walk speed attribute.
     */
    public static void applySpeedBonus(Player player, int skillLevel) {
        double bonus = getSpeedBonus(skillLevel);
        player.setWalkSpeed(Math.min(1.0f, 0.2f + (float) bonus));
    }

    /**
     * Removes the speed bonus, restoring default walk speed.
     */
    public static void removeSpeedBonus(Player player) {
        player.setWalkSpeed(0.2f);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeSpeedBonus(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeSpeedBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double speed = getSpeedBonus(skillLevel) * 100;
        return applyLoreTemplates(Map.of("value", String.format("%.1f", speed)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getSpeedBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getSpeedBonus(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // Movement speed skill doesn't affect damage
    }

    @Override
    public TestStrategy getTestStrategy() {
        return TestStrategy.ATTRIBUTE_CHECK;
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
