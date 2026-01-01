package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Tier 1 ARMOR skill - Battle Hardened
 * Provides passive damage reduction
 */
public class BattleHardenedSkill extends SkillBonus {

    private static final HashSet<UUID> activePlayers = new HashSet<>();

    public BattleHardenedSkill() {
        super(
            SkillType.ARMOR,
            10,
            "Battle Hardened",
            "Passive damage reduction from all sources",
            SkillBonusType.PASSIVE,
            1,
            "battle_hardened"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        // Passive bonus, no active application needed
        // Damage reduction is calculated on damage events
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
        List<String> lore = new ArrayList<>();
        lore.add("Passive damage reduction from all sources");
        lore.add(String.format("Damage Reduction: %.1f%%", getBonusValue(skillLevel) * 15.0));
        return lore;
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getScaledValue(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% Damage Reduction", getBonusValue(skillLevel) * 15.0);
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
        // Reduce damage by up to 15% based on skill level
        return originalDamage * (1 - getScaledValue(skillLevel) * 0.15);
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
