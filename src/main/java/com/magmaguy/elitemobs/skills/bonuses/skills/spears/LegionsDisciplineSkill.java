package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legion's Discipline (STACKING) - Consecutive hits increase damage. Stacks fade after 2.5s without a hit.
 * Tier 3 unlock.
 */
public class LegionsDisciplineSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "spears_legions_discipline";
    private static final int BASE_MAX_STACKS = 10;
    private static final double BASE_STACK_BONUS = 0.025; // 2.5% per stack
    /**
     * Discipline breaks after 2.5s without landing a hit.
     * <p>
     * The skill was written to reset on a miss, but the miss hook had no callers anywhere in
     * the plugin, so stacks were never cleared: a player ramped to the cap once and
     * kept the full bonus permanently, including across fights. This timeout is what actually
     * makes the bonus intermittent, and it is what the per-stack value above is priced for.
     */
    private static final long STACK_DECAY_MS = 2500;

    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public LegionsDisciplineSkill() {
        super(SkillType.SPEARS, 50, "Legion's Discipline",
              "Consecutive hits increase damage. Stacks fade after 2.5s without a hit.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return BASE_MAX_STACKS;
    }

    /**
     * Compatibility overload retained for callers that select a stack cap by skill level.
     */
    public int getMaxStacks(int skillLevel) {
        return getMaxStacks();
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        // Power budget: ~50% uptime on the ramp, so a full 10 stack bar is worth +40% at
        // level 50 (E = 0.50 * 0.40 = 0.20).
        return scaled(BASE_STACK_BONUS, 0.0003, skillLevel);
    }

    @Override
    public int getCurrentStacks(Player player) {
        checkStackDecay(player);
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int maxStacks = getMaxStacks();
        int current = getCurrentStacks(player);

        if (current < maxStacks) {
            playerStacks.put(player.getUniqueId(), current + 1);
            // Small visual effect for stacks
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
        }
        lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void resetStacks(Player player) {
        int current = playerStacks.getOrDefault(player.getUniqueId(), 0);
        lastHitTime.remove(player.getUniqueId());
        if (current > 0) {
            playerStacks.remove(player.getUniqueId());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(DungeonsConfig.getLegionsDisciplineBrokenMessage()));
        }
    }

    /**
     * Drops the whole stack bar once the player has gone {@link #STACK_DECAY_MS} without
     * landing a hit.
     */
    private void checkStackDecay(Player player) {
        Long last = lastHitTime.get(player.getUniqueId());
        if (last != null && System.currentTimeMillis() - last > STACK_DECAY_MS) {
            resetStacks(player);
        }
    }

    /**
     * Explicitly breaks discipline after a missed attack. The built-in dispatcher currently uses
     * inactivity decay because it has no miss event, but this hook remains part of the skill's
     * behavior for integrations able to identify a miss.
     */
    public void onMiss(Player player) {
        if (isActive(player)) resetStacks(player);
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
        playerStacks.remove(player.getUniqueId());
        lastHitTime.remove(player.getUniqueId());
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
                "perStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxBonus", String.format("%.0f", getMaxStacks() * getBonusPerStack(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel) * getMaxStacks();
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "maxBonus", String.format("%.0f", getBonusValue(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        playerStacks.clear();
        lastHitTime.clear();
    }
}
