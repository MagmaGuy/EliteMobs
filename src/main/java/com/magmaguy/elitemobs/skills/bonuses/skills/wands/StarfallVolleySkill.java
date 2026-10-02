package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Starfall Volley (COOLDOWN) - Every 20 seconds, a missile hit calls down a volley of extra bolts.
 * Tier 4 unlock.
 * <p>
 * Eight bolts at 90% of the triggering hit, spread over the target and up to three enemies near it.
 * Power budget: a 20 second cooldown spans ~36 missiles, so E = 8 * 0.90 / 36 = 0.20.
 */
public class StarfallVolleySkill extends MagicWeaponSkill implements CooldownSkill {

    public static final String SKILL_ID = "wands_starfall_volley";
    private static final long COOLDOWN_SECONDS = 20;
    private static final int BOLTS = 8;
    private static final int MAX_TARGETS = 4;
    private static final double VOLLEY_RADIUS = 10.0;
    private static final long BOLT_INTERVAL_TICKS = 2;

    private final Set<BukkitRunnable> volleys = ConcurrentHashMap.newKeySet();

    public StarfallVolleySkill() {
        super(SkillType.WANDS, 4, "Starfall Volley", "A missile hit calls down a volley of bolts every 20 seconds.",
                SkillBonusType.COOLDOWN, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return COOLDOWN_SECONDS;
    }

    private double boltFraction(int skillLevel) {
        return scaled(0.70, 0.004, skillLevel); // 90% at level 50
    }

    @Override
    public boolean tryActivate(Player player, Object event) {
        EliteMobDamagedByPlayerEvent hit = strike(event, MagicStrike.WAND_MISSILE);
        if (hit == null || hit.getEliteMobEntity().getLivingEntity() == null) return false;
        LivingEntity first = hit.getEliteMobEntity().getLivingEntity();
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(first);
        for (LivingEntity enemy : enemiesNear(player, first.getLocation(), VOLLEY_RADIUS, first)) {
            if (targets.size() >= MAX_TARGETS) break;
            targets.add(enemy);
        }
        int skillLevel = skillLevel(player);
        double boltDamage = hit.getDamage() * boltFraction(skillLevel);
        BukkitRunnable volley = new BukkitRunnable() {
            private int fired = 0;

            @Override
            public void run() {
                targets.removeIf(target -> !target.isValid() || target.isDead());
                if (fired >= BOLTS || targets.isEmpty() || !player.isOnline()) {
                    volleys.remove(this);
                    cancel();
                    return;
                }
                LivingEntity target = targets.get(fired % targets.size());
                beam(player.getEyeLocation(), chest(target), Particle.END_ROD);
                sideDamage(player, target, boltDamage);
                fired++;
            }
        };
        volleys.add(volley);
        volley.runTaskTimer(MetadataHandler.PLUGIN, 0L, BOLT_INTERVAL_TICKS);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1F, 1.3F);
        startCooldown(player, skillLevel);
        return true;
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bolts", String.valueOf(BOLTS),
                "boltPercent", String.format("%.0f", boltFraction(skillLevel) * 100),
                "cooldown", String.valueOf(COOLDOWN_SECONDS)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return boltFraction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bolts", String.valueOf(BOLTS),
                "boltPercent", String.format("%.0f", boltFraction(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The bolts are separate hits.
    }

    @Override
    public void shutdown() {
        volleys.forEach(BukkitRunnable::cancel);
        volleys.clear();
        super.shutdown();
    }
}
