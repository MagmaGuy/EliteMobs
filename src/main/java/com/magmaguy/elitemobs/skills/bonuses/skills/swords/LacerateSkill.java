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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final Map<UUID, BukkitRunnable> activeBleedEffects = new ConcurrentHashMap<>();
    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

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
        // Bleed damage = % of the hit damage, not flat. Scales properly at all levels.
        double bleedDamagePerTick = event.getDamage() * getBleedPercent(skillLevel);

        applyBleedEffect(player, eliteEntity, bleedDamagePerTick);
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
     * Gets the bleed damage per tick as a fraction of hit damage.
     * Each tick does a small % of the original hit, so total DoT = percent * 5 ticks.
     */
    private double getBleedPercent(int skillLevel) {
        // 8% per tick base (40% total over 5s), +0.1% per level
        return 0.08 + (skillLevel * 0.001);
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
        double bleedPercent = getBleedPercent(skillLevel) * 100;
        double totalPercent = bleedPercent * 5;
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", procChance),
                "bleedPercent", String.format("%.0f", bleedPercent),
                "totalPercent", String.format("%.0f", totalPercent)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBleedPercent(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("totalPercent", String.format("%.0f", getBleedPercent(skillLevel) * 500)));
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
