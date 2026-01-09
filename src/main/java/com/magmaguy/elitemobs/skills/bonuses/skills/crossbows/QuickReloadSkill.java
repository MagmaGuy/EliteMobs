package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Quick Reload (PASSIVE) - Gain haste on hit for faster reloading.
 * Tier 1 unlock.
 */
public class QuickReloadSkill extends SkillBonus {

    public static final String SKILL_ID = "crossbows_quick_reload";
    private static final int HASTE_DURATION = 60; // 3 seconds

    private static final Set<UUID> activePlayers = new HashSet<>();

    public QuickReloadSkill() {
        super(SkillType.CROSSBOWS, 10, "Quick Reload",
              "Successful hits grant haste for faster reloading.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    public void applyHaste(Player player) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        int amplifier = Math.min(2, skillLevel / 30); // 0-2 based on level
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, HASTE_DURATION, amplifier));
    }

    private int getHasteAmplifier(int skillLevel) {
        return Math.min(2, skillLevel / 30);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&7Haste Level: &f" + (getHasteAmplifier(skillLevel) + 1),
                "&7Duration: &f3 seconds",
                "&7Triggers on hit"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getHasteAmplifier(skillLevel) + 1; }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("Haste %d on hit", getHasteAmplifier(skillLevel) + 1); }
    @Override
    public boolean affectsDamage() { return false; } // Haste for reloading doesn't affect damage
    @Override
    public void shutdown() { activePlayers.clear(); }
}
