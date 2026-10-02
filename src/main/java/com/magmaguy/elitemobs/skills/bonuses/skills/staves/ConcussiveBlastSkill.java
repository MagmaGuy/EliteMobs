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
 * Concussive Blast (PROC) - Fireball hits can fling targets away and slow them.
 * Tier 1 unlock.
 * <p>
 * Crowd control, outside the damage budget like Frostbite and Snare. With one fireball every
 * 2 seconds, a 20% chance at level 50 and a 2 second slow keep a target slowed about 20% of the time.
 */
public class ConcussiveBlastSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "staves_concussive_blast";
    private static final int SLOW_TICKS = 40; // 2 seconds

    public ConcussiveBlastSkill() {
        super(SkillType.STAVES, 1, "Concussive Blast", "Fireball hits can fling targets away and slow them.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.STAFF_FIREBALL) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.10, 0.002, 0.40, skillLevel); // 20% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        Vector away = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
        if (away.lengthSquared() > 1.0E-4) target.setVelocity(away.normalize().multiply(0.7).setY(0.3));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_TICKS, 1));
        target.getWorld().spawnParticle(Particle.CLOUD, chest(target), 12, 0.4, 0.3, 0.4, 0.05);
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
