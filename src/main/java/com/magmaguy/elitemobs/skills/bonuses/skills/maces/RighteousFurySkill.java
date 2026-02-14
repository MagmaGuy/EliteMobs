package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Righteous Fury (STACKING) - Build stacks on hit, increasing damage.
 * Tier 1 unlock.
 */
public class RighteousFurySkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "maces_righteous_fury";
    private static final int BASE_MAX_STACKS = 5;
    private static final double BASE_STACK_BONUS = 0.05; // 5% per stack
    private static final long STACK_DECAY_MS = 5000; // 5 seconds

    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public RighteousFurySkill() {
        super(SkillType.MACES, 10, "Righteous Fury",
              "Build fury stacks on hit, increasing your damage.",
              SkillBonusType.STACKING, 1, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return BASE_MAX_STACKS;
    }

    public int getMaxStacks(int skillLevel) {
        return BASE_MAX_STACKS + (skillLevel / 20);
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return BASE_STACK_BONUS + (skillLevel * 0.001);
    }

    @Override
    public int getCurrentStacks(Player player) {
        checkStackDecay(player);
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
        int maxStacks = getMaxStacks(skillLevel);
        int current = getCurrentStacks(player);

        if (current < maxStacks) {
            playerStacks.put(player.getUniqueId(), current + 1);
            // Visual effect
            player.getWorld().spawnParticle(Particle.FLAME,
                player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.02);
        }
        lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());

    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        lastHitTime.remove(player.getUniqueId());
    }

    private void checkStackDecay(Player player) {
        Long last = lastHitTime.get(player.getUniqueId());
        if (last != null && System.currentTimeMillis() - last > STACK_DECAY_MS) {
            resetStacks(player);
        }
    }

    public double getDamageMultiplier(Player player, int skillLevel) {
        int stacks = getCurrentStacks(player);
        return 1.0 + (stacks * getBonusPerStack(skillLevel));
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
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "maxStacks", String.valueOf(getMaxStacks(skillLevel)),
                "damagePerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel) * getMaxStacks(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "maxDamage", String.format("%.0f", getBonusValue(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        playerStacks.clear();
        lastHitTime.clear();
    }
}
