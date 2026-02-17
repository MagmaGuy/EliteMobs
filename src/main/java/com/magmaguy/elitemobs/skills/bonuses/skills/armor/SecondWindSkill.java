package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 3 ARMOR skill - Second Wind
 * Automatically heals when health drops below threshold
 */
public class SecondWindSkill extends SkillBonus implements CooldownSkill {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    private static final double HEALTH_THRESHOLD = 0.25; // 25% health

    public SecondWindSkill() {
        super(
            SkillType.ARMOR,
            50,
            "Second Wind",
            "Automatically heal when health drops below 25%",
            SkillBonusType.COOLDOWN,
            3,
            "armor_second_wind"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        // No persistent bonus to remove
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        cooldownMap.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "healAmount", String.format("%.1f", getHealPercent(skillLevel) * 100),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getHealPercent(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "healAmount", String.format("%.1f", getHealPercent(skillLevel) * 100),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldownMap.clear();
    }

    // CooldownSkill interface methods

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Base 60 seconds cooldown
        return 60;
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long cooldownEnd = cooldownMap.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return false;
        }
        if (System.currentTimeMillis() >= cooldownEnd) {
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
        Long cooldownEnd = cooldownMap.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        int skillLevel = getPlayerSkillLevel(player);

        // Heal the player
        double healAmount = player.getMaxHealth() * getHealPercent(skillLevel);
        double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.HEART,
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        // Start cooldown
        startCooldown(player, skillLevel);
    }

    /**
     * Checks if the skill should trigger based on health threshold.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param newHealthPercent The player's new health percentage (0.0 to 1.0)
     */
    public void checkTrigger(Player player, double newHealthPercent) {
        if (!isActive(player) || isOnCooldown(player)) {
            return;
        }

        if (newHealthPercent <= HEALTH_THRESHOLD) {
            onActivate(player, null);
            incrementProcCount(player);
            SkillBonus.sendSkillActionBar(player, this);
        }
    }

    /**
     * Gets the heal percentage based on skill level.
     *
     * @param skillLevel The player's skill level
     * @return The heal percentage (0.0 to 1.0)
     */
    private double getHealPercent(int skillLevel) {
        // Base 20% + up to 10% based on skill level
        return 0.20 + getScaledValue(skillLevel) * 0.10;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
