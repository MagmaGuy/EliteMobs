package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;
import com.magmaguy.elitemobs.MetadataHandler;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 3 ARMOR skill - Second Wind
 * Automatically heals when health drops below threshold
 */
public class SecondWindSkill extends SkillBonus implements CooldownSkill {

    /**
     * Share of max health restored when the skill fires.
     * <p>
     * Second Wind restores health rather than reducing a hit, so it carries no reduction term and
     * sits outside the {@code E = uptime * reduction} defensive budget. Pinned to the value the old
     * config scaling produced at the reference skill level of 50, so nothing changes at that level —
     * this removes balance config from the skill and stops the heal growing past 100% of max health
     * at very high levels.
     */
    private static final double HEAL_PERCENT = 0.40;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    private final Map<UUID, PendingHit> pendingHits = new HashMap<>();
    private static final double HEALTH_THRESHOLD = 0.25; // 25% health

    public SecondWindSkill() {
        super(
            SkillType.ARMOR,
            50,
            "Second Wind",
            "Automatically heal when health drops below 25%",
            SkillBonusType.COOLDOWN,
            3,
            "armor_second_wind"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        onDeactivate(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        cancelPending(player);
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
                "healAmount", String.format("%.1f", getHealPercent(skillLevel) * 100),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getHealPercent(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "healAmount", String.format("%.1f", getHealPercent(skillLevel) * 100),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public void shutdown() {
        pendingHits.values().forEach(pending -> pending.task.cancel());
        pendingHits.clear();
        activePlayers.clear();
        cooldownMap.clear();
    }

    // CooldownSkill interface methods

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        // Base 60 seconds cooldown
        return 60;
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
        if (event instanceof EntityDamageEvent hit) afterHit(player, hit);
    }

    /** Second Wind heals surviving players after accepted native damage has changed health. */
    public void afterHit(Player player, EntityDamageEvent event) {
        if (!isActive(player) || isOnCooldown(player)) return;
        PendingHit existing = pendingHits.get(player.getUniqueId());
        if (existing != null) {
            existing.hits.add(new Hit(event, player.getHealth()));
            return;
        }
        PendingHit pending = new PendingHit(player, event);
        pendingHits.put(player.getUniqueId(), pending);
        try {
            pending.task = player.getServer().getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                if (!pendingHits.remove(player.getUniqueId(), pending)) return;
                if (!player.isOnline() || player.isDead() || !isActive(player) || isOnCooldown(player)
                        || !player.getWorld().getUID().equals(pending.worldId)) return;
                double health = player.getHealth();
                if (health <= 0 || health / player.getMaxHealth() > HEALTH_THRESHOLD) return;
                boolean accepted = pending.hits.stream().anyMatch(hit -> !hit.event.isCancelled()
                        && hit.event.getFinalDamage() > 0 && health < hit.healthBefore);
                if (!accepted) return;
                int skillLevel = getPlayerSkillLevel(player);
                double healed = Math.min(player.getMaxHealth(), health + player.getMaxHealth() * getHealPercent(skillLevel));
                if (healed <= health) return;
                player.setHealth(healed);
                startCooldown(player, skillLevel);
                incrementProcCount(player);
                SkillBonus.sendSkillActionBar(player, this);
                player.getWorld().spawnParticle(Particle.HEART,
                        player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            });
        } catch (RuntimeException failure) {
            pendingHits.remove(player.getUniqueId(), pending);
            throw failure;
        }
    }

    public void cancelPending(Player player) {
        PendingHit pending = pendingHits.remove(player.getUniqueId());
        if (pending != null && pending.task != null) pending.task.cancel();
    }

    private record Hit(EntityDamageEvent event, double healthBefore) {}
    private static final class PendingHit {
        final UUID worldId;
        final List<Hit> hits = new ArrayList<>();
        BukkitTask task;
        PendingHit(Player player, EntityDamageEvent event) {
            worldId = player.getWorld().getUID();
            hits.add(new Hit(event, player.getHealth()));
        }
    }

    /**
     * Gets the heal percentage based on skill level.
     *
     * @param skillLevel The player's skill level
     * @return The heal percentage (0.0 to 1.0)
     */
    private double getHealPercent(int skillLevel) {
        if (configFields == null) return HEAL_PERCENT;
        return Math.min(1.0, Math.max(0.0, 0.20 + configFields.calculateValue(skillLevel) * 0.10));
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
