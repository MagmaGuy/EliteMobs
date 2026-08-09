package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ricochet (PROC) - Arrows bounce to nearby enemies.
 * Tier 3 unlock.
 */
public class RicochetSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "bows_ricochet";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% chance
    private static final double RICOCHET_RANGE = 5.0;
    private static final double BASE_RICOCHET_DAMAGE = 0.55; // 55% of original damage

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public RicochetSkill() {
        super(SkillType.BOWS, 50, "Ricochet",
              "Arrows can bounce to nearby enemies.",
              SkillBonusType.PROC, 3, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(BASE_PROC_CHANCE, 0.002, 0.35, skillLevel);
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;

        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
        double ricochetDamage = event.getDamage() * getRicochetDamageMultiplier(skillLevel);

        // Find nearest enemy to bounce to
        target.getNearbyEntities(RICOCHET_RANGE, RICOCHET_RANGE, RICOCHET_RANGE).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(target) && !(e instanceof Player))
                .map(e -> (LivingEntity) e)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(target.getLocation())))
                .ifPresent(bounceTarget -> {
                    // Use bypass to prevent recursive skill processing
                    CombatDamageContext.runPlayerToEliteBypass(
                            () -> bounceTarget.damage(ricochetDamage, player));

                    // Visual effect - line between targets
                    bounceTarget.getWorld().spawnParticle(
                            Particle.CRIT, bounceTarget.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                });
    }

    /**
     * Power budget: the bounce is a second hit worth 80% of the first at level 50, on a 25%
     * proc rate (E = 0.25 * 0.80 = 0.20).
     */
    private double getRicochetDamageMultiplier(int skillLevel) {
        return scaled(BASE_RICOCHET_DAMAGE, 0.005, skillLevel); // 55% base + 0.5% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "ricochetDamage", String.format("%.0f", getRicochetDamageMultiplier(skillLevel) * 100),
                "range", String.valueOf((int) RICOCHET_RANGE)
        ));
    }

    @Override
    // Fraction of the hit dealt to the ricochet target, not a bonus to the main hit - see affectsDamage()
    public double getBonusValue(int skillLevel) { return getRicochetDamageMultiplier(skillLevel); }
    @Override
    public boolean affectsDamage() { return false; } // Damages a second target via onProc, not the main hit
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "ricochetDamage", String.format("%.0f", getRicochetDamageMultiplier(skillLevel) * 100)
        ));
    }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
