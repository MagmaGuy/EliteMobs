package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

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
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

/**
 * Phoenix Mantle (COOLDOWN) - Survive a fatal blow and burst into flame, once per cooldown.
 * Tier 4 unlock.
 * <p>
 * The save matches Last Stand: one heart left, 120 seconds. The flame burst only throws back and
 * ignites enemies within 4 blocks; it deals no damage of its own and heals nothing.
 */
public class PhoenixMantleSkill extends MagicWeaponSkill implements CooldownSkill {

    public static final String SKILL_ID = "staves_phoenix_mantle";
    private static final long COOLDOWN_SECONDS = 120;
    private static final double SURVIVING_HEALTH = 2.0; // one heart, like Last Stand
    private static final double BURST_RADIUS = 4.0;
    private static final int BURN_TICKS = 60;

    public PhoenixMantleSkill() {
        super(SkillType.STAVES, 4, "Phoenix Mantle", "Survive a fatal blow and burst into flame.",
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

    /** Saves a staff wielder from a fatal elite hit. Called from the defensive damage pass. */
    public static boolean preventDeath(Player player) {
        if (!(SkillBonusRegistry.getSkillById(SKILL_ID) instanceof PhoenixMantleSkill mantle)
                || !mantle.wieldedBy(player) || mantle.isOnCooldown(player)) return false;
        player.setHealth(SURVIVING_HEALTH);
        for (LivingEntity enemy : enemiesNear(player, player.getLocation(), BURST_RADIUS, null)) {
            Vector away = enemy.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
            if (away.lengthSquared() > 1.0E-4) enemy.setVelocity(away.normalize().multiply(1.2).setY(0.4));
            enemy.setFireTicks(Math.max(enemy.getFireTicks(), BURN_TICKS));
        }
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 80, 1.5, 1.0, 1.5, 0.08);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1F, 1.4F);
        mantle.startCooldown(player, mantle.skillLevel(player));
        mantle.incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, mantle);
        return true;
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("cooldown", String.valueOf(COOLDOWN_SECONDS)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return 0;
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
