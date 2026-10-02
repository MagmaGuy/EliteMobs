package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Arcane Ward (PASSIVE) - Take less damage from projectiles while holding a wand.
 * Tier 1 unlock.
 * <p>
 * Defensive passive on the shared budget. It rewards trading shots from where you stand, since
 * wand balance is built around not kiting. Roughly half of a caster's damage taken is ranged, so
 * 15% at level 50 is E = 0.5 * 0.15 = 0.08, inside the always-on passive band.
 */
public class ArcaneWardSkill extends MagicWeaponSkill {

    public static final String SKILL_ID = "wands_arcane_ward";

    public ArcaneWardSkill() {
        super(SkillType.WANDS, 1, "Arcane Ward", "Take less damage from projectiles while holding a wand.",
                SkillBonusType.PASSIVE, SKILL_ID);
    }

    private double reduction(int skillLevel) {
        return SkillBonus.clampDefensiveReduction(scaled(0.10, 0.001, skillLevel)); // 15% at level 50
    }

    /** Reduces a projectile hit. Called from the defensive damage pass. */
    public static double applyWard(Player player, PlayerDamagedByEliteMobEvent event, double damage) {
        if (event.getProjectile() == null) return damage;
        if (!(SkillBonusRegistry.getSkillById(SKILL_ID) instanceof ArcaneWardSkill ward) || !ward.wieldedBy(player))
            return damage;
        ward.incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, ward);
        return damage * (1 - ward.reduction(ward.skillLevel(player)));
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("reduction", String.format("%.1f", reduction(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return reduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("reduction", String.format("%.1f", reduction(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // Defensive only.
    }
}
