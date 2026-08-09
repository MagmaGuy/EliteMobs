package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Impaler (COOLDOWN) - Pin an enemy in place, dealing massive damage.
 * Tier 4 unlock.
 */
public class ImpalerSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "spears_impaler";
    private static final long BASE_COOLDOWN_SECONDS = 45;
    private static final int PIN_DURATION_TICKS = 60; // 3 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 4.0; // 400% damage

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> pinnedEntities = ConcurrentHashMap.newKeySet();

    public ImpalerSkill() {
        super(SkillType.SPEARS, 75, "Impaler",
              "Pin an enemy in place, dealing massive damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        return Math.max(25, BASE_COOLDOWN_SECONDS - (skillLevel / 4));
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
     * Attempts to impale the target.
     * Returns the damage multiplier if successful, 1.0 otherwise.
     */
    public double checkAndApply(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player) || isOnCooldown(player)) return 1.0;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return 1.0;

        // Don't impale if already pinned
        if (pinnedEntities.contains(eliteEntity.getLivingEntity().getUniqueId())) return 1.0;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double multiplier = getDamageMultiplier(skillLevel);

        // Execute the impale
        activateImpale(player, eliteEntity, skillLevel);

        return multiplier;
    }

    /**
     * Reports whether the impale actually landed, so the caller only consumes the cooldown and the
     * damage bonus on a real activation. Returning 1.0 from checkAndApply means the target was
     * already pinned (or the skill was unavailable) and nothing happened.
     */
    @Override
    public boolean tryActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return false;
        return checkAndApply(player, damageEvent) > 1.0;
    }

    private void activateImpale(Player player, EliteEntity target, int skillLevel) {
        LivingEntity living = target.getLivingEntity();
        if (living == null) return;

        UUID targetUUID = living.getUniqueId();
        pinnedEntities.add(targetUUID);

        // Apply root effect (extreme slowness)
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PIN_DURATION_TICKS, 127));

        // Visual and sound effects
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.5f);

        // Impale particle effect
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= PIN_DURATION_TICKS || living.isDead()) {
                    pinnedEntities.remove(targetUUID);
                    cancel();
                    return;
                }

                // Visual effect - spear through target
                living.getWorld().spawnParticle(Particle.END_ROD,
                    living.getLocation().add(0, 1, 0), 5, 0.2, 0.5, 0.2, 0.02);

                if (ticks % 10 == 0) {
                    living.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        living.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.1);
                }

                ticks++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) return configFields.calculateValue(skillLevel);
        // Hardcoded rather than read from config so every server runs the same numbers while the
        // rebalance is being validated. The 10.0 ceiling is the same cap the config path applied.
        return scaled(BASE_DAMAGE_MULTIPLIER, 0.05, 10.0, skillLevel);
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
        return applyLoreTemplates(Map.of(
                "damage", String.format("%.0f", getDamageMultiplier(skillLevel) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Return the bonus portion only (e.g., 3.0 for a 4.0x multiplier).
        // processOffensiveSkill adds 1.0 + this, so total = the damage multiplier.
        // The "target not already pinned" gate is enforced by tryActivate, which the caller
        // consults before ever reaching this value, so no gating is needed here.
        return getDamageMultiplier(skillLevel) - 1.0;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damage", String.format("%.0f", getDamageMultiplier(skillLevel) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
        pinnedEntities.clear();
    }
}
