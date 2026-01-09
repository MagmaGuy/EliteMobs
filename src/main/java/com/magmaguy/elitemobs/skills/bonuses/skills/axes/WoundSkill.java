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

import java.util.*;

/**
 * Wound (PROC) - Chance to inflict a deep wound that bleeds heavily.
 * Tier 1 unlock.
 */
public class WoundSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "axes_wound";
    private static final double BASE_PROC_CHANCE = 0.15;
    private static final double BASE_BLEED_DAMAGE = 3.0;
    private static final int BLEED_DURATION = 80; // 4 seconds

    private static final Map<UUID, BukkitRunnable> activeWounds = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

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
        applyWound(player, target, skillLevel);
    }

    private void applyWound(Player player, EliteEntity target, int skillLevel) {
        UUID entityUUID = target.getLivingEntity().getUniqueId();
        double damage = BASE_BLEED_DAMAGE + (skillLevel * 0.15);

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
                    entity.damage(damage, player);
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
        return List.of(
                "&7Proc Chance: &f" + String.format("%.1f", getProcChance(skillLevel) * 100) + "%",
                "&7Bleed Damage: &f" + String.format("%.1f", BASE_BLEED_DAMAGE + (skillLevel * 0.15)) + "/s",
                "&7Duration: &f4 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return BASE_BLEED_DAMAGE + (skillLevel * 0.15); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.1f Wound Damage/s", getBonusValue(skillLevel)); }
    @Override
    public boolean affectsDamage() { return false; } // Proc applies DoT, doesn't multiply main hit damage

    @Override
    public void shutdown() {
        for (BukkitRunnable task : activeWounds.values()) task.cancel();
        activeWounds.clear();
        activePlayers.clear();
    }
}
