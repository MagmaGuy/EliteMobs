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
 * Tier 4 ARMOR skill - Reactive Shielding
 * Activates a temporary damage reduction shield when taking a big hit
 */
public class ReactiveShieldingSkill extends SkillBonus implements CooldownSkill {

    /**
     * Shield strength and its duty cycle, on the shared defensive power budget.
     * <p>
     * Sustained power is {@code E = uptime * reduction} and the budget is 0.20. The shield is the
     * strongest single reduction the system allows ({@link SkillBonus#MAX_DEFENSIVE_REDUCTION}), so
     * the budget is spent on uptime instead: {@code 0.20 / 0.80 = 0.25}, which is 5 seconds of shield
     * per 20 second cooldown. The old 3s/30s duty cycle was 0.10 uptime, less than half of budget.
     * Hardcoded on purpose: balance values no longer come from config, only presentation does.
     */
    private static final double SHIELD_REDUCTION = 0.80;
    private static final long SHIELD_DURATION = 5000; // 5 seconds
    private static final long COOLDOWN_SECONDS = 20;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> shieldActiveMap = new ConcurrentHashMap<>();
    private static final double BIG_HIT_THRESHOLD = 0.10; // 10% of max health

    public ReactiveShieldingSkill() {
        super(
            SkillType.ARMOR,
            75,
            "Reactive Shielding",
            "Activate damage reduction shield when taking a big hit",
            SkillBonusType.COOLDOWN,
            4,
            "armor_reactive_shielding"
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
        shieldActiveMap.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "shieldReduction", String.format("%.1f", getShieldReduction(skillLevel) * 100),
                "shieldDuration", String.format("%.1fs", SHIELD_DURATION / 1000.0),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getShieldReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "shieldReduction", String.format("%.1f", getShieldReduction(skillLevel) * 100),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldownMap.clear();
        shieldActiveMap.clear();
    }

    // CooldownSkill interface methods

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        // 5s shield / 20s cooldown = 0.25 uptime, the budgeted duty cycle for an 80% reduction
        return COOLDOWN_SECONDS;
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

        // Activate shield
        shieldActiveMap.put(player.getUniqueId(), System.currentTimeMillis() + SHIELD_DURATION);

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.END_ROD,
            player.getLocation(), 30, 1, 1, 1, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);

        // Start cooldown
        startCooldown(player, skillLevel);
    }

    /**
     * Checks if the shield is currently active for a player.
     *
     * @param player The player to check
     * @return true if shield is active
     */
    public boolean isShieldActive(Player player) {
        Long activeUntil = shieldActiveMap.get(player.getUniqueId());
        if (activeUntil == null) {
            return false;
        }

        if (System.currentTimeMillis() > activeUntil) {
            shieldActiveMap.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    /**
     * Checks if damage should trigger the shield, and activates it if conditions are met.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param damagePercent The damage as a percentage of max health (0.0 to 1.0)
     */
    public void checkTrigger(Player player, double damagePercent) {
        if (isOnCooldown(player)) {
            return;
        }

        // Trigger on big hits (10%+ of max health)
        if (damagePercent >= BIG_HIT_THRESHOLD) {
            onActivate(player, null);
        }
    }

    /**
     * Calculates damage reduction if shield is active.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param originalDamage The original damage amount
     * @return The modified damage amount
     */
    public double modifyIncomingDamage(Player player, double originalDamage) {
        if (!isShieldActive(player)) {
            return originalDamage;
        }

        int skillLevel = getPlayerSkillLevel(player);
        double reduction = getShieldReduction(skillLevel);

        // Massive damage reduction while shield is active
        return originalDamage * (1 - reduction);
    }

    /**
     * Gets the shield's damage reduction percentage.
     *
     * @param skillLevel The player's skill level
     * @return The reduction percentage (0.0 to 1.0)
     */
    private double getShieldReduction(int skillLevel) {
        double configuredReduction = configFields == null
                ? SHIELD_REDUCTION
                : 0.50 + configFields.calculateValue(skillLevel) * 0.25;
        return clampDefensiveReduction(configuredReduction);
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
