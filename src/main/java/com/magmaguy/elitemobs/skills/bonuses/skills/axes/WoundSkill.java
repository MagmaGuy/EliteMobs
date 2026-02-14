package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Material;
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
 * Wound (PROC) - Chance to inflict a deep wound that bleeds heavily.
 * Tier 1 unlock.
 */
public class WoundSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "axes_wound";
    private static final double BASE_PROC_CHANCE = 0.15;
    private static final double BASE_BLEED_DAMAGE = 3.0;
    private static final int BLEED_DURATION = 80; // 4 seconds

    private static final Map<UUID, BukkitRunnable> activeWounds = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public WoundSkill() {
        super(SkillType.AXES, 10, "Wound",
              "Attacks can inflict deep wounds that bleed heavily.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.40, BASE_PROC_CHANCE + (skillLevel * 0.003));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
        // Bleed damage = % of the hit damage, scales properly at all levels
        double bleedDamagePerTick = event.getDamage() * getBleedPercent(skillLevel);
        applyWound(player, target, bleedDamagePerTick);
    }

    private void applyWound(Player player, EliteEntity target, double damagePerTick) {
        UUID entityUUID = target.getLivingEntity().getUniqueId();

        if (activeWounds.containsKey(entityUUID)) {
            activeWounds.get(entityUUID).cancel();
        }

        BukkitRunnable woundTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= BLEED_DURATION || target.getLivingEntity() == null || target.getLivingEntity().isDead()) {
                    activeWounds.remove(entityUUID);
                    cancel();
                    return;
                }
                LivingEntity entity = target.getLivingEntity();
                // Use bypass to prevent recursive skill processing
                EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
                try {
                    entity.damage(damagePerTick, player);
                } finally {
                    EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
                }
                entity.getWorld().spawnParticle(Particle.BLOCK, entity.getLocation().add(0, 1, 0),
                        8, 0.3, 0.3, 0.3, 0, Material.REDSTONE_BLOCK.createBlockData());
                ticks += 20;
            }
        };
        woundTask.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
        activeWounds.put(entityUUID, woundTask);
    }

    /**
     * Gets the bleed damage per tick as a fraction of hit damage.
     * Each tick does a % of the original hit, total DoT = percent * 4 ticks.
     */
    private double getBleedPercent(int skillLevel) {
        // 10% per tick base (40% total over 4s), +0.15% per level
        return 0.10 + (skillLevel * 0.0015);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bleedPercent = getBleedPercent(skillLevel) * 100;
        double totalPercent = bleedPercent * 4;
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "bleedPercent", String.format("%.0f", bleedPercent),
                "totalPercent", String.format("%.0f", totalPercent)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getBleedPercent(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("totalPercent", String.format("%.0f", getBleedPercent(skillLevel) * 400))); }
    @Override
    public boolean affectsDamage() { return false; } // Proc applies DoT, doesn't multiply main hit damage

    @Override
    public void shutdown() {
        for (BukkitRunnable task : activeWounds.values()) task.cancel();
        activeWounds.clear();
        activePlayers.clear();
    }
}
