package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Death's Embrace (COOLDOWN) - Cheat death once per cooldown.
 * When you would take fatal damage, instead heal to 20% health and gain brief invulnerability.
 * Tier 4 unlock.
 */
public class DeathsEmbraceSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "hoes_deaths_embrace";
    private static final long BASE_COOLDOWN = 60; // 60 seconds
    private static final double HEAL_PERCENT = 0.20; // Heal to 20% HP
    private static final double BASE_PASSIVE_BONUS = 0.05; // 5% passive damage

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public DeathsEmbraceSkill() {
        super(SkillType.HOES, 75, "Death's Embrace",
              "Cheat death once per minute, reviving with health.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Base cooldown - 0.4s per level
        long reduction = (long) (skillLevel * 0.4);
        return Math.max(30, BASE_COOLDOWN - reduction);
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

    /**
     * Attempts to prevent death for the player.
     * Returns true if death was prevented, false if on cooldown.
     */
    public static boolean preventDeath(Player player) {
        if (!activePlayers.contains(player.getUniqueId())) return false;

        // Get the instance from registry
        SkillBonus skill = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getSkillById(SKILL_ID);
        if (!(skill instanceof DeathsEmbraceSkill deathsEmbrace)) return false;

        if (deathsEmbrace.isOnCooldown(player)) {
            return false;
        }

        int skillLevel = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry
            .getPlayerSkillLevel(player, SkillType.HOES);

        // Prevent death and heal
        player.setHealth(player.getMaxHealth() * HEAL_PERCENT);

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.SOUL,
            player.getLocation(), 50, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(),
            Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);

        // Feedback
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§5Death's Embrace saved you!"));

        // Start cooldown
        deathsEmbrace.startCooldown(player, skillLevel);

        return true;
    }

    public double getPassiveDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 5% + 0.1% per level
        return BASE_PASSIVE_BONUS + (skillLevel * 0.001);
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
        double healPercent = HEAL_PERCENT * 100;
        double passiveBonus = getPassiveDamageBonus(skillLevel) * 100;
        return List.of(
                "&7Prevents fatal damage",
                "&7Heals to: &f" + String.format("%.0f", healPercent) + "% HP",
                "&7Passive Bonus: &f+" + String.format("%.1f", passiveBonus) + "% damage",
                "&7Cooldown: &f" + cooldown + " seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getPassiveDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Damage & Death Prevention", getPassiveDamageBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        cooldowns.clear();
        activePlayers.clear();
    }
}
