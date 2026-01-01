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
 * Tier 4 ARMOR skill - Last Stand
 * Prevents death once per cooldown, saving the player at 1 HP
 */
public class LastStandSkill extends SkillBonus implements CooldownSkill {

    private static final HashSet<UUID> activePlayers = new HashSet<>();
    private static final Map<UUID, Long> cooldownMap = new HashMap<>();

    public LastStandSkill() {
        super(
            SkillType.ARMOR,
            75,
            "Last Stand",
            "Survive a fatal blow once per cooldown",
            SkillBonusType.COOLDOWN,
            4,
            "last_stand"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        // Cooldown skill, triggered when fatal damage would occur
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
        List<String> lore = new ArrayList<>();
        lore.add("Survive a fatal blow once per cooldown");
        lore.add("Prevents death and restores you to 1 HP");
        lore.add(String.format("Cooldown: %ds", getCooldownSeconds(skillLevel)));
        return lore;
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return 1.0; // Binary effect - either active or not
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("Prevent death (CD: %ds)", getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldownMap.clear();
    }

    // CooldownSkill interface methods

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Base 120 seconds (2 minutes) cooldown
        return 120;
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

        // Set health to 1 heart (2.0 HP)
        player.setHealth(2.0);

        // Visual and sound effects - like totem of undying
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
            player.getLocation(), 50, 1, 1, 1, 0.5);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        // Send action bar message
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Last Stand! You survived!"));

        // Start cooldown
        startCooldown(player, skillLevel);
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
            onActivate(player, null);
            return true;
        }

        return false;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
