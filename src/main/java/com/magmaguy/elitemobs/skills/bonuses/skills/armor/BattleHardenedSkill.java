package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 1 ARMOR skill - Battle Hardened
 * Provides passive damage reduction
 */
public class BattleHardenedSkill extends SkillBonus {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public BattleHardenedSkill() {
        super(
            SkillType.ARMOR,
            10,
            "Battle Hardened",
            "Passive damage reduction from all sources",
            SkillBonusType.PASSIVE,
            1,
            "armor_battle_hardened"
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
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("value", String.format("%.1f", getBonusValue(skillLevel) * 8.0)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getScaledValue(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getBonusValue(skillLevel) * 8.0)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }

    /**
     * Calculates damage reduction for incoming damage.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param originalDamage The original damage amount
     * @return The modified damage amount
     */
    public double modifyIncomingDamage(Player player, double originalDamage) {
        if (!isActive(player)) {
            return originalDamage;
        }

        int skillLevel = getPlayerSkillLevel(player);
        // Reduce damage by up to 8% based on skill level
        return originalDamage * (1 - getScaledValue(skillLevel) * 0.08);
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
