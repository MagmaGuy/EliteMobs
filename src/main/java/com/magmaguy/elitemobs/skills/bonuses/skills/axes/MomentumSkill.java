package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Momentum (STACKING) - Build up damage with consecutive hits.
 * Tier 3 unlock.
 */
public class MomentumSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "axes_momentum";
    private static final int MAX_STACKS = 8;
    private static final double DAMAGE_PER_STACK = 0.04;
    private static final int DECAY_TICKS = 80;

    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitRunnable> decayTasks = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public MomentumSkill() {
        super(SkillType.AXES, 50, "Momentum",
              "Build up damage with each consecutive hit.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() { return MAX_STACKS; }

    @Override
    public int getCurrentStacks(Player player) {
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        UUID uuid = player.getUniqueId();
        int current = playerStacks.getOrDefault(uuid, 0);
        if (current < MAX_STACKS) playerStacks.put(uuid, current + 1);
        resetDecayTimer(player);
    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        cancelDecayTimer(player.getUniqueId());
    }

    public static double getDamageMultiplier(Player player, int skillLevel) {
        if (!activePlayers.contains(player.getUniqueId())) return 1.0;
        int stacks = playerStacks.getOrDefault(player.getUniqueId(), 0);
        double bonus = Math.min(0.10, DAMAGE_PER_STACK + (skillLevel * 0.0005)) * stacks;
        return 1.0 + bonus;
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return Math.min(0.10, DAMAGE_PER_STACK + (skillLevel * 0.0005));
    }

    private void resetDecayTimer(Player player) {
        UUID uuid = player.getUniqueId();
        cancelDecayTimer(uuid);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int current = playerStacks.getOrDefault(uuid, 0);
                if (current > 0) {
                    playerStacks.put(uuid, current - 1);
                    if (current - 1 > 0) resetDecayTimer(player);
                    else decayTasks.remove(uuid);
                }
            }
        };
        task.runTaskLater(MetadataHandler.PLUGIN, DECAY_TICKS);
        decayTasks.put(uuid, task);
    }

    private void cancelDecayTimer(UUID uuid) {
        BukkitRunnable task = decayTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); resetStacks(player); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { removeBonus(player); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonusPerStack = getBonusPerStack(skillLevel) * 100;
        return applyLoreTemplates(Map.of(
                "maxStacks", String.valueOf(MAX_STACKS),
                "bonusPerStack", String.format("%.1f", bonusPerStack),
                "maxBonus", String.format("%.1f", bonusPerStack * MAX_STACKS)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getBonusPerStack(skillLevel) * MAX_STACKS; }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("maxBonus", String.format("%.0f", getBonusValue(skillLevel) * 100))); }

    @Override
    public void shutdown() {
        for (BukkitRunnable task : decayTasks.values()) task.cancel();
        decayTasks.clear(); playerStacks.clear(); activePlayers.clear();
    }
}
