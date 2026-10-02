package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

/**
 * Repel (PROC) - Staff strikes can knock an enemy well back and slow it.
 * Tier 2 unlock.
 * <p>
 * Crowd control for the staff's weak close-range strike, outside the damage budget. It buys the
 * caster room to fire again instead of adding damage.
 */
public class RepelSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "staves_repel";
    private static final int SLOW_TICKS = 40; // 2 seconds

    public RepelSkill() {
        super(SkillType.STAVES, 2, "Repel", "Staff strikes can knock enemies back.", SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.STAFF_MELEE) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.25, 0.002, 0.55, skillLevel); // 35% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_MELEE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        Vector away = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
        if (away.lengthSquared() < 1.0E-4) away = player.getLocation().getDirection().setY(0);
        if (away.lengthSquared() > 1.0E-4) target.setVelocity(away.normalize().multiply(1.6).setY(0.35));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_TICKS, 0));
        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, chest(target), 1, 0, 0, 0, 0);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("procChance", String.format("%.1f", getProcChance(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getProcChance(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("procChance", String.format("%.1f", getProcChance(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false;
    }
}
