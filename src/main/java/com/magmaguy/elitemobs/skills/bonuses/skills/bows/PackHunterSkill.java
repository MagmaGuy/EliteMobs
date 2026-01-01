package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Pack Hunter (CONDITIONAL) - Bonus damage when allies are nearby.
 * Tier 1 unlock.
 */
public class PackHunterSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "bows_pack_hunter";
    private static final double ALLY_RANGE = 10.0;
    private static final double BASE_BONUS_PER_ALLY = 0.08; // 8% per ally

    private static final Set<UUID> activePlayers = new HashSet<>();

    public PackHunterSkill() {
        super(SkillType.BOWS, 10, "Pack Hunter",
              "Deal bonus damage for each nearby ally.",
              SkillBonusType.CONDITIONAL, 1, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        long nearbyPlayers = player.getNearbyEntities(ALLY_RANGE, ALLY_RANGE, ALLY_RANGE).stream()
                .filter(e -> e instanceof Player && !e.equals(player))
                .count();
        return nearbyPlayers >= 1;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return BASE_BONUS_PER_ALLY + (skillLevel * 0.001); // 8% base + 0.1% per level
    }

    public double calculateBonus(Player player, int skillLevel) {
        long nearbyPlayers = player.getNearbyEntities(ALLY_RANGE, ALLY_RANGE, ALLY_RANGE).stream()
                .filter(e -> e instanceof Player && !e.equals(player))
                .count();
        // Bonus per nearby ally, capped at 3 allies
        return getConditionalBonus(skillLevel) * Math.min(nearbyPlayers, 3);
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
                "&7Bonus per Ally: &f" + String.format("%.1f", getConditionalBonus(skillLevel) * 100) + "%",
                "&7Max Allies: &f3",
                "&7Range: &f" + (int)ALLY_RANGE + " blocks"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getConditionalBonus(skillLevel) * 3; }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.1f%% damage per ally", getConditionalBonus(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
