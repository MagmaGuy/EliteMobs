package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Death Mark (PROC) - Attacks have a chance to mark enemies for death.
 * Marked enemies glow and take increased damage.
 * Tier 2 unlock.
 */
public class DeathMarkSkill extends SkillBonus implements ProcSkill, TargetDebuffBonus {

    public static final String SKILL_ID = "hoes_death_mark";
    private static final double BASE_PROC_CHANCE = 0.17; // 17% chance
    private static final long MARK_DURATION = 15000; // 15 seconds
    private static final double BASE_DAMAGE_BONUS = 0.11; // 11% extra damage

    private static final Map<UUID, UUID> markedTargets = new ConcurrentHashMap<>(); // EntityUUID -> PlayerUUID
    private static final Map<UUID, Long> markExpiry = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public DeathMarkSkill() {
        super(SkillType.HOES, 25, "Death Mark",
              "Mark enemies for death, causing them to take bonus damage.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        // Base chance + 0.2% per level, ~27% at level 50
        return scaled(BASE_PROC_CHANCE, 0.002, 0.40, skillLevel);
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

    /**
     * Power budget: the trigger rate for a mark is the mark's <em>uptime</em>, not the proc chance.
     * The old 1.75x number was priced as if only 26.7% of hits landed on a marked target, but a
     * 15 second mark refreshed by a 27% proc at hoe attack speed (4 swings/sec, ~60 rolls per mark
     * window) is up on essentially every hit of a sustained fight. Allowing ~5% downtime for target
     * switching and approach, the honest trigger rate is 0.95, so the level 50 bonus is +21%
     * (E = 0.95 * 0.21 = 0.20). Hardcoded rather than read from config so every server runs the
     * same numbers while the rebalance is being validated.
     */
    public double getMarkDamageBonus(int skillLevel) {
        if (configFields != null) return configFields.calculateValue(skillLevel);
        return scaled(BASE_DAMAGE_BONUS, 0.002, skillLevel); // 11% base + 0.2% per level
    }

    @Override
    public boolean appliesTo(LivingEntity target, Player attacker) {
        return isMarkedForDeath(target, attacker);
    }

    @Override
    public SkillType levelSource() {
        return SkillType.HOES;
    }

    @Override
    public double bonusFor(Player attacker, LivingEntity target, int level) {
        return getMarkDamageBonus(level);
    }

    @Override
    public String debugLabel() {
        return "DeathMark=";
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
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "damageBonus", String.format("%.0f", getMarkDamageBonus(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Bonus fraction applied to marked targets, not to the hit that applies the mark - see affectsDamage().
        return getMarkDamageBonus(skillLevel);
    }

    @Override
    public boolean affectsDamage() {
        return false; // Proc applies the mark, bonus checked separately on marked targets
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damageBonus", String.format("%.0f", getMarkDamageBonus(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        markedTargets.clear();
        markExpiry.clear();
        activePlayers.clear();
    }
}
