package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Explosive Tip (PROC) - Chance for bolts to explode on impact.
 * Tier 2 unlock.
 */
public class ExplosiveTipSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "crossbows_explosive_tip";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% chance
    private static final double BASE_EXPLOSION_RADIUS = 4.0;
    private static final double BASE_EXPLOSION_DAMAGE = 0.55; // 55% of original

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public ExplosiveTipSkill() {
        super(SkillType.CROSSBOWS, 25, "Explosive Tip",
              "Bolts have a chance to explode on impact.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        return scaled(BASE_PROC_CHANCE, 0.002, 0.35, skillLevel);
    }

    public double getExplosionRadius(int skillLevel) {
        return scaled(BASE_EXPLOSION_RADIUS, 0.02, skillLevel); // 4.0 to 5.5 at level 75
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;

        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        double explosionDamage = event.getDamage() * getExplosionDamageMultiplier(skillLevel);
        double radius = getExplosionRadius(skillLevel);

        // Create explosion effect
        target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);

        // Damage nearby enemies
        // Use bypass to prevent recursive skill processing
        target.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                .forEach(e -> CombatDamageContext.runPlayerToEliteBypass(
                        () -> ((LivingEntity) e).damage(explosionDamage, player)));
    }

    /**
     * Power budget: the blast is worth 80% of the triggering hit at level 50, on a 25% proc
     * rate (E = 0.25 * 0.80 = 0.20).
     */
    private double getExplosionDamageMultiplier(int skillLevel) {
        return scaled(BASE_EXPLOSION_DAMAGE, 0.005, skillLevel); // 55% base + 0.5% per level
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
                "aoeDamage", String.format("%.0f", getExplosionDamageMultiplier(skillLevel) * 100),
                "radius", String.format("%.1f", getExplosionRadius(skillLevel))
        ));
    }

    @Override
    // Fraction of the hit dealt to nearby enemies by the explosion, not a bonus to the main hit - see affectsDamage()
    public double getBonusValue(int skillLevel) { return getExplosionDamageMultiplier(skillLevel); }
    @Override
    public boolean affectsDamage() { return false; } // Deals AoE explosion damage via onProc, not the main hit
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("aoeDamage", String.format("%.0f", getExplosionDamageMultiplier(skillLevel) * 100))); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
