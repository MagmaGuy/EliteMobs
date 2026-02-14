package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vortex Thrust (COOLDOWN) - Pull nearby enemies toward the target you hit, grouping them up.
 * Applies brief slowness after the pull.
 * Tier 2 unlock.
 */
public class VortexThrustSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "spears_vortex_thrust";
    private static final long BASE_COOLDOWN_SECONDS = 18;
    private static final double PULL_RADIUS = 5.0;
    private static final double PULL_STRENGTH = 0.8;
    private static final int SLOW_DURATION_TICKS = 40; // 2 seconds
    private static final int SLOW_AMPLIFIER = 1; // Slowness II

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public VortexThrustSkill() {
        super(SkillType.SPEARS, 25, "Vortex Thrust",
              "Pull nearby enemies toward your target, grouping them up.",
              SkillBonusType.COOLDOWN, 2, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(10, BASE_COOLDOWN_SECONDS - (skillLevel / 10));
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return false;
        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000L;
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return 0;
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;
        if (damageEvent.getEliteMobEntity() == null) return;
        LivingEntity target = damageEvent.getEliteMobEntity().getLivingEntity();
        if (target == null) return;
        activateVortexThrust(player, target);
    }

    /**
     * Activates the Vortex Thrust ability, pulling nearby enemies toward the target.
     */
    public void activateVortexThrust(Player player, LivingEntity target) {
        if (!isActive(player) || isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        Location targetLoc = target.getLocation();
        double pullStrength = PULL_STRENGTH + (skillLevel * 0.005);

        // Sound effect
        player.getWorld().playSound(targetLoc, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 1.5f);

        // Pull nearby enemies toward the target
        for (Entity entity : targetLoc.getWorld().getNearbyEntities(targetLoc, PULL_RADIUS, PULL_RADIUS, PULL_RADIUS)) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity.equals(player)) continue;
            if (entity.equals(target)) continue;

            // Only affect elite mobs
            if (EntityTracker.getEliteMobEntity(living) == null) continue;

            // Calculate pull vector toward target
            Vector pullDirection = targetLoc.toVector().subtract(living.getLocation().toVector());
            if (pullDirection.lengthSquared() < 0.1) continue; // Already at target

            pullDirection.normalize().multiply(pullStrength);
            pullDirection.setY(0.2); // Slight upward to prevent ground drag

            living.setVelocity(living.getVelocity().add(pullDirection));

            // Particle trail from entity to target
            spawnPullParticles(living.getLocation(), targetLoc);
        }

        // Apply slowness to the target too
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                SLOW_DURATION_TICKS, SLOW_AMPLIFIER, true, true));

        // Particle burst at target location
        targetLoc.getWorld().spawnParticle(Particle.PORTAL,
                targetLoc.add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.5);
        targetLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                targetLoc, 30, 2, 1, 2, 0.1);

        startCooldown(player, skillLevel);
    }

    private void spawnPullParticles(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        if (distance < 0.5) return;
        direction.normalize();

        // Spawn particles along the pull path
        for (double d = 0; d < distance; d += 0.8) {
            Location point = from.clone().add(direction.clone().multiply(d)).add(0, 1, 0);
            point.getWorld().spawnParticle(Particle.REVERSE_PORTAL, point, 2, 0.1, 0.1, 0.1, 0);
        }
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "radius", String.valueOf((int) PULL_RADIUS),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return 0; // Utility skill, no direct damage bonus
    }

    @Override
    public boolean affectsDamage() {
        return false; // Side-effect skill, doesn't modify damage
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
