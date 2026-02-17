package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Leviathan's Wrath (COOLDOWN) - Massive AOE water attack with lightning strikes.
 * Deals massive damage to all nearby enemies.
 * Tier 4 unlock.
 */
public class LeviathanWrathSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "tridents_leviathan_wrath";
    private static final long BASE_COOLDOWN = 60; // 60 seconds
    private static final double AOE_RADIUS = 6.0;
    private static final double BASE_DAMAGE_MULTIPLIER = 2.0;

    // Track cooldowns: PlayerUUID -> Cooldown end time
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public LeviathanWrathSkill() {
        super(SkillType.TRIDENTS, 75, "Leviathan's Wrath",
              "Massive AOE water attack with lightning strikes. Deals massive damage to all nearby enemies.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Cooldown reduces by 0.2s per level, minimum 30 seconds
        return Math.max(30, BASE_COOLDOWN - (skillLevel / 5));
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long endTime = cooldownMap.get(player.getUniqueId());
        if (endTime == null) return false;
        if (System.currentTimeMillis() >= endTime) {
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
        Long endTime = cooldownMap.get(player.getUniqueId());
        if (endTime == null) return 0;
        long remaining = (endTime - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;
        if (isOnCooldown(player)) return;

        EliteEntity eliteEntity = damageEvent.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = getPlayerSkillLevel(player);

        startCooldown(player, skillLevel);

        // Leviathan's Wrath: Massive AOE water attack
        target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation(), 200, 3, 2, 3, 0.5);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.5f);

        // Strike lightning at multiple points (suppress during testing to avoid entity spam)
        if (!CombatSimulator.isTestingActive()) {
            for (int i = 0; i < 3; i++) {
                target.getWorld().strikeLightningEffect(target.getLocation().add(
                        (Math.random() - 0.5) * AOE_RADIUS,
                        0,
                        (Math.random() - 0.5) * AOE_RADIUS
                ));
            }
        }

        // Massive damage to all nearby enemies
        // Use bypass to prevent recursive skill processing
        double aoeDamage = damageEvent.getDamage() * calculateDamageMultiplier(skillLevel);
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.getNearbyEntities(AOE_RADIUS, AOE_RADIUS, AOE_RADIUS).stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                    .forEach(e -> ((LivingEntity) e).damage(aoeDamage, player));
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }
    }

    private double calculateDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return BASE_DAMAGE_MULTIPLIER + configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        long cooldown = getCooldownSeconds(skillLevel);
        double damageMultiplier = calculateDamageMultiplier(skillLevel);
        return applyLoreTemplates(Map.of(
                "cooldown", String.valueOf(cooldown),
                "multiplier", String.format("%.1f", damageMultiplier),
                "radius", String.valueOf(AOE_RADIUS)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "multiplier", String.format("%.1f", calculateDamageMultiplier(skillLevel))
        ));
    }

    @Override
    public void shutdown() {
        cooldownMap.clear();
        activePlayers.clear();
    }
}
