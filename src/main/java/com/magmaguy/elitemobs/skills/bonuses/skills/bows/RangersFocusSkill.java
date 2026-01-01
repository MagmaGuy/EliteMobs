package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Ranger's Focus (STACKING) - Focus on a single target for increasing damage.
 * Tier 4 unlock.
 */
public class RangersFocusSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "bows_rangers_focus";
    private static final int MAX_STACKS = 8;
    private static final double BASE_BONUS_PER_STACK = 0.04; // 4% per stack

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Map<UUID, Integer> playerStacks = new HashMap<>();
    private static final Map<UUID, UUID> targetedEnemy = new HashMap<>();

    public RangersFocusSkill() {
        super(SkillType.BOWS, 75, "Ranger's Focus",
              "Focusing on a single target increases your damage.",
              SkillBonusType.STACKING, 4, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return MAX_STACKS;
    }

    @Override
    public int getCurrentStacks(Player player) {
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int current = getCurrentStacks(player);
        if (current < MAX_STACKS) {
            playerStacks.put(player.getUniqueId(), current + 1);
        }
    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        targetedEnemy.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return BASE_BONUS_PER_STACK + (skillLevel * 0.0008); // 4% base + 0.08% per level
    }

    public UUID getTargetedEnemy(Player player) {
        return targetedEnemy.get(player.getUniqueId());
    }

    public void setTargetedEnemy(Player player, UUID targetUUID) {
        UUID currentTarget = targetedEnemy.get(player.getUniqueId());
        if (currentTarget != null && !currentTarget.equals(targetUUID)) {
            // Switched targets, reset stacks
            resetStacks(player);
        }
        targetedEnemy.put(player.getUniqueId(), targetUUID);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        resetStacks(player);
    }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        resetStacks(player);
    }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&7Bonus per Stack: &f" + String.format("%.1f", getBonusPerStack(skillLevel) * 100) + "%",
                "&7Max Stacks: &f" + MAX_STACKS,
                "&7Stacks reset when switching targets"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getBonusPerStack(skillLevel) * MAX_STACKS; }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.1f%% per stack (max %d)", getBonusPerStack(skillLevel) * 100, MAX_STACKS); }
    @Override
    public void shutdown() {
        activePlayers.clear();
        playerStacks.clear();
        targetedEnemy.clear();
    }
}
