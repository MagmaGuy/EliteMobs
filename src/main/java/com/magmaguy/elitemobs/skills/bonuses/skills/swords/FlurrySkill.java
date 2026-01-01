package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Flurry (STACKING) - Consecutive hits increase attack speed.
 * Stacks decay after a short period of not attacking.
 * Tier 2 unlock.
 */
public class FlurrySkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "swords_flurry";
    private static final int MAX_STACKS = 5;
    private static final double ATTACK_SPEED_PER_STACK = 0.05; // 5% per stack
    private static final int STACK_DECAY_TICKS = 60; // 3 seconds

    private static final Map<UUID, Integer> playerStacks = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> decayTasks = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public FlurrySkill() {
        super(SkillType.SWORDS, 25, "Flurry",
              "Consecutive hits increase your attack speed. Stacks decay over time.",
              SkillBonusType.STACKING, 2, SKILL_ID);
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
        UUID uuid = player.getUniqueId();
        int current = playerStacks.getOrDefault(uuid, 0);
        if (current < MAX_STACKS) {
            playerStacks.put(uuid, current + 1);
        }
        resetDecayTimer(player);
    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        cancelDecayTimer(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        // Base 5% per stack + 0.1% per level
        return ATTACK_SPEED_PER_STACK + (skillLevel * 0.001);
    }

    /**
     * Gets the total attack speed bonus for a player.
     */
    public static double getAttackSpeedBonus(Player player, int skillLevel) {
        if (!activePlayers.contains(player.getUniqueId())) return 0;
        int stacks = playerStacks.getOrDefault(player.getUniqueId(), 0);
        double bonusPerStack = ATTACK_SPEED_PER_STACK + (skillLevel * 0.001);
        return stacks * bonusPerStack;
    }

    /**
     * Checks if a player has this skill active.
     */
    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    private void resetDecayTimer(Player player) {
        UUID uuid = player.getUniqueId();
        cancelDecayTimer(uuid);

        BukkitRunnable decayTask = new BukkitRunnable() {
            @Override
            public void run() {
                int current = playerStacks.getOrDefault(uuid, 0);
                if (current > 0) {
                    playerStacks.put(uuid, current - 1);
                    if (current - 1 > 0) {
                        // Schedule next decay
                        resetDecayTimer(player);
                    } else {
                        decayTasks.remove(uuid);
                    }
                }
            }
        };
        decayTask.runTaskLater(MetadataHandler.PLUGIN, STACK_DECAY_TICKS);
        decayTasks.put(uuid, decayTask);
    }

    private void cancelDecayTimer(UUID uuid) {
        BukkitRunnable existing = decayTasks.remove(uuid);
        if (existing != null) {
            existing.cancel();
        }
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        playerStacks.remove(uuid);
        cancelDecayTimer(uuid);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonusPerStack = getBonusPerStack(skillLevel) * 100;
        return List.of(
                "&7Max Stacks: &f" + MAX_STACKS,
                "&7Attack Speed per Stack: &f+" + String.format("%.1f", bonusPerStack) + "%",
                "&7Max Bonus: &f+" + String.format("%.1f", bonusPerStack * MAX_STACKS) + "%",
                "&7Stacks decay after 3 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel) * MAX_STACKS;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Attack Speed (max)", getBonusPerStack(skillLevel) * MAX_STACKS * 100);
    }

    @Override
    public void shutdown() {
        for (BukkitRunnable task : decayTasks.values()) {
            task.cancel();
        }
        decayTasks.clear();
        playerStacks.clear();
        activePlayers.clear();
    }
}
