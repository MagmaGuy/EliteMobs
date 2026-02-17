package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
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
 * Legion's Discipline (STACKING) - Consecutive hits increase damage. Resets on miss.
 * Tier 3 unlock.
 */
public class LegionsDisciplineSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "spears_legions_discipline";
    private static final int BASE_MAX_STACKS = 10;
    private static final double BASE_STACK_BONUS = 0.03; // 3% per stack

    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public LegionsDisciplineSkill() {
        super(SkillType.SPEARS, 50, "Legion's Discipline",
              "Consecutive hits increase damage. Resets on miss.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return BASE_MAX_STACKS;
    }

    public int getMaxStacks(int skillLevel) {
        return BASE_MAX_STACKS;
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return BASE_STACK_BONUS + (skillLevel * 0.0005);
    }

    @Override
    public int getCurrentStacks(Player player) {
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        int maxStacks = getMaxStacks(skillLevel);
        int current = getCurrentStacks(player);

        if (current < maxStacks) {
            playerStacks.put(player.getUniqueId(), current + 1);
            // Small visual effect for stacks
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
        }

    }

    @Override
    public void resetStacks(Player player) {
        int current = getCurrentStacks(player);
        if (current > 0) {
            playerStacks.remove(player.getUniqueId());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(DungeonsConfig.getLegionsDisciplineBrokenMessage()));
        }
    }

    /**
     * Called when an attack misses or the player takes damage.
     * Resets the discipline stacks.
     */
    public void onMiss(Player player) {
        if (isActive(player)) {
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
                "perStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxBonus", String.format("%.0f", getMaxStacks(skillLevel) * getBonusPerStack(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel) * getMaxStacks(skillLevel);
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
    }
}
