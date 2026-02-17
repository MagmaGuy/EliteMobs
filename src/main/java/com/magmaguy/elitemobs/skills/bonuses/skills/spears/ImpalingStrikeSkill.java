package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
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
    private static final double BASE_PROC_CHANCE = 0.15;
    private static final int BLEED_DURATION_TICKS = 100; // 5 seconds
    private static final int BLEED_TICK_INTERVAL = 20; // Damage every second
    private static final double BASE_BLEED_DAMAGE = 0.10; // 10% of initial hit per tick (50% total over 5s)

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, BukkitRunnable> activeBleedTasks = new ConcurrentHashMap<>();

    public ImpalingStrikeSkill() {
        super(SkillType.SPEARS, 25, "Impaling Strike",
              "Chance to cause bleed damage over time.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.003));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double bleedDamage = event.getDamage() * getBleedDamagePercent(skillLevel);
        // Cap bleed per tick to 15% of hit damage to prevent insane DoT
        bleedDamage = Math.min(bleedDamage, event.getDamage() * 0.15);

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
                    livingTarget.damage(damagePerTick, player);

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
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_BLEED_DAMAGE + (skillLevel * 0.01);
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
        return getBleedDamagePercent(skillLevel);
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
