package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shatter (COOLDOWN) - AoE ground pound that damages and slows nearby enemies.
 * Tier 2 unlock.
 */
public class ShatterSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "maces_shatter";
    private static final long BASE_COOLDOWN_SECONDS = 15;
    private static final double AOE_RADIUS = 5.0;
    private static final int SLOW_DURATION_TICKS = 60; // 3 seconds

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public ShatterSkill() {
        super(SkillType.MACES, 25, "Shatter",
              "Slam the ground, damaging and slowing nearby enemies.",
              SkillBonusType.COOLDOWN, 2, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        // Reduce cooldown slightly with level
        return Math.max(8, BASE_COOLDOWN_SECONDS - (skillLevel / 15));
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
        activateShatter(player);
    }

    /**
     * Activates the Shatter ability.
     * Called when attacking while ability is off cooldown.
     */
    public void activateShatter(Player player) {
        if (!isActive(player) || isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
        double damageMultiplier = getDamageMultiplier(skillLevel);

        Location center = player.getLocation();

        // Visual effects - ground slam
        player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        player.getWorld().playSound(center, Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

        // Particle ring expanding outward
        new BukkitRunnable() {
            double radius = 0.5;
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 10 || radius > AOE_RADIUS) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI / 20) * i;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location particleLoc = new Location(center.getWorld(), x, center.getY() + 0.1, z);
                    center.getWorld().spawnParticle(Particle.BLOCK,
                        particleLoc, 3, 0.1, 0.1, 0.1, 0,
                        Material.STONE.createBlockData());
                }

                radius += 0.5;
                ticks++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        // Damage and slow nearby enemies on next tick to avoid nested damage events
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : center.getWorld().getNearbyEntities(center, AOE_RADIUS, 3, AOE_RADIUS)) {
                    if (!(entity instanceof LivingEntity living)) continue;
                    if (entity.equals(player)) continue;

                    EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                    if (eliteEntity != null) {
                        double baseDamage = player.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
                        double damage = baseDamage * damageMultiplier;
                        // Bypass the player→elite formula so this skill-computed AoE hit lands
                        // as-is instead of being re-scaled and counted by the autoclicker throttle.
                        CombatDamageContext.runPlayerToEliteBypass(
                                () -> living.damage(damage, player));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_DURATION_TICKS, 1));
                    }
                }
            }
        }.runTask(MetadataHandler.PLUGIN);

        startCooldown(player, skillLevel);
    }

    public double getDamageMultiplier(int skillLevel) {
        // Power budget: a 12s cooldown at level 50 is a 1-in-12 trigger, so the slam is worth
        // 1.44 hits of damage (E = 0.083 * 1.44 = 0.12 per target, and it hits everything in a
        // 5 block radius). Hardcoded rather than read from config so every server runs the same
        // numbers while the rebalance is being validated.
        return scaled(0.70, 0.015, skillLevel);
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
                "damage", String.format("%.0f", getDamageMultiplier(skillLevel) * 100),
                "radius", String.valueOf(AOE_RADIUS),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // This is the AoE slam's own damage value, not a damage fraction - see affectsDamage().
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public boolean affectsDamage() {
        return false; // The ground slam deals its own AoE damage, doesn't modify main hit damage
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damage", String.format("%.0f", getDamageMultiplier(skillLevel) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
