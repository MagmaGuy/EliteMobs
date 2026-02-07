package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Skewer (COOLDOWN) - Pierce through multiple enemies in a line.
 * Tier 2 unlock.
 */
public class SkewerSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "spears_skewer";
    private static final long BASE_COOLDOWN_SECONDS = 18;
    private static final double PIERCE_DISTANCE = 8.0;
    private static final double PIERCE_WIDTH = 1.5;

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public SkewerSkill() {
        super(SkillType.SPEARS, 25, "Skewer",
              "Pierce through enemies in a line.",
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
        activateSkewer(player);
    }

    /**
     * Activates the Skewer ability.
     */
    public void activateSkewer(Player player) {
        if (!isActive(player) || isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double damageMultiplier = getDamageMultiplier(skillLevel);

        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        // Play sound
        player.getWorld().playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);

        // Particle trail showing the pierce path
        new BukkitRunnable() {
            double distance = 0;
            Set<UUID> hitEntities = new HashSet<>();

            @Override
            public void run() {
                if (distance > PIERCE_DISTANCE) {
                    cancel();
                    return;
                }

                Location current = start.clone().add(direction.clone().multiply(distance));

                // Particle effect
                current.getWorld().spawnParticle(Particle.CRIT,
                    current, 3, 0.1, 0.1, 0.1, 0);
                current.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                    current, 1, 0, 0, 0, 0);

                // Check for entities to hit
                for (Entity entity : current.getWorld().getNearbyEntities(current, PIERCE_WIDTH, PIERCE_WIDTH, PIERCE_WIDTH)) {
                    if (!(entity instanceof LivingEntity living)) continue;
                    if (entity.equals(player)) continue;
                    if (hitEntities.contains(entity.getUniqueId())) continue;

                    EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                    if (eliteEntity != null) {
                        double baseDamage = player.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
                        double damage = baseDamage * damageMultiplier;
                        living.damage(damage, player);
                        hitEntities.add(entity.getUniqueId());

                        // Hit effect
                        living.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                            living.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                    }
                }

                distance += 0.5;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7b\u00A7lSKEWER!"));

        startCooldown(player, skillLevel);
        incrementProcCount(player);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return 0.8 + (skillLevel * 0.015);
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
            "&7Damage: &f" + String.format("%.0f", getDamageMultiplier(skillLevel) * 100) + "% per target",
            "&7Range: &f" + PIERCE_DISTANCE + " blocks",
            "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("Pierce %.0f%% (CD: %ds)", getDamageMultiplier(skillLevel) * 100, getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
