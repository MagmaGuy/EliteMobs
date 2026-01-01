package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Ricochet (PROC) - Arrows bounce to nearby enemies.
 * Tier 3 unlock.
 */
public class RicochetSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "bows_ricochet";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% chance
    private static final double RICOCHET_RANGE = 5.0;
    private static final double BASE_RICOCHET_DAMAGE = 0.50; // 50% of original damage

    private static final Set<UUID> activePlayers = new HashSet<>();

    public RicochetSkill() {
        super(SkillType.BOWS, 50, "Ricochet",
              "Arrows can bounce to nearby enemies.",
              SkillBonusType.PROC, 3, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.002));
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
                    EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
                    try {
                        bounceTarget.damage(ricochetDamage, player);
                    } finally {
                        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
                    }

                    // Visual effect - line between targets
                    bounceTarget.getWorld().spawnParticle(
                            Particle.CRIT, bounceTarget.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                });
    }

    private double getRicochetDamageMultiplier(int skillLevel) {
        return BASE_RICOCHET_DAMAGE + (skillLevel * 0.005); // 50% base + 0.5% per level
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
        return List.of(
                "&7Proc Chance: &f" + String.format("%.1f", getProcChance(skillLevel) * 100) + "%",
                "&7Ricochet Damage: &f" + String.format("%.0f", getRicochetDamageMultiplier(skillLevel) * 100) + "%",
                "&7Range: &f" + (int)RICOCHET_RANGE + " blocks"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getRicochetDamageMultiplier(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.0f%% ricochet damage", getRicochetDamageMultiplier(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
