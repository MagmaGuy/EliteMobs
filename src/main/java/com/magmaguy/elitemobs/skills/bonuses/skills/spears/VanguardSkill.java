package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Vanguard (COOLDOWN) - Charge forward, damaging enemies in path.
 * Tier 3 unlock.
 */
public class VanguardSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "spears_vanguard";
    private static final long BASE_COOLDOWN_SECONDS = 20;
    private static final double CHARGE_DISTANCE = 10.0;
    private static final double CHARGE_WIDTH = 2.0;
    private static final int CHARGE_DURATION_TICKS = 10;

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public VanguardSkill() {
        super(SkillType.SPEARS, 50, "Vanguard",
              "Charge forward, damaging enemies in your path.",
              SkillBonusType.COOLDOWN, 3, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(12, BASE_COOLDOWN_SECONDS - (skillLevel / 8));
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

    /**
     * Activates the Vanguard charge.
     */
    public void activateVanguard(Player player) {
        if (!isActive(player) || isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double damageMultiplier = getDamageMultiplier(skillLevel);

        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Set<UUID> hitEntities = new HashSet<>();

        // Play charge sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1.0f, 1.2f);

        // Charge forward
        new BukkitRunnable() {
            int ticks = 0;
            double distanceTraveled = 0;

            @Override
            public void run() {
                if (ticks >= CHARGE_DURATION_TICKS || distanceTraveled >= CHARGE_DISTANCE) {
                    cancel();
                    return;
                }

                // Move player forward
                double moveDistance = CHARGE_DISTANCE / CHARGE_DURATION_TICKS;
                Vector movement = direction.clone().multiply(moveDistance);
                Location newLoc = player.getLocation().add(movement);

                // Check if movement is valid (not into a wall)
                if (newLoc.getBlock().isPassable()) {
                    player.setVelocity(movement.multiply(2));

                    // Particle trail
                    player.getWorld().spawnParticle(Particle.CLOUD,
                        player.getLocation(), 5, 0.3, 0.1, 0.3, 0.02);
                }

                // Damage nearby enemies
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), CHARGE_WIDTH, 2, CHARGE_WIDTH)) {
                    if (!(entity instanceof LivingEntity living)) continue;
                    if (entity.equals(player)) continue;
                    if (hitEntities.contains(entity.getUniqueId())) continue;

                    EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                    if (eliteEntity != null) {
                        double damage = CombatSystem.getPlayerMaxDamage(player, eliteEntity) * damageMultiplier;
                        eliteEntity.damage(damage, player);
                        hitEntities.add(entity.getUniqueId());

                        // Knockback
                        living.setVelocity(direction.clone().multiply(0.5).setY(0.3));

                        // Hit effect
                        living.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                            living.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                    }
                }

                distanceTraveled += moveDistance;
                ticks++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7b\u00A7lVANGUARD!"));

        startCooldown(player, skillLevel);
        incrementProcCount(player);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return 1.0 + (skillLevel * 0.02);
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
        return List.of(
            "&7Damage: &f" + String.format("%.0f", getDamageMultiplier(skillLevel) * 100) + "%",
            "&7Distance: &f" + CHARGE_DISTANCE + " blocks",
            "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("Charge %.0f%% (CD: %ds)", getDamageMultiplier(skillLevel) * 100, getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
