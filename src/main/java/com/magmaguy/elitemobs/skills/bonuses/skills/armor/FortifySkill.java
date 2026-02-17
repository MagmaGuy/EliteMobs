package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 3 ARMOR skill - Fortify
 * Builds damage reduction stacks when taking damage
 */
public class FortifySkill extends SkillBonus implements StackingSkill {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> stackMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
    private static final long STACK_DECAY_TIME = 10000; // 10 seconds

    public FortifySkill() {
        super(
            SkillType.ARMOR,
            50,
            "Fortify",
            "Build damage reduction stacks when taking damage",
            SkillBonusType.STACKING,
            3,
            "armor_fortify"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        // No persistent bonus to remove
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        stackMap.remove(player.getUniqueId());
        lastDamageTime.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "maxStacks", String.valueOf(getMaxStacks()),
                "reductionPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "reductionPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxStacks", String.valueOf(getMaxStacks())));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        stackMap.clear();
        lastDamageTime.clear();
    }

    // StackingSkill interface methods

    @Override
    public int getMaxStacks() {
        return 5;
    }

    @Override
    public int getCurrentStacks(Player player) {
        // Check if stacks have decayed
        Long lastDamage = lastDamageTime.get(player.getUniqueId());
        if (lastDamage != null && System.currentTimeMillis() - lastDamage > STACK_DECAY_TIME) {
            resetStacks(player);
        }

        return stackMap.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int current = stackMap.getOrDefault(player.getUniqueId(), 0);
        if (current < getMaxStacks()) {
            stackMap.put(player.getUniqueId(), current + 1);
        }
        lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void resetStacks(Player player) {
        stackMap.remove(player.getUniqueId());
        lastDamageTime.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        // Base 5% damage reduction per stack
        return 0.05 * getScaledValue(skillLevel);
    }

    /**
     * Calculates damage reduction and adds a stack.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param originalDamage The original damage amount
     * @return The modified damage amount
     */
    public double modifyIncomingDamage(Player player, double originalDamage) {
        int skillLevel = getPlayerSkillLevel(player);
        int currentStacks = getCurrentStacks(player);

        // Calculate damage reduction from current stacks
        double reduction = currentStacks * getBonusPerStack(skillLevel);
        double modifiedDamage = originalDamage * (1 - reduction);

        // Add a stack after calculating damage
        addStack(player);

        // Always show action bar with current stack count (after adding)
        sendStackingSkillActionBar(player, this, getCurrentStacks(player), getMaxStacks());

        return modifiedDamage;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
