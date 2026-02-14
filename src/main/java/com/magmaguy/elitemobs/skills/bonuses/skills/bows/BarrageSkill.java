package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Barrage (STACKING) - Consecutive hits increase damage.
 * Tier 3 unlock.
 */
public class BarrageSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "bows_barrage";
    private static final int MAX_STACKS = 5;
    private static final long STACK_DECAY_TIME = 3000; // 3 seconds
    private static final double BASE_BONUS_PER_STACK = 0.05; // 5% per stack

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();

    public BarrageSkill() {
        super(SkillType.BOWS, 50, "Barrage",
              "Consecutive hits increase your damage.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return MAX_STACKS;
    }

    @Override
    public int getCurrentStacks(Player player) {
        Long lastHit = lastHitTime.get(player.getUniqueId());
        if (lastHit != null && System.currentTimeMillis() - lastHit > STACK_DECAY_TIME) {
            resetStacks(player);
        }
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int current = getCurrentStacks(player);
        if (current < MAX_STACKS) {
            playerStacks.put(player.getUniqueId(), current + 1);
        }
        lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        lastHitTime.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return BASE_BONUS_PER_STACK + (skillLevel * 0.001); // 5% base + 0.1% per level
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
        return applyLoreTemplates(Map.of(
                "bonusPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxStacks", String.valueOf(MAX_STACKS)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getBonusPerStack(skillLevel) * MAX_STACKS; }
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonusPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxStacks", String.valueOf(MAX_STACKS)
        ));
    }
    @Override
    public void shutdown() {
        activePlayers.clear();
        playerStacks.clear();
        lastHitTime.clear();
    }
}
