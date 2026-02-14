package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wind Runner (PASSIVE) - Gain speed boost on successful hits.
 * Tier 3 unlock.
 */
public class WindRunnerSkill extends SkillBonus {

    public static final String SKILL_ID = "bows_wind_runner";
    private static final int SPEED_DURATION = 100; // 5 seconds

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public WindRunnerSkill() {
        super(SkillType.BOWS, 50, "Wind Runner",
              "Successful hits grant you a speed boost.",
              SkillBonusType.PASSIVE, 3, SKILL_ID);
    }

    public void applySpeedBoost(Player player) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
        int amplifier = Math.min(2, skillLevel / 30); // 0-2 based on level
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_DURATION, amplifier));
    }

    private int getSpeedAmplifier(int skillLevel) {
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
        return applyLoreTemplates(Map.of(
                "speedLevel", String.valueOf(getSpeedAmplifier(skillLevel) + 1)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getSpeedAmplifier(skillLevel) + 1; }
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "speedLevel", String.valueOf(getSpeedAmplifier(skillLevel) + 1)
        ));
    }
    @Override
    public boolean affectsDamage() { return false; } // Speed boost doesn't affect damage
    @Override
    public void shutdown() { activePlayers.clear(); }
}
