package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Returning Haste (STACKING) - Build stacks with consecutive trident throws.
 * Each stack increases damage. Stacks decay after 5 seconds of no throws.
 * Tier 3 unlock.
 */
public class ReturningHasteSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "tridents_returning_haste";
    private static final int BASE_MAX_STACKS = 5;
    private static final long STACK_DECAY_TIME = 5000; // 5 seconds
    private static final double BASE_BONUS_PER_STACK = 0.1;

    // Track player stacks: PlayerUUID -> Stack count
    private static final Map<UUID, Integer> playerStacks = new HashMap<>();
    // Track last throw time: PlayerUUID -> Timestamp
    private static final Map<UUID, Long> lastThrowTime = new HashMap<>();
    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public ReturningHasteSkill() {
        super(SkillType.TRIDENTS, 50, "Returning Haste",
              "Build stacks with consecutive trident throws. Each stack increases damage.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        if (configFields != null && configFields.getMaxStacks() > 0) {
            return configFields.getMaxStacks();
        }
        return BASE_MAX_STACKS;
    }

    @Override
    public int getCurrentStacks(Player player) {
        Long lastThrow = lastThrowTime.get(player.getUniqueId());
        if (lastThrow != null && System.currentTimeMillis() - lastThrow > STACK_DECAY_TIME) {
            resetStacks(player);
        }
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int current = getCurrentStacks(player);
        if (current < getMaxStacks()) {
            playerStacks.put(player.getUniqueId(), current + 1);
        }
        lastThrowTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        lastThrowTime.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        if (configFields != null) {
            return BASE_BONUS_PER_STACK + (configFields.calculateValue(skillLevel) * 0.01);
        }
        return BASE_BONUS_PER_STACK + (skillLevel * 0.002);
    }

    @Override
    public double getTotalStackBonus(Player player, int skillLevel) {
        return getCurrentStacks(player) * getBonusPerStack(skillLevel);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        resetStacks(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        resetStacks(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonusPerStack = getBonusPerStack(skillLevel) * 100;
        double maxBonus = bonusPerStack * getMaxStacks();
        return List.of(
                "&7Max Stacks: &f" + getMaxStacks(),
                "&7Per Stack: &f+" + String.format("%.1f", bonusPerStack) + "%",
                "&7Max Bonus: &f+" + String.format("%.1f", maxBonus) + "%",
                "&7Decay: &f5 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% per stack", getBonusPerStack(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        playerStacks.clear();
        lastThrowTime.clear();
        activePlayers.clear();
    }
}
