package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Expose Weakness (PROC) - Attacks have a chance to reduce target's defense.
 * Debuff stacks and lasts for a short duration.
 * Tier 2 unlock.
 */
public class ExposeWeaknessSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "swords_expose_weakness";
    private static final double BASE_PROC_CHANCE = 0.20; // 20% chance
    private static final double BASE_DEFENSE_REDUCTION = 0.10; // 10% defense reduction
    private static final int DEBUFF_DURATION_TICKS = 100; // 5 seconds

    // Track debuffed entities: EntityUUID -> (debuff stacks, expiry runnable)
    private static final Map<UUID, DebuffData> debuffedEntities = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public ExposeWeaknessSkill() {
        super(SkillType.SWORDS, 25, "Expose Weakness",
              "Attacks have a chance to reduce the target's defense.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base 20% + 0.3% per level
        return Math.min(0.50, BASE_PROC_CHANCE + (skillLevel * 0.003));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        applyDebuff(target, skillLevel);
    }

    /**
     * Applies or refreshes the defense reduction debuff.
     */
    private void applyDebuff(EliteEntity target, int skillLevel) {
        UUID entityUUID = target.getLivingEntity().getUniqueId();

        DebuffData existing = debuffedEntities.get(entityUUID);
        if (existing != null) {
            existing.cancel();
        }

        double reduction = getDefenseReduction(skillLevel);
        BukkitRunnable expiryTask = new BukkitRunnable() {
            @Override
            public void run() {
                debuffedEntities.remove(entityUUID);
            }
        };
        expiryTask.runTaskLater(MetadataHandler.PLUGIN, DEBUFF_DURATION_TICKS);

        debuffedEntities.put(entityUUID, new DebuffData(reduction, expiryTask));

        // Visual indicator
        if (target.getLivingEntity() != null) {
            target.getLivingEntity().getWorld().spawnParticle(
                    org.bukkit.Particle.ENCHANT,
                    target.getLivingEntity().getLocation().add(0, 1, 0),
                    10, 0.5, 0.5, 0.5, 0
            );
        }
    }

    /**
     * Gets the damage multiplier for an entity with this debuff.
     * Returns 1.0 if no debuff, or > 1.0 if debuffed.
     */
    public static double getDamageMultiplier(UUID entityUUID) {
        DebuffData data = debuffedEntities.get(entityUUID);
        if (data == null) return 1.0;
        return 1.0 + data.defenseReduction;
    }

    /**
     * Checks if an entity is debuffed.
     */
    public static boolean isDebuffed(UUID entityUUID) {
        return debuffedEntities.containsKey(entityUUID);
    }

    private double getDefenseReduction(int skillLevel) {
        // Base 10% + 0.2% per level
        return BASE_DEFENSE_REDUCTION + (skillLevel * 0.002);
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
        double proc = getProcChance(skillLevel) * 100;
        double reduction = getDefenseReduction(skillLevel) * 100;
        return List.of(
                "&7Proc Chance: &f" + String.format("%.1f", proc) + "%",
                "&7Defense Reduction: &f" + String.format("%.1f", reduction) + "%",
                "&7Duration: &f5 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDefenseReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("-%.1f%% Target Defense", getDefenseReduction(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        for (DebuffData data : debuffedEntities.values()) {
            data.cancel();
        }
        debuffedEntities.clear();
        activePlayers.clear();
    }

    private static class DebuffData {
        final double defenseReduction;
        final BukkitRunnable expiryTask;

        DebuffData(double defenseReduction, BukkitRunnable expiryTask) {
            this.defenseReduction = defenseReduction;
            this.expiryTask = expiryTask;
        }

        void cancel() {
            if (expiryTask != null) {
                expiryTask.cancel();
            }
        }
    }
}
