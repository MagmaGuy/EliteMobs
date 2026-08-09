package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dead Eye (COOLDOWN) - Massive damage on critical shots.
 * Tier 4 unlock.
 */
public class DeadEyeSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "bows_dead_eye";
    private static final long BASE_COOLDOWN = 30; // 30 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 3.3; // 330% damage

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> onCooldown = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownEndTimes = new ConcurrentHashMap<>();

    public DeadEyeSkill() {
        super(SkillType.BOWS, 75, "Dead Eye",
              "Critical shots deal massive bonus damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        return Math.max(15, BASE_COOLDOWN - (skillLevel / 5)); // 30s base, 20s at level 50, min 15s
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return onCooldown.contains(player.getUniqueId());
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        UUID uuid = player.getUniqueId();
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000L;
        onCooldown.add(uuid);
        cooldownEndTimes.put(uuid, System.currentTimeMillis() + cooldownMs);

        new BukkitRunnable() {
            @Override
            public void run() {
                endCooldown(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, getCooldownSeconds(skillLevel) * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long endTime = cooldownEndTimes.get(player.getUniqueId());
        if (endTime == null) return 0;
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    @Override
    public void endCooldown(Player player) {
        onCooldown.remove(player.getUniqueId());
        cooldownEndTimes.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;

        // Visual effect only - damage is applied via getBonusValue in processOffensiveSkill
        if (damageEvent.getEliteMobEntity().getLivingEntity() != null) {
            damageEvent.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                    Particle.ENCHANTED_HIT, damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.2);
        }
    }

    /**
     * Dead Eye is advertised - in its name, its description and its lore - as a critical-shot
     * payoff, but it never implemented the gate. Relying on the default {@code tryActivate} meant
     * the first arrow off cooldown always fired it, crit or not.
     *
     * @return true only on a critical strike, so a non-crit hit consumes neither the cooldown nor
     * the damage bonus.
     */
    @Override
    public boolean tryActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return false;
        if (!damageEvent.isCriticalStrike()) return false;
        onActivate(player, event);
        return true;
    }

    /**
     * Power budget: the 20s cooldown at level 50 is a 1-in-20-shot trigger on its own, but the
     * critical-hit gate above adds an expected wait of 1/critChance further shots before the skill
     * can fire. At the ~25% crit chance a geared archer runs (capped at 40% by
     * maximumCriticalStrikeChance) that is ~4 extra shots, so the real period is ~24 shots rather
     * than 20. The payload rises from 5.0x to 5.8x to hold the same budget
     * (E = 1/24 * 4.80 = 0.20).
     */
    private double getDamageMultiplier(int skillLevel) {
        return scaled(BASE_DAMAGE_MULTIPLIER, 0.05, skillLevel); // 330% base + 5% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "damageMultiplier", String.format("%.1f", getDamageMultiplier(skillLevel)),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getDamageMultiplier(skillLevel) - 1.0; }
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damageMultiplier", String.format("%.1f", getDamageMultiplier(skillLevel))
        ));
    }
    @Override
    public void shutdown() {
        activePlayers.clear();
        onCooldown.clear();
        cooldownEndTimes.clear();
    }
}
