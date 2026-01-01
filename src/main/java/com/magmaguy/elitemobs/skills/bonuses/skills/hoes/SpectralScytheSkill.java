package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Spectral Scythe (COOLDOWN) - Launch a spectral projectile that damages enemies in its path.
 * Long-range piercing attack with a cooldown.
 * Tier 4 unlock.
 */
public class SpectralScytheSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "hoes_spectral_scythe";
    private static final long BASE_COOLDOWN = 25; // 25 seconds
    private static final double PROJECTILE_RANGE = 10.0;
    private static final double BASE_DAMAGE_MULTIPLIER = 2.0; // 200% of weapon damage

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public SpectralScytheSkill() {
        super(SkillType.HOES, 75, "Spectral Scythe",
              "Launch a spectral scythe projectile that pierces through enemies.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Base cooldown - 0.2s per level
        long reduction = (long) (skillLevel * 0.2);
        return Math.max(10, BASE_COOLDOWN - reduction);
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
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000;
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return 0;

        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cSpectral Scythe on cooldown: " + remaining + "s"));
            return;
        }

        int skillLevel = getPlayerSkillLevel(player);
        launchSpectralScythe(player, skillLevel);
        startCooldown(player, skillLevel);
    }

    private void launchSpectralScythe(Player player, int skillLevel) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VEX_CHARGE, 1.0f, 0.5f);
        Vector direction = player.getLocation().getDirection().normalize();
        Location startLoc = player.getEyeLocation();

        double baseDamage = calculateDamage(skillLevel);
        Set<UUID> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            Location currentLoc = startLoc.clone();
            double distance = 0;

            @Override
            public void run() {
                if (distance > PROJECTILE_RANGE) {
                    cancel();
                    return;
                }

                currentLoc.add(direction.clone().multiply(0.5));
                distance += 0.5;

                // Particle trail
                currentLoc.getWorld().spawnParticle(Particle.SOUL,
                    currentLoc, 3, 0.1, 0.1, 0.1, 0.02);
                currentLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                    currentLoc, 1, 0.1, 0.1, 0.1, 0);

                // Check for entity hits (piercing - can hit multiple)
                // Use bypass to prevent recursive skill processing
                EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
                try {
                    currentLoc.getWorld().getNearbyEntities(currentLoc, 1, 1, 1).stream()
                        .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                        .filter(e -> !hitEntities.contains(e.getUniqueId()))
                        .forEach(e -> {
                            LivingEntity target = (LivingEntity) e;
                            target.damage(baseDamage, player);
                            hitEntities.add(e.getUniqueId());
                        });
                } finally {
                    EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private double calculateDamage(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 200% + 5% per level
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.05);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        endCooldown(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        endCooldown(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        long cooldown = getCooldownSeconds(skillLevel);
        double damagePercent = calculateDamage(skillLevel) * 100;
        return List.of(
                "&7Damage: &f" + String.format("%.0f", damagePercent) + "% weapon damage",
                "&7Range: &f" + String.format("%.1f", PROJECTILE_RANGE) + " blocks",
                "&7Cooldown: &f" + cooldown + " seconds",
                "&7Pierces through enemies"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateDamage(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.0f%% Projectile Damage", calculateDamage(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        cooldowns.clear();
        activePlayers.clear();
    }
}
