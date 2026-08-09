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
 * Legion's Discipline (STACKING) - Maintaining an aggressive combat pace builds damage; letting
 * more than 2.5 seconds pass without landing a hit breaks the rhythm.
 * Tier 3 unlock.
 */
public class LegionsDisciplineSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "spears_legions_discipline";
    private static final int BASE_MAX_STACKS = 10;
    private static final double BASE_STACK_BONUS = 0.025; // 2.5% per stack
    /**
     * The pace window makes the bonus intermittent and prevents stacks carrying between fights.
     * It is also the uptime assumption used by the per-stack power budget below.
     */
    private static final long STACK_DECAY_MS = 2500;

    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public LegionsDisciplineSkill() {
        super(SkillType.SPEARS, 50, "Legion's Discipline",
              "Keep landing hits to build damage; lose the rhythm for 2.5s and the stacks fade.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return BASE_MAX_STACKS;
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        // Power budget: ~50% uptime on the ramp, so a full 10 stack bar is worth +40% at
        // the level 100 soft cap (E = 0.50 * 0.40 = 0.20).
        double approvedCurve = scaled(BASE_STACK_BONUS, 0.00015, skillLevel);
        if (configFields == null) return approvedCurve;
        double configuredBonus = configFields.getStackBonus()
                + (skillLevel * configFields.getScalingPerLevel());
        if (!Double.isFinite(configuredBonus)) return approvedCurve;
        return Math.max(0D, Math.min(configuredBonus, approvedCurve));
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
     * Drops the stack bar when the player stops maintaining the combat pace.
     */
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
