package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Battlemage's Guard (PASSIVE) - Take less damage from enemies close to you while holding a staff.
 * Tier 1 unlock.
 * <p>
 * Defensive passive on the shared budget, {@code E = uptime * reduction} against 0.20. About three
 * quarters of the damage a caster takes comes from enemies within 4 blocks, so 15% at level 50 is
 * E = 0.75 * 0.15 = 0.11, the always-on passive band.
 */
public class BattlemagesGuardSkill extends MagicWeaponSkill {

    public static final String SKILL_ID = "staves_battlemages_guard";
    private static final double GUARD_RANGE = 4.0;

    public BattlemagesGuardSkill() {
        super(SkillType.STAVES, 1, "Battlemage's Guard", "Take less damage from nearby enemies while holding a staff.",
                SkillBonusType.PASSIVE, SKILL_ID);
    }

    private double reduction(int skillLevel) {
        return SkillBonus.clampDefensiveReduction(scaled(0.10, 0.001, skillLevel)); // 15% at level 50
    }

    /** Reduces a hit from an enemy within guard range. Called from the defensive damage pass. */
    public static double applyGuard(Player player, PlayerDamagedByEliteMobEvent event, double damage) {
        if (!(SkillBonusRegistry.getSkillById(SKILL_ID) instanceof BattlemagesGuardSkill guard) || !guard.wieldedBy(player))
            return damage;
        LivingEntity attacker = event.getAttacker();
        if (attacker == null || !attacker.getWorld().equals(player.getWorld())
                || attacker.getLocation().distanceSquared(player.getLocation()) > GUARD_RANGE * GUARD_RANGE)
            return damage;
        guard.incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, guard);
        return damage * (1 - guard.reduction(guard.skillLevel(player)));
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
