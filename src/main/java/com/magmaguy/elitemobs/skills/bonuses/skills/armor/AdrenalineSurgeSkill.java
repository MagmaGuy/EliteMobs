package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 2 ARMOR skill - Adrenaline Surge
 * Grants buffs when health drops below threshold
 */
public class AdrenalineSurgeSkill extends SkillBonus implements CooldownSkill {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    private static final double HEALTH_THRESHOLD = 0.30; // 30% health

    public AdrenalineSurgeSkill() {
        super(
            SkillType.ARMOR,
            25,
            "Adrenaline Surge",
            "Gain buffs when health drops below 30%",
            SkillBonusType.COOLDOWN,
            2,
            "armor_adrenaline_surge"
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
                "duration", String.format("%.1fs", getDuration(skillLevel) / 20.0),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getScaledValue(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
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
        // Base 45 seconds cooldown
        return 45;
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

        // Grant adrenaline buffs
        int duration = getDuration(skillLevel);
        int amplifier = (int) Math.floor(getScaledValue(skillLevel));

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 0));

        // Visual effect
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
            player.getLocation(), 10, 0.5, 0.5, 0.5, 0);

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
        }
    }

    /**
     * Gets the buff duration in ticks.
     *
     * @param skillLevel The player's skill level
     * @return Duration in ticks
     */
    private int getDuration(int skillLevel) {
        // Base 5 seconds (100 ticks) + up to 2 seconds (40 ticks) based on skill level
        return 100 + (int) (getScaledValue(skillLevel) * 40);
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
