package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Lacerate (PROC) - Attacks have a chance to cause bleeding.
 * Bleeding deals damage over time based on skill level.
 * Tier 1 unlock.
 */
public class LacerateSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "swords_lacerate";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% base chance
    private static final double BASE_BLEED_DAMAGE = 2.0;
    private static final int BLEED_DURATION_TICKS = 100; // 5 seconds
    private static final int BLEED_TICK_INTERVAL = 20; // Every second

    // Track active bleed effects: EntityUUID -> BukkitRunnable
    private static final Map<UUID, BukkitRunnable> activeBleedEffects = new HashMap<>();
    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public LacerateSkill() {
        super(SkillType.SWORDS, 10, "Lacerate",
              "Attacks have a chance to cause bleeding, dealing damage over time.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.2% per level
        return Math.min(0.5, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        int skillLevel = getPlayerSkillLevel(player);
        double bleedDamage = calculateBleedDamage(skillLevel);

        applyBleedEffect(player, eliteEntity, bleedDamage);
    }

    /**
     * Applies a bleed effect to the target entity.
     */
    private void applyBleedEffect(Player player, EliteEntity target, double damagePerTick) {
        UUID entityUUID = target.getLivingEntity().getUniqueId();

        // Cancel existing bleed on this target
        if (activeBleedEffects.containsKey(entityUUID)) {
            activeBleedEffects.get(entityUUID).cancel();
        }

        // Apply new bleed
        BukkitRunnable bleedTask = new BukkitRunnable() {
            int ticksRemaining = BLEED_DURATION_TICKS;

            @Override
            public void run() {
                if (ticksRemaining <= 0 || target.getLivingEntity() == null || target.getLivingEntity().isDead()) {
                    activeBleedEffects.remove(entityUUID);
                    this.cancel();
                    return;
                }

                // Deal bleed damage
                LivingEntity entity = target.getLivingEntity();
                if (entity != null && !entity.isDead()) {
                    // Use bypass to prevent recursive skill processing
                    EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
                    try {
                        entity.damage(damagePerTick, player);
                    } finally {
                        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
                    }
                    // Visual effect - red particles
                    entity.getWorld().spawnParticle(org.bukkit.Particle.BLOCK,
                            entity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0,
                            org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
                }

                ticksRemaining -= BLEED_TICK_INTERVAL;
            }
        };

        bleedTask.runTaskTimer(MetadataHandler.PLUGIN, 0, BLEED_TICK_INTERVAL);
        activeBleedEffects.put(entityUUID, bleedTask);
    }

    /**
     * Calculates bleed damage per tick based on skill level.
     */
    private double calculateBleedDamage(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_BLEED_DAMAGE + (skillLevel * 0.1);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
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
        double procChance = getProcChance(skillLevel) * 100;
        double damage = calculateBleedDamage(skillLevel);
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Bleed Damage: &f" + String.format("%.1f", damage) + "/s",
                "&7Duration: &f5 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateBleedDamage(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f Bleed Damage/s", calculateBleedDamage(skillLevel));
    }

    @Override
    public boolean affectsDamage() {
        return false; // DoT skill - applies bleed via onProc, doesn't multiply main hit damage
    }

    @Override
    public void shutdown() {
        for (BukkitRunnable task : activeBleedEffects.values()) {
            task.cancel();
        }
        activeBleedEffects.clear();
        activePlayers.clear();
    }
}
