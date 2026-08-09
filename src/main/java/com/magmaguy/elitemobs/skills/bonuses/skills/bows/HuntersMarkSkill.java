package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
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
 * Hunter's Mark (PROC) - Mark enemies to take bonus damage.
 * Tier 2 unlock.
 */
public class HuntersMarkSkill extends SkillBonus implements ProcSkill, TargetDebuffBonus {

    public static final String SKILL_ID = "bows_hunters_mark";
    private static final double BASE_PROC_CHANCE = 0.25; // 25% chance
    private static final long MARK_DURATION = 10000; // 10 seconds
    private static final double BASE_BONUS_DAMAGE = 0.11; // 11% bonus to marked

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, UUID> markedTargets = new ConcurrentHashMap<>(); // Target UUID -> Marker UUID
    private static final Map<UUID, Long> markExpiry = new ConcurrentHashMap<>();

    public HuntersMarkSkill() {
        super(SkillType.BOWS, 25, "Hunter's Mark",
              "Mark enemies to take increased damage from you.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        return scaled(BASE_PROC_CHANCE, 0.0025, 0.50, skillLevel);
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;

        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        markedTargets.put(target.getUniqueId(), player.getUniqueId());
        markExpiry.put(target.getUniqueId(), System.currentTimeMillis() + MARK_DURATION);
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int)(MARK_DURATION / 50), 0));
    }

    public static boolean isMarkedBy(LivingEntity target, Player player) {
        UUID markerUUID = markedTargets.get(target.getUniqueId());
        if (markerUUID == null) return false;
        Long expiry = markExpiry.get(target.getUniqueId());
        if (expiry == null || System.currentTimeMillis() > expiry) {
            markedTargets.remove(target.getUniqueId());
            markExpiry.remove(target.getUniqueId());
            return false;
        }
        return markerUUID.equals(player.getUniqueId());
    }

    /**
     * Power budget: the trigger rate for a mark is the mark's <em>uptime</em>, not the proc chance
     * that applies it - the same correction already made to Death Mark and Judgment. The old +35%
     * was priced as if only the 37.5% of shots that proc benefited, but the mark lasts 10 seconds
     * and every shot in that window re-rolls it. A fully drawn bow shot takes ~1.5s
     * ({@link OverdrawSkill} treats 1500ms as a full draw, and archers draw fully as a matter of
     * course), so a 10 second mark window covers ~6.7 shots. At the level 50 proc chance of 37.5%
     * that is an uptime of 1 - 0.625^6.7 = 0.96, and allowing for target switching and approach the
     * honest trigger rate is 0.95 - the mark is up on essentially every shot of a sustained fight.
     * The budget is {@code E = rate * (multiplier - 1) = 0.20}, so the level 50 bonus is +21%
     * (E = 0.95 * 0.21 = 0.20) instead of +35% (E = 0.95 * 0.35 = 0.33, 66% over budget).
     * Hardcoded rather than read from config so every server runs the same numbers while the
     * rebalance is being validated.
     */
    public double getMarkBonus(int skillLevel) {
        return scaled(BASE_BONUS_DAMAGE, 0.002, skillLevel); // 11% base + 0.2% per level
    }

    @Override
    public boolean appliesTo(LivingEntity target, Player attacker) {
        return isMarkedBy(target, attacker);
    }

    @Override
    public SkillType levelSource() {
        return SkillType.BOWS;
    }

    @Override
    public double bonusFor(Player attacker, LivingEntity target, int level) {
        return getMarkBonus(level);
    }

    @Override
    public String debugLabel() {
        return "HuntersMark=";
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
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "markBonus", String.format("%.1f", getMarkBonus(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getMarkBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "markBonus", String.format("%.1f", getMarkBonus(skillLevel) * 100)
        ));
    }
    @Override
    public boolean affectsDamage() { return false; } // Proc applies mark, bonus checked separately on marked targets
    @Override
    public void shutdown() {
        activePlayers.clear();
        markedTargets.clear();
        markExpiry.clear();
    }
}
