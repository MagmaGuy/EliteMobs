package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Smolder (PROC) - Fireball hits can leave the target burning for part of the hit.
 * Tier 1 unlock.
 * <p>
 * Power budget: the standard proc band, 25% at level 50 for 80% of the hit over 4 seconds
 * (E = 0.25 * 0.80 = 0.20). Unlike the Ignition enchantment's vanilla fire, the burn scales with
 * the hit. A new burn replaces the old one on the same target.
 */
public class SmolderSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "staves_smolder";
    private static final int BURN_PULSES = 4; // one per second

    private final Map<UUID, BukkitRunnable> burns = new ConcurrentHashMap<>();

    public SmolderSkill() {
        super(SkillType.STAVES, 1, "Smolder", "Fireball hits can leave the target smoldering.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.STAFF_FIREBALL) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.15, 0.002, 0.45, skillLevel); // 25% at level 50
    }

    /** Share of the triggering hit dealt over the whole burn. */
    private double burnFraction(int skillLevel) {
        return scaled(0.60, 0.004, skillLevel); // 80% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        double pulseDamage = event.getDamageWithoutCriticalStrike() * burnFraction(skillLevel(player)) / BURN_PULSES;
        BukkitRunnable previous = burns.remove(target.getUniqueId());
        if (previous != null) previous.cancel();
        BukkitRunnable burn = new BukkitRunnable() {
            private int remaining = BURN_PULSES;

            @Override
            public void run() {
                if (remaining-- <= 0 || !target.isValid() || target.isDead()) {
                    burns.remove(target.getUniqueId(), this);
                    cancel();
                    return;
                }
                sideDamage(player, target, pulseDamage);
                target.getWorld().spawnParticle(Particle.FLAME, chest(target), 8, 0.3, 0.4, 0.3, 0.01);
            }
        };
        burns.put(target.getUniqueId(), burn);
        burn.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "burnPercent", String.format("%.0f", burnFraction(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return burnFraction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("burnPercent", String.format("%.0f", burnFraction(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The burn is dealt over time; the fireball hit itself is unchanged.
    }

    @Override
    public void shutdown() {
        burns.values().forEach(BukkitRunnable::cancel);
        burns.clear();
        super.shutdown();
    }
}
