package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusEventHandler;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ToggleSkill;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reckless Fury (TOGGLE) - Deal more damage but take more damage.
 * Tier 3 unlock.
 */
public class RecklessFurySkill extends SkillBonus implements ToggleSkill {

    public static final String SKILL_ID = "axes_reckless_fury";
    private static final double BASE_DAMAGE_BONUS = 0.30;
    private static final double BASE_DAMAGE_TAKEN = 0.25;

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Set<UUID> toggledPlayers = new HashSet<>();

    public RecklessFurySkill() {
        super(SkillType.AXES, 50, "Reckless Fury",
              "Toggle: Deal and take more damage.",
              SkillBonusType.TOGGLE, 3, SKILL_ID);
    }

    @Override
    public void toggle(Player player) {
        if (toggledPlayers.contains(player.getUniqueId())) disable(player);
        else enable(player);
    }

    @Override
    public boolean isToggled(Player player) { return toggledPlayers.contains(player.getUniqueId()); }

    @Override
    public void enable(Player player) {
        toggledPlayers.add(player.getUniqueId());
        SkillBonusEventHandler.setToggle(player.getUniqueId(), SKILL_ID, true);
        player.sendMessage("\u00A7c\u00A7lReckless Fury \u00A7aenabled");
    }

    @Override
    public void disable(Player player) {
        toggledPlayers.remove(player.getUniqueId());
        SkillBonusEventHandler.setToggle(player.getUniqueId(), SKILL_ID, false);
        player.sendMessage("\u00A7c\u00A7lReckless Fury \u00A7cdisabled");
    }

    @Override
    public double getPositiveBonus(int skillLevel) { return BASE_DAMAGE_BONUS + (skillLevel * 0.004); }
    @Override
    public double getNegativeEffect(int skillLevel) { return Math.max(0.10, BASE_DAMAGE_TAKEN - (skillLevel * 0.002)); }

    public static double getDamageMultiplier(Player player, int skillLevel) {
        if (!toggledPlayers.contains(player.getUniqueId())) return 1.0;
        return 1.0 + BASE_DAMAGE_BONUS + (skillLevel * 0.004);
    }

    public static double getDamageTakenMultiplier(Player player, int skillLevel) {
        if (!toggledPlayers.contains(player.getUniqueId())) return 1.0;
        return 1.0 + Math.max(0.10, BASE_DAMAGE_TAKEN - (skillLevel * 0.002));
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); toggledPlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { removeBonus(player); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&aBonus Damage: &f+" + String.format("%.0f", getPositiveBonus(skillLevel) * 100) + "%",
                "&cExtra Damage Taken: &f+" + String.format("%.0f", getNegativeEffect(skillLevel) * 100) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getPositiveBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.0f%% Damage (toggle)", getPositiveBonus(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); toggledPlayers.clear(); }
}
