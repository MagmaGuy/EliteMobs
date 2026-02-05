package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Divine Shield (COOLDOWN) - When taking fatal damage, become invulnerable briefly.
 * Tier 2 unlock.
 */
public class DivineShieldSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "maces_divine_shield";
    private static final long BASE_COOLDOWN_SECONDS = 120; // 2 minutes
    private static final int INVULN_DURATION_TICKS = 40; // 2 seconds

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public DivineShieldSkill() {
        super(SkillType.MACES, 25, "Divine Shield",
              "When taking fatal damage, become invulnerable briefly.",
              SkillBonusType.COOLDOWN, 2, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Reduce cooldown with level, minimum 60 seconds
        return Math.max(60, BASE_COOLDOWN_SECONDS - (skillLevel / 2));
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
     * Attempts to prevent fatal damage.
     * Called from damage event handler before damage is applied.
     *
     * @param player The player about to take damage
     * @param incomingDamage The damage that would be dealt
     * @return true if death was prevented, false otherwise
     */
    public boolean preventDeath(Player player, double incomingDamage) {
        if (!isActive(player) || isOnCooldown(player)) {
            return false;
        }

        // Check if damage would be fatal
        if (player.getHealth() - incomingDamage <= 0) {
            activateShield(player);
            return true;
        }

        return false;
    }

    /**
     * Activates the divine shield effect.
     */
    public void activateShield(Player player) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);

        // Set health to a small amount instead of dying
        player.setHealth(Math.min(player.getMaxHealth(), 4.0)); // 2 hearts

        // Make temporarily invulnerable
        player.setInvulnerable(true);
        player.getServer().getScheduler().runTaskLater(
            com.magmaguy.elitemobs.MetadataHandler.PLUGIN,
            () -> player.setInvulnerable(false),
            INVULN_DURATION_TICKS
        );

        // Visual and sound effects - divine/holy theme
        player.getWorld().spawnParticle(Particle.END_ROD,
            player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.3);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
            player.getLocation(), 30, 1, 1, 1, 0.5);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7e\u00A7lDIVINE SHIELD!"));

        startCooldown(player, skillLevel);
        incrementProcCount(player);
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
            "&7Prevents death once",
            "&7Invulnerable for 2 seconds",
            "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return 1.0; // Binary effect
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("Prevent Death (CD: %ds)", getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
