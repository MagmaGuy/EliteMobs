package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Impaling Strike (PROC) - Chance to cause bleed damage over time.
 * Tier 2 unlock.
 */
public class ImpalingStrikeSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "spears_impaling_strike";
    private static final double BASE_PROC_CHANCE = 0.17;
    private static final int BLEED_DURATION_TICKS = 100; // 5 seconds
    private static final int BLEED_TICK_INTERVAL = 20; // Damage every second
    private static final double BASE_BLEED_DAMAGE = 0.08; // 8% of initial hit per tick

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, BukkitRunnable> activeBleedTasks = new ConcurrentHashMap<>();

    public ImpalingStrikeSkill() {
        super(SkillType.SPEARS, 25, "Impaling Strike",
              "Chance to cause bleed damage over time.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        // ~27% at level 50
        return scaled(BASE_PROC_CHANCE, 0.002, 0.35, skillLevel);
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double baseDamage = event.getDamageWithoutCriticalStrike();
        double bleedDamage = baseDamage * getBleedDamagePercent(skillLevel);
        // Cap bleed per tick to 15% of hit damage to prevent insane DoT
        bleedDamage = Math.min(bleedDamage, baseDamage * 0.15);

        applyBleed(player, eliteEntity, bleedDamage);
    }

    private void applyBleed(Player player, EliteEntity target, double damagePerTick) {
        LivingEntity livingTarget = target.getLivingEntity();
        if (livingTarget == null) return;

        UUID targetUUID = livingTarget.getUniqueId();

        // Cancel existing bleed on this target
        BukkitRunnable existingTask = activeBleedTasks.remove(targetUUID);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Start new bleed effect
        BukkitRunnable bleedTask = new BukkitRunnable() {
            int ticksRemaining = BLEED_DURATION_TICKS;

            @Override
            public void run() {
                if (ticksRemaining <= 0 || target.getLivingEntity() == null ||
                    target.getLivingEntity().isDead()) {
                    activeBleedTasks.remove(targetUUID);
                    cancel();
                    return;
                }

                // Apply bleed damage every tick interval
                if (ticksRemaining % BLEED_TICK_INTERVAL == 0) {
                    // Bypass the player→elite formula so the flat bleed tick lands as-is
                    // (mirrors LacerateSkill) instead of being re-scaled by the formula
                    // and counted as a click by the autoclicker throttle.
                    CombatDamageContext.runPlayerToEliteBypass(
                            () -> livingTarget.damage(damagePerTick, player));

                    // Bleed particle effect
                    LivingEntity living = target.getLivingEntity();
                    if (living != null) {
                        living.getWorld().spawnParticle(Particle.BLOCK,
                            living.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0,
                            org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
                    }
                }

                ticksRemaining--;
            }
        };

        bleedTask.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        activeBleedTasks.put(targetUUID, bleedTask);
    }

    public double getBleedDamagePercent(int skillLevel) {
        // Power budget: 5 bleed ticks at 15% of the hit each is 75% of a hit at level 50, on a
        // 27% proc rate (E = 0.267 * 0.75 = 0.20). Hardcoded rather than read from config so
        // every server runs the same numbers while the rebalance is being validated.
        return scaled(BASE_BLEED_DAMAGE, 0.0014, skillLevel); // 8% base + 0.14% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "chance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "bleedDamage", String.format("%.0f", getBleedDamagePercent(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Fraction of the hit dealt per bleed tick, not a bonus to the main hit - see affectsDamage().
        return getBleedDamagePercent(skillLevel);
    }

    @Override
    public boolean affectsDamage() {
        return false; // DoT skill - applies bleed via onProc, doesn't multiply main hit damage
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "totalBleed", String.format("%.0f", getBleedDamagePercent(skillLevel) * 100 * 5)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        for (BukkitRunnable task : activeBleedTasks.values()) {
            task.cancel();
        }
        activeBleedTasks.clear();
    }
}
