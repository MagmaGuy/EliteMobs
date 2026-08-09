package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Steady Aim (CONDITIONAL) - Bonus damage when standing still.
 * Tier 1 unlock.
 */
public class SteadyAimSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "crossbows_steady_aim";
    private static final long STAND_TIME_REQUIRED = 1500; // 1.5 seconds
    // Budgeted against how often the condition ACTUALLY holds. Crossbow users stand still to fire
    // as a matter of course, so this is up roughly half the time: 1 + 0.20/0.50 = 1.40x.
    private static final double BASE_BONUS = 0.25;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> standingStillSince = new ConcurrentHashMap<>();

    public SteadyAimSkill() {
        super(SkillType.CROSSBOWS, 10, "Steady Aim",
              "Standing still increases your damage.",
              SkillBonusType.CONDITIONAL, 1, SKILL_ID);
    }

    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    public static void updatePlayerMovement(UUID uuid, boolean isMoving) {
        if (isMoving) {
            standingStillSince.remove(uuid);
        } else if (!standingStillSince.containsKey(uuid)) {
            standingStillSince.put(uuid, System.currentTimeMillis());
        }
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        Long stillSince = standingStillSince.get(player.getUniqueId());
        if (stillSince == null) return false;
        return System.currentTimeMillis() - stillSince >= STAND_TIME_REQUIRED;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // Power budget: standard conditional band - a 1.75x hit at level 50
        // (E = 0.267 * 0.75 = 0.20).
        return scaled(BASE_BONUS, 0.003, skillLevel); // 25% base + 0.3% per level -> 1.40x at level 50
    }

    public double calculateBonus(Player player, int skillLevel) {
        Long stillSince = standingStillSince.get(player.getUniqueId());
        if (stillSince == null) return 0;
        long standTime = System.currentTimeMillis() - stillSince;
        if (standTime < STAND_TIME_REQUIRED) return 0;
        // Bonus increases with time standing still, capped at 3 seconds
        double timeBonus = Math.min((standTime - STAND_TIME_REQUIRED) / 1500.0, 2.0);
        return getConditionalBonus(skillLevel) * (1 + timeBonus);
    }

    /**
     * Simulates the player standing still for testing purposes.
     * Sets the standingStillSince timestamp to the given duration ago.
     */
    public static void simulateStationary(UUID uuid, long durationMs) {
        standingStillSince.put(uuid, System.currentTimeMillis() - durationMs);
    }

    @Override
    public TestStrategy getTestStrategy() {
        return TestStrategy.CONDITION_SETUP;
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        standingStillSince.remove(player.getUniqueId());
    }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        standingStillSince.remove(player.getUniqueId());
    }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("value", String.format("%.1f", getConditionalBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getConditionalBonus(skillLevel) * 3; }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getConditionalBonus(skillLevel) * 100))); }
    @Override
    public void shutdown() {
        activePlayers.clear();
        standingStillSince.clear();
    }
}
