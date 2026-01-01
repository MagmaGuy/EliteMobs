package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tier 1 ARMOR skill - Iron Stance
 * Provides damage reduction when standing still
 */
public class IronStanceSkill extends SkillBonus implements ConditionalSkill {

    private static final HashSet<UUID> activePlayers = new HashSet<>();
    private static final Map<UUID, Long> lastMoveTime = new HashMap<>();
    private static final long STAND_TIME_REQUIRED = 1000; // 1 second

    public IronStanceSkill() {
        super(
            SkillType.ARMOR,
            10,
            "Iron Stance",
            "Gain damage reduction when standing still",
            SkillBonusType.CONDITIONAL,
            1,
            "iron_stance"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        // Conditional bonus is checked on damage event
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
    public List<String> getLoreDescription(int skillLevel) {
        List<String> lore = new ArrayList<>();
        lore.add("Gain damage reduction when standing still");
        lore.add(String.format("Damage Reduction: %.1f%%", getConditionalBonus(skillLevel) * 50.0));
        lore.add("Requires: Standing still for 1 second");
        return lore;
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% Damage Reduction", getConditionalBonus(skillLevel) * 50.0);
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
