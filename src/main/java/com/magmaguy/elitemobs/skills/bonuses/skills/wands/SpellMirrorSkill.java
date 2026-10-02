package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Spell Mirror (COOLDOWN) - Every 15 seconds, negate the next hit and answer it in kind.
 * Tier 4 unlock.
 * <p>
 * The counter deals 200% of the negated hit to the attacker. Like Retaliation, that damage sits
 * outside the defensive budget. Power budget: under steady fire a caster is hit every ~2 seconds,
 * so one negation per 15 seconds is about one hit in 7.5 (E = 0.13).
 */
public class SpellMirrorSkill extends MagicWeaponSkill implements CooldownSkill {

    public static final String SKILL_ID = "wands_spell_mirror";
    private static final long COOLDOWN_SECONDS = 15;
    private static final double COUNTER_MULTIPLIER = 2.0;

    public SpellMirrorSkill() {
        super(SkillType.WANDS, 4, "Spell Mirror", "Negate the next hit every 15 seconds and answer it.",
                SkillBonusType.COOLDOWN, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return COOLDOWN_SECONDS;
    }

    @Override
    public boolean triggersOnOffensiveHit() {
        return false;
    }

    /** Negates an elite hit on a wand wielder. Called from the defensive damage pass. */
    public static boolean tryMirror(Player player, PlayerDamagedByEliteMobEvent event, double damage) {
        if (!(SkillBonusRegistry.getSkillById(SKILL_ID) instanceof SpellMirrorSkill mirror)
                || !mirror.wieldedBy(player) || mirror.isOnCooldown(player)) return false;
        mirror.startCooldown(player, mirror.skillLevel(player));
        LivingEntity attacker = event.getAttacker();
        if (attacker != null && attacker.getWorld().equals(player.getWorld())) {
            beam(player.getEyeLocation(), chest(attacker), Particle.ENCHANT);
            sideDamage(player, attacker, damage * COUNTER_MULTIPLIER);
        }
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 16, 0.4, 0.6, 0.4, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1F, 1.2F);
        mirror.incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, mirror);
        return true;
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "counterPercent", String.format("%.0f", COUNTER_MULTIPLIER * 100),
                "cooldown", String.valueOf(COOLDOWN_SECONDS)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return COUNTER_MULTIPLIER;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("cooldown", String.valueOf(COOLDOWN_SECONDS)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // Defensive only.
    }
}
