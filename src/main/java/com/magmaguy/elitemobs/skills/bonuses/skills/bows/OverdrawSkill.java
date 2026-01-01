package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Overdraw (CONDITIONAL) - Bonus damage for holding bow longer.
 * Tier 2 unlock.
 */
public class OverdrawSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "bows_overdraw";
    private static final long FULL_DRAW_TIME = 1000; // 1 second for full draw
    private static final double BASE_BONUS = 0.15; // 15% base bonus

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Map<UUID, Long> drawStartTimes = new HashMap<>();

    public OverdrawSkill() {
        super(SkillType.BOWS, 25, "Overdraw",
              "Holding your bow longer increases damage.",
              SkillBonusType.CONDITIONAL, 2, SKILL_ID);
    }

    public static void startDrawing(UUID uuid) {
        drawStartTimes.put(uuid, System.currentTimeMillis());
    }

    public static void stopDrawing(UUID uuid) {
        drawStartTimes.remove(uuid);
    }

    public static long getDrawTime(UUID uuid) {
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
        return List.of(
                "&7Base Bonus: &f" + String.format("%.1f", getConditionalBonus(skillLevel) * 100) + "%",
                "&7Max Overdraw: &f3x bonus",
                "&7Requires full draw"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getConditionalBonus(skillLevel) * 3; }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.1f%% damage (overdraw)", getConditionalBonus(skillLevel) * 100); }
    @Override
    public void shutdown() {
        activePlayers.clear();
        drawStartTimes.clear();
    }
}
