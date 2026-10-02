package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

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

import java.util.List;
import java.util.Map;

/**
 * Chilling Bolt (PROC) - Missiles can chill the target, slowing it.
 * Tier 1 unlock.
 * <p>
 * Crowd control, outside the damage budget. A wand fires every 0.55 seconds, so the chance is
 * sized for rapid fire: 5% at level 50 with a 2 second slow keeps a target slowed about 18% of the
 * time, level with Concussive Blast on a fireball every 2 seconds.
 */
public class ChillingBoltSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "wands_chilling_bolt";
    private static final int SLOW_TICKS = 40; // 2 seconds

    public ChillingBoltSkill() {
        super(SkillType.WANDS, 1, "Chilling Bolt", "Missiles can chill enemies, slowing them.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.WAND_MISSILE) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.03, 0.0004, 0.10, skillLevel); // 5% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_TICKS, 1));
        target.getWorld().spawnParticle(Particle.SNOWFLAKE, chest(target), 14, 0.3, 0.4, 0.3, 0.01);
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
