package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Arrow Rain (COOLDOWN) - Rain arrows on target location.
 * Tier 4 unlock.
 */
public class ArrowRainSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "crossbows_arrow_rain";
    private static final long BASE_COOLDOWN = 30; // 30 seconds
    private static final double BASE_ARROW_DAMAGE = 0.12; // 12% of original, per arrow

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownEndTimes = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitRunnable> volleys = new ConcurrentHashMap<>();

    public ArrowRainSkill() {
        super(SkillType.CROSSBOWS, 75, "Arrow Rain",
              "Rain arrows on the target location.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        return Math.max(15, BASE_COOLDOWN - (skillLevel / 5)); // 30s base, min 15s
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return getRemainingCooldown(player) > 0L;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        UUID uuid = player.getUniqueId();
        cooldownEndTimes.put(uuid, GameClock.getCurrentTick() + getCooldownSeconds(skillLevel) * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long endTime = cooldownEndTimes.get(player.getUniqueId());
        if (endTime == null) return 0;
        long remaining = endTime - GameClock.getCurrentTick();
        if (remaining <= 0L) cooldownEndTimes.remove(player.getUniqueId(), endTime);
        return Math.max(0L, (remaining + 19L) / 20L);
    }

    @Override
    public void endCooldown(Player player) {
        cooldownEndTimes.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (CombatSimulator.isTestingActive(player)) return;
        if (!isActive(player) || isOnCooldown(player)) return;
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;
        if (damageEvent.getEliteMobEntity().getLivingEntity() == null) return;
        if (damageEvent.getEntityDamageByEntityEvent() == null
                || !(damageEvent.getEntityDamageByEntityEvent().getDamager() instanceof org.bukkit.entity.Projectile origin)) return;
        if (ItemTagger.isSecondaryArrow(origin)) return;

        int skillLevel = damageEvent.getRangedSkillLevel();
        double damageMultiplier = getArrowDamageMultiplier(skillLevel);
        Location targetLoc = damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0, 10, 0);

        ItemTagger.ArrowCombatSnapshot attack = ItemTagger.snapshotArrowCombat(origin);
        stopVolley(player.getUniqueId());
        startCooldown(player, skillLevel);
        BukkitRunnable volley = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (volleys.get(player.getUniqueId()) != this || count >= 5
                        || !player.isOnline() || !isActive(player)
                        || !player.getWorld().equals(targetLoc.getWorld())) {
                    volleys.remove(player.getUniqueId(), this);
                    cancel();
                    return;
                }
                try {
                for (int i = 0; i < 3; i++) {
                    Location spawnLoc = targetLoc.clone().add(
                            ThreadLocalRandom.current().nextDouble(-2, 2),
                            0,
                            ThreadLocalRandom.current().nextDouble(-2, 2)
                    );
                    Arrow arrow = targetLoc.getWorld().spawn(spawnLoc, Arrow.class);
                    arrow.setShooter(player);
                    arrow.setVelocity(new Vector(0, -2, 0));
                    arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

                    attack.applyToSecondary(arrow, damageMultiplier);
                }
                count++;
                } catch (RuntimeException | Error failure) {
                    volleys.remove(player.getUniqueId(), this);
                    cancel();
                    throw failure;
                }
            }
        };
        try {
            volley.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
            volleys.put(player.getUniqueId(), volley);
        } catch (RuntimeException failure) {
            endCooldown(player);
            throw failure;
        }
    }

    /**
     * Power budget: the volley is 15 arrows, so the payload at level 50 is 15 x 0.21 = 3.2 hits
     * worth of damage on a 16s cooldown (E = 0.062 * 3.2 = 0.20). The old 55% per arrow was
     * 8.25 hits worth per volley, far over budget.
     */
    private double getArrowDamageMultiplier(int skillLevel) {
        return scaled(BASE_ARROW_DAMAGE, 0.0018, 0.30, skillLevel); // 12% base + 0.18% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        stopVolley(player.getUniqueId());
        endCooldown(player);
    }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "arrowDamage", String.format("%.0f", getArrowDamageMultiplier(skillLevel) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    // Fraction of the hit dealt by each rained arrow, not a bonus to the main hit - see affectsDamage()
    public double getBonusValue(int skillLevel) { return getArrowDamageMultiplier(skillLevel); }
    @Override
    public boolean affectsDamage() { return false; } // The arrow volley deals its own damage, not the main hit
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("arrowDamage", String.format("%.0f", getArrowDamageMultiplier(skillLevel) * 100))); }
    @Override
    public void shutdown() {
        activePlayers.clear();
        volleys.values().forEach(BukkitRunnable::cancel);
        volleys.clear();
        cooldownEndTimes.clear();
    }

    private static void stopVolley(UUID playerId) {
        BukkitRunnable volley = volleys.remove(playerId);
        if (volley != null) volley.cancel();
    }
}
