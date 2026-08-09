package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

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
 * Judgment (PROC) - Mark enemies for judgment, causing them to take bonus damage.
 * Tier 2 unlock.
 */
public class JudgmentSkill extends SkillBonus implements ProcSkill, TargetDebuffBonus {

    public static final String SKILL_ID = "maces_judgment";
    private static final double BASE_PROC_CHANCE = 0.17;
    private static final long MARK_DURATION = 10000; // 10 seconds
    private static final double BASE_DAMAGE_BONUS = 0.14; // 14% extra damage

    private static final Map<UUID, UUID> judgedTargets = new ConcurrentHashMap<>(); // EntityUUID -> PlayerUUID
    private static final Map<UUID, Long> markExpiry = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public JudgmentSkill() {
        super(SkillType.MACES, 25, "Judgment",
              "Mark enemies for judgment, causing them to take bonus damage.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        // ~27% at level 50
        return scaled(BASE_PROC_CHANCE, 0.002, 0.40, skillLevel);
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        UUID entityUUID = target.getUniqueId();

        // Apply judgment mark
        judgedTargets.put(entityUUID, player.getUniqueId());
        markExpiry.put(entityUUID, System.currentTimeMillis() + MARK_DURATION);

        // Visual effects - golden glow
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int)(MARK_DURATION / 50), 0));
        target.getWorld().spawnParticle(Particle.END_ROD,
            target.getLocation().add(0, 1.5, 0), 20, 0.5, 0.5, 0.5, 0.05);
    }

    /**
     * Checks if a target is judged by this player.
     */
    public static boolean isJudged(LivingEntity target, Player player) {
        UUID entityUUID = target.getUniqueId();
        UUID judgerUUID = judgedTargets.get(entityUUID);

        if (judgerUUID == null) return false;

        // Check if mark has expired
        Long expiry = markExpiry.get(entityUUID);
        if (expiry == null || System.currentTimeMillis() > expiry) {
            judgedTargets.remove(entityUUID);
            markExpiry.remove(entityUUID);
            return false;
        }

        return judgerUUID.equals(player.getUniqueId());
    }

    /**
     * Power budget: the trigger rate for a mark is the mark's <em>uptime</em>, not the proc chance.
     * The old 1.75x number was priced as if only 26.7% of hits landed on a judged target. A 10
     * second mark refreshed by a 27% proc at mace attack speed (0.6 swings/sec, ~6 rolls per mark
     * window) is up 1 - 0.73^6 = 85% of the time in a sustained fight, so the level 50 bonus is
     * +24% (E = 0.85 * 0.24 = 0.20). Hardcoded rather than read from config so every server runs
     * the same numbers while the rebalance is being validated.
     */
    public double getJudgmentDamageBonus(int skillLevel) {
        if (configFields != null) return configFields.calculateValue(skillLevel);
        return scaled(BASE_DAMAGE_BONUS, 0.002, skillLevel); // 14% base + 0.2% per level
    }

    @Override
    public boolean appliesTo(LivingEntity target, Player attacker) {
        return isJudged(target, attacker);
    }

    @Override
    public SkillType levelSource() {
        return SkillType.MACES;
    }

    @Override
    public double bonusFor(Player attacker, LivingEntity target, int level) {
        return getJudgmentDamageBonus(level);
    }

    @Override
    public String debugLabel() {
        return "Judgment=";
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
        judgedTargets.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
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
                "damageBonus", String.format("%.0f", getJudgmentDamageBonus(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Bonus fraction applied to judged targets, not to the hit that applies the judgment - see affectsDamage().
        return getJudgmentDamageBonus(skillLevel);
    }

    @Override
    public boolean affectsDamage() {
        return false; // Proc applies the judgment, bonus checked separately on judged targets
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damageBonus", String.format("%.0f", getJudgmentDamageBonus(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        judgedTargets.clear();
        markExpiry.clear();
        activePlayers.clear();
    }
}
