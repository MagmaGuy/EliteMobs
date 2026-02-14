package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 1 ARMOR skill - Iron Stance
 * Provides damage reduction when standing still
 */
public class IronStanceSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "armor_iron_stance";

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> lastMoveTime = new ConcurrentHashMap<>();
    private static final long STAND_TIME_REQUIRED = 1000; // 1 second

    public IronStanceSkill() {
        super(
            SkillType.ARMOR,
            10,
            "Iron Stance",
            "Gain damage reduction when standing still",
            SkillBonusType.CONDITIONAL,
            1,
            SKILL_ID
        );
    }

    /**
     * Simulates the player being stationary for testing purposes.
     * Removes movement tracking so conditionMet() returns true.
     */
    public static void simulateStationary(UUID uuid) {
        lastMoveTime.remove(uuid);
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
        lastMoveTime.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "reduction", String.format("%.1f", getConditionalBonus(skillLevel) * 50.0)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        lastMoveTime.clear();
    }

    // ConditionalSkill interface methods

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!isActive(player)) {
            return false;
        }

        Long lastMove = lastMoveTime.get(player.getUniqueId());
        if (lastMove == null) {
            return true; // Never moved
        }
        return System.currentTimeMillis() - lastMove >= STAND_TIME_REQUIRED;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return getScaledValue(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "reduction", String.format("%.1f", getConditionalBonus(skillLevel) * 50.0)));
    }

    @Override
    public TestStrategy getTestStrategy() { return TestStrategy.CONDITION_SETUP; }

    /**
     * Called to update player movement state.
     * Should be called from a movement listener.
     *
     * @param uuid The player's UUID
     * @param isMoving Whether the player is currently moving
     */
    public static void updatePlayerMovement(UUID uuid, boolean isMoving) {
        if (isMoving) {
            lastMoveTime.put(uuid, System.currentTimeMillis());
        }
    }

    /**
     * Calculates damage reduction when condition is met.
     *
     * @param player The player
     * @param originalDamage The original damage amount
     * @param context The damage event context
     * @return The modified damage amount
     */
    public double modifyIncomingDamage(Player player, double originalDamage, Object context) {
        if (conditionMet(player, context)) {
            int skillLevel = getPlayerSkillLevel(player);
            // Reduce damage by up to 50% based on skill level
            return originalDamage * (1 - getConditionalBonus(skillLevel) * 0.5);
        }
        return originalDamage;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
