package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Death Mark (PROC) - Attacks have a chance to mark enemies for death.
 * Marked enemies glow and take increased damage.
 * Tier 2 unlock.
 */
public class DeathMarkSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "hoes_death_mark";
    private static final double BASE_PROC_CHANCE = 0.25; // 25% chance
    private static final long MARK_DURATION = 15000; // 15 seconds
    private static final double BASE_DAMAGE_BONUS = 0.50; // 50% extra damage

    private static final Map<UUID, UUID> markedTargets = new HashMap<>(); // EntityUUID -> PlayerUUID
    private static final Map<UUID, Long> markExpiry = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public DeathMarkSkill() {
        super(SkillType.HOES, 25, "Death Mark",
              "Mark enemies for death, causing them to take bonus damage.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.3% per level
        return Math.min(0.5, BASE_PROC_CHANCE + (skillLevel * 0.003));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        UUID entityUUID = target.getUniqueId();

        // Apply mark
        markedTargets.put(entityUUID, player.getUniqueId());
        markExpiry.put(entityUUID, System.currentTimeMillis() + MARK_DURATION);

        // Visual effects
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int)(MARK_DURATION / 50), 0));
        target.getWorld().spawnParticle(Particle.SCULK_SOUL,
            target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
    }

    /**
     * Checks if a target is marked for death by this player.
     */
    public static boolean isMarkedForDeath(LivingEntity target, Player player) {
        UUID entityUUID = target.getUniqueId();
        UUID markerUUID = markedTargets.get(entityUUID);

        if (markerUUID == null) return false;

        // Check if mark has expired
        Long expiry = markExpiry.get(entityUUID);
        if (expiry == null || System.currentTimeMillis() > expiry) {
            markedTargets.remove(entityUUID);
            markExpiry.remove(entityUUID);
            return false;
        }

        return markerUUID.equals(player.getUniqueId());
    }

    public double getMarkDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 50% + 1% per level
        return BASE_DAMAGE_BONUS + (skillLevel * 0.01);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        // Clean up marks created by this player
        UUID playerUUID = player.getUniqueId();
        markedTargets.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double procChance = getProcChance(skillLevel) * 100;
        double damageBonus = getMarkDamageBonus(skillLevel) * 100;
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Mark Damage: &f+" + String.format("%.0f", damageBonus) + "%",
                "&7Duration: &f15 seconds",
                "&7Marked enemies glow"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getMarkDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% vs Marked", getMarkDamageBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        markedTargets.clear();
        markExpiry.clear();
        activePlayers.clear();
    }
}
