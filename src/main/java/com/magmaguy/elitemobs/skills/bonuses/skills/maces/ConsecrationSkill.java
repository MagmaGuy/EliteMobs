package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consecration (COOLDOWN) - Create holy ground that damages enemies standing in it.
 * Tier 3 unlock.
 */
public class ConsecrationSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "maces_consecration";
    private static final long BASE_COOLDOWN_SECONDS = 25;
    private static final double AOE_RADIUS = 4.0;
    private static final int DURATION_TICKS = 100; // 5 seconds
    private static final int TICK_INTERVAL = 20; // Damage every second

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public ConsecrationSkill() {
        super(SkillType.MACES, 50, "Consecration",
              "Create holy ground that damages enemies within.",
              SkillBonusType.COOLDOWN, 3, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(15, BASE_COOLDOWN_SECONDS - (skillLevel / 10));
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
        activateConsecration(player);
    }

    /**
     * Activates the Consecration ability.
     */
    public void activateConsecration(Player player) {
        if (!isActive(player) || isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
        double damagePerTick = getDamagePerTick(skillLevel);

        Location center = player.getLocation();

        // Initial effect
        player.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);

        // Create consecrated ground effect
        new BukkitRunnable() {
            int ticksRemaining = DURATION_TICKS;

            @Override
            public void run() {
                if (ticksRemaining <= 0 || !player.isOnline()) {
                    cancel();
                    return;
                }

                // Visual effect - golden particles on the ground
                for (int i = 0; i < 20; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = Math.random() * AOE_RADIUS;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location particleLoc = new Location(center.getWorld(), x, center.getY() + 0.1, z);
                    center.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0.1, 0, 0);
                }

                // Damage enemies every tick interval
                if (ticksRemaining % TICK_INTERVAL == 0) {
                    for (Entity entity : center.getWorld().getNearbyEntities(center, AOE_RADIUS, 3, AOE_RADIUS)) {
                        if (!(entity instanceof LivingEntity living)) continue;
                        if (entity.equals(player)) continue;

                        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                        if (eliteEntity != null) {
                            living.damage(damagePerTick, player);
                            // Small hit effect
                            living.getWorld().spawnParticle(Particle.FLAME,
                                living.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }

                ticksRemaining--;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        startCooldown(player, skillLevel);
    }

    public double getDamagePerTick(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel); // Base damage per tick
        }
        return 5.0 + (skillLevel * 0.1);
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
                "damagePerSecond", String.format("%.1f", getDamagePerTick(skillLevel)),
                "radius", String.valueOf(AOE_RADIUS),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamagePerTick(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damagePerSecond", String.format("%.1f", getDamagePerTick(skillLevel)),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
