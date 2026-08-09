package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
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

    /**
     * Flat damage reduction, on the shared defensive power budget.
     * <p>
     * Sustained power is {@code E = uptime * reduction} and the budget is 0.20. Battle Hardened is a
     * true passive with no condition and no ramp, so uptime is 1.0 and the fair reduction is
     * {@code 0.20 / 1.0}. Hardcoded on purpose: balance values no longer come from config, only
     * presentation (name, lore, proc message) does.
     */
    private static final double DAMAGE_REDUCTION = 0.20;

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
        return applyLoreTemplates(Map.of("value", String.format("%.1f", getBonusValue(skillLevel) * 100)));
    }

    /**
     * The passive damage reduction applied to incoming damage.
     * <p>
     * This is the single source of truth for the skill: the generic PASSIVE branch in
     * {@link com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent} applies this value directly
     * as {@code damage * (1 - bonus)}, so it must already be the final reduction rather than the
     * raw scaled value. Flat {@link #DAMAGE_REDUCTION}, still run through the shared defensive clamp
     * so it can never negate or invert incoming damage.
     */
    @Override
    public double getBonusValue(int skillLevel) {
        return clampDefensiveReduction(configFields == null
                ? DAMAGE_REDUCTION
                : configFields.calculateValue(skillLevel));
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getBonusValue(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
