package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hammer of Wrath (COOLDOWN) - Massive damage to enemies below 30% health.
 * Tier 4 unlock.
 */
public class HammerOfWrathSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "maces_hammer_of_wrath";
    private static final long BASE_COOLDOWN_SECONDS = 30;
    private static final double HEALTH_THRESHOLD = 0.30; // 30% health
    private static final double BASE_DAMAGE_MULTIPLIER = 3.0; // 300% damage

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public HammerOfWrathSkill() {
        super(SkillType.MACES, 75, "Hammer of Wrath",
              "Deal massive damage to enemies below 30% health.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(15, BASE_COOLDOWN_SECONDS - (skillLevel / 5));
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
     * Checks if Hammer of Wrath can be used on the target.
     * Returns the damage multiplier if applicable, 1.0 if not.
     */
    public double checkAndApply(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player) || isOnCooldown(player)) return 1.0;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return 1.0;

        LivingEntity target = eliteEntity.getLivingEntity();

        // Check if target is below health threshold
        double healthPercent = target.getHealth() / target.getMaxHealth();
        if (healthPercent > HEALTH_THRESHOLD) return 1.0;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
        double multiplier = getDamageMultiplier(skillLevel);

        // Activate the skill
        activateHammer(player, target, skillLevel);

        return multiplier;
    }

    private void activateHammer(Player player, LivingEntity target, int skillLevel) {
        // Visual effects - divine hammer strike
        target.getWorld().spawnParticle(Particle.END_ROD,
            target.getLocation().add(0, 2, 0), 30, 0.5, 0.5, 0.5, 0.2);
        target.getWorld().spawnParticle(Particle.EXPLOSION,
            target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7e\u00A7lHAMMER OF WRATH!"));

        startCooldown(player, skillLevel);
        incrementProcCount(player);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.05);
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
            "&7Threshold: &f<30% HP",
            "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.0f%% vs Low HP (CD: %ds)", getDamageMultiplier(skillLevel) * 100, getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
    }
}
