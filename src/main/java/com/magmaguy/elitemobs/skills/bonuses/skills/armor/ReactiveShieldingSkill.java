package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tier 4 ARMOR skill - Reactive Shielding
 * Activates a temporary damage reduction shield when taking a big hit
 */
public class ReactiveShieldingSkill extends SkillBonus implements CooldownSkill {

    private static final HashSet<UUID> activePlayers = new HashSet<>();
    private static final Map<UUID, Long> cooldownMap = new HashMap<>();
    private static final Map<UUID, Long> shieldActiveMap = new HashMap<>();
    private static final long SHIELD_DURATION = 3000; // 3 seconds
    private static final double BIG_HIT_THRESHOLD = 0.20; // 20% of max health

    public ReactiveShieldingSkill() {
        super(
            SkillType.ARMOR,
            75,
            "Reactive Shielding",
            "Activate damage reduction shield when taking a big hit",
            SkillBonusType.COOLDOWN,
            4,
            "reactive_shielding"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        // Cooldown skill, triggered by big damage hits
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
        List<String> lore = new ArrayList<>();
        lore.add("Activate damage reduction shield when taking a big hit");
        lore.add("Triggers on hits dealing 20%+ of max health");
        lore.add(String.format("Shield Reduction: %.1f%%", getShieldReduction(skillLevel) * 100));
        lore.add(String.format("Shield Duration: %.1fs", SHIELD_DURATION / 1000.0));
        lore.add(String.format("Cooldown: %ds", getCooldownSeconds(skillLevel)));
        return lore;
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getShieldReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% damage reduction shield (CD: %ds)",
            getShieldReduction(skillLevel) * 100, getCooldownSeconds(skillLevel));
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
        // Base 30 seconds cooldown
        return 30;
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

        // Send action bar message
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Reactive Shield Activated!"));

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
        if (!isActive(player)) {
            return false;
        }

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
        if (!isActive(player) || isOnCooldown(player)) {
            return;
        }

        // Trigger on big hits (20%+ of max health)
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
        // Base 50% + up to 25% more based on skill level
        return 0.50 + getScaledValue(skillLevel) * 0.25;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
