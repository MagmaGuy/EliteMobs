package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

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
 * Overdraw (CONDITIONAL) - Bonus damage for holding bow longer.
 * Tier 2 unlock.
 */
public class OverdrawSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "bows_overdraw";
    private static final long FULL_DRAW_TIME = 1500; // 1.5 seconds for full draw
    private static final double BASE_BONUS = 0.15; // 15% base bonus

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> drawStartTimes = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> snapshotDrawDurations = new ConcurrentHashMap<>();

    public OverdrawSkill() {
        super(SkillType.BOWS, 25, "Overdraw",
              "Holding your bow longer increases damage.",
              SkillBonusType.CONDITIONAL, 2, SKILL_ID);
    }

    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    public static void startDrawing(UUID uuid) {
        drawStartTimes.put(uuid, System.currentTimeMillis());
        snapshotDrawDurations.remove(uuid);
    }

    /**
     * Called on bow shoot to snapshot the draw duration before the draw start is cleared.
     * The snapshot persists until the arrow hits and the damage event reads it.
     */
    public static void snapshotDrawDuration(UUID uuid) {
        Long startTime = drawStartTimes.remove(uuid);
        if (startTime != null) {
            snapshotDrawDurations.put(uuid, System.currentTimeMillis() - startTime);
        }
    }

    public static void stopDrawing(UUID uuid) {
        drawStartTimes.remove(uuid);
        snapshotDrawDurations.remove(uuid);
    }

    public static long getDrawTime(UUID uuid) {
        // First check snapshot (set at bow release, used at arrow impact)
        Long snapshot = snapshotDrawDurations.get(uuid);
        if (snapshot != null) return snapshot;
        // Fallback: still drawing
        Long startTime = drawStartTimes.get(uuid);
        if (startTime == null) return 0;
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        return getDrawTime(player.getUniqueId()) >= FULL_DRAW_TIME;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return BASE_BONUS + (skillLevel * 0.003); // 15% base + 0.3% per level
    }

    public double calculateBonus(Player player, int skillLevel) {
        long drawTime = getDrawTime(player.getUniqueId());
        if (drawTime < FULL_DRAW_TIME) return 0;
        // Bonus scales with overdraw time, capped at 2x base bonus
        double overdrawMultiplier = Math.min((drawTime - FULL_DRAW_TIME) / 1000.0, 2.0);
        return getConditionalBonus(skillLevel) * (1 + overdrawMultiplier);
    }

    /**
     * Simulates a full bow draw for testing purposes.
     * Backdates the draw start time so the skill thinks the bow was drawn for 2 seconds.
     */
    public static void simulateFullDraw(UUID uuid) {
        // Set draw start to 2 seconds ago (exceeds FULL_DRAW_TIME of 1000ms)
        drawStartTimes.put(uuid, System.currentTimeMillis() - 2000);
        snapshotDrawDuration(uuid);
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
        drawStartTimes.remove(player.getUniqueId());
    }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        drawStartTimes.remove(player.getUniqueId());
    }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "baseBonus", String.format("%.1f", getConditionalBonus(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getConditionalBonus(skillLevel) * 3; }
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "baseBonus", String.format("%.1f", getConditionalBonus(skillLevel) * 100)
        ));
    }
    @Override
    public void shutdown() {
        activePlayers.clear();
        drawStartTimes.clear();
        snapshotDrawDurations.clear();
    }
}
