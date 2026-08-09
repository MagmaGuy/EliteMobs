package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
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
    /**
     * There is exactly one stack cap. The skill used to carry two - a no-arg {@code getMaxStacks()}
     * returning 5 and a level-scaled {@code getMaxStacks(int)} returning 5 + level/20 - with
     * {@code addStack} enforcing the scaled one while the action bar printed the flat one, so a
     * level 50 player watched their bar read "7/5". The cap is now flat and the per-stack value was
     * re-priced so a full bar is still worth the same +40% it was worth at 7 stacks.
     */
    private static final int MAX_STACKS = 5;
    private static final double BASE_STACK_BONUS = 0.03; // 3% per stack
    /**
     * Stacks fall off after 2.5s without a hit. The old 5s window never lapsed at any realistic
     * mace swing cadence, so the bar stayed pinned at max and the skill behaved as a flat
     * passive rather than the ramp its per-stack value is priced for.
     */
    private static final long STACK_DECAY_MS = 2500;

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
        return MAX_STACKS;
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        // Power budget: ~50% uptime on the ramp, so a full bar (5 stacks) is worth +40% at level
        // 50 (E = 0.50 * 0.40 = 0.20). The curve also still lands +20% at the level 10 unlock,
        // matching what the old 5-stack/3.95%-per-stack pairing paid there.
        return scaled(BASE_STACK_BONUS, 0.001, skillLevel);
    }

    @Override
    public int getCurrentStacks(Player player) {
        checkStackDecay(player);
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int current = getCurrentStacks(player);

        if (current < getMaxStacks()) {
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
                "maxStacks", String.valueOf(getMaxStacks()),
                "damagePerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel) * getMaxStacks();
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "maxDamage", String.format("%.0f", getBonusValue(skillLevel) * 100),
                "maxStacks", String.valueOf(getMaxStacks())
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        playerStacks.clear();
        lastHitTime.clear();
    }
}
