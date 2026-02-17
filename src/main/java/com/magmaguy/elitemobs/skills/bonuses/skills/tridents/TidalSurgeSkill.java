package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tidal Surge (COOLDOWN) - Creates a water surge that knocks back and damages nearby enemies.
 * Tier 2 unlock.
 */
public class TidalSurgeSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "tridents_tidal_surge";
    private static final long BASE_COOLDOWN = 20; // 20 seconds
    private static final double SURGE_RADIUS = 5.0;

    // Track cooldowns: PlayerUUID -> Cooldown end time
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public TidalSurgeSkill() {
        super(SkillType.TRIDENTS, 25, "Tidal Surge",
              "Creates a water surge that knocks back and damages nearby enemies.",
              SkillBonusType.COOLDOWN, 2, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Cooldown reduces by 0.1s per level, minimum 10 seconds
        return Math.max(10, BASE_COOLDOWN - (skillLevel / 10));
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long endTime = cooldownMap.get(player.getUniqueId());
        if (endTime == null) return false;
        if (System.currentTimeMillis() >= endTime) {
            cooldownMap.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000L;
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long endTime = cooldownMap.get(player.getUniqueId());
        if (endTime == null) return 0;
        long remaining = (endTime - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;
        if (isOnCooldown(player)) return;

        EliteEntity eliteEntity = damageEvent.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = getPlayerSkillLevel(player);

        startCooldown(player, skillLevel);

        // Create tidal surge effect
        target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 100, 2, 1, 2, 0.1);

        // Push all nearby enemies away
        double knockbackStrength = calculateKnockback(skillLevel);
        double aoeDamageMultiplier = calculateAoeDamage(skillLevel);

        // Use bypass to prevent recursive skill processing
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.getNearbyEntities(SURGE_RADIUS, SURGE_RADIUS, SURGE_RADIUS).stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                    .forEach(e -> {
                        Vector direction = e.getLocation().toVector()
                                .subtract(target.getLocation().toVector());
                        // Check for zero/near-zero vector to avoid NaN from normalize()
                        if (direction.lengthSquared() < 0.001) {
                            // Entities at same location - use random direction
                            direction = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
                        }
                        direction.normalize().multiply(knockbackStrength);
                        direction.setY(0.5);
                        e.setVelocity(direction);
                        ((LivingEntity) e).damage(damageEvent.getDamage() * aoeDamageMultiplier, player);
                    });
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }
    }

    private double calculateKnockback(int skillLevel) {
        if (configFields != null) {
            return 1.5 + (configFields.calculateValue(skillLevel) * 0.5);
        }
        return 1.5 + (skillLevel * 0.01);
    }

    private double calculateAoeDamage(int skillLevel) {
        if (configFields != null) {
            return 0.3 * configFields.calculateValue(skillLevel);
        }
        return 0.3 + (skillLevel * 0.003);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        long cooldown = getCooldownSeconds(skillLevel);
        double knockback = calculateKnockback(skillLevel);
        return applyLoreTemplates(Map.of(
                "cooldown", String.valueOf(cooldown),
                "knockback", String.format("%.1f", knockback),
                "radius", String.valueOf(SURGE_RADIUS)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateKnockback(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "knockback", String.format("%.1f", calculateKnockback(skillLevel))
        ));
    }

    @Override
    public void shutdown() {
        cooldownMap.clear();
        activePlayers.clear();
    }
}
