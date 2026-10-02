package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unravel (PROC) - Missiles can unravel the target's defenses for everyone.
 * Tier 3 unlock.
 * <p>
 * An unravelled target takes more damage from every player's weapon hits for 5 seconds, like
 * Expose Weakness. The strength is fixed when applied. Power budget: ~70% uptime under sustained
 * fire gives E = 0.70 * 0.15 = 0.10 for the caster, the rest goes to the party.
 */
public class UnravelSkill extends MagicWeaponSkill implements ProcSkill, TargetDebuffBonus {

    public static final String SKILL_ID = "wands_unravel";
    private static final long DURATION_MILLIS = 5000;

    private final Map<UUID, Unravelled> unravelled = new ConcurrentHashMap<>();

    private record Unravelled(double bonus, long expiresAt) {
    }

    public UnravelSkill() {
        super(SkillType.WANDS, 3, "Unravel", "Missiles can unravel an enemy's defenses for everyone.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.WAND_MISSILE) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.10, 0.002, 0.40, skillLevel); // 20% at level 50
    }

    private double unravelBonus(int skillLevel) {
        return scaled(0.10, 0.001, skillLevel); // 15% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        long now = System.currentTimeMillis();
        unravelled.values().removeIf(state -> state.expiresAt() <= now);
        unravelled.put(target.getUniqueId(), new Unravelled(unravelBonus(skillLevel(player)), now + DURATION_MILLIS));
        target.getWorld().spawnParticle(Particle.ENCHANT, chest(target), 20, 0.4, 0.5, 0.4, 0.5);
    }

    @Override
    public boolean appliesTo(LivingEntity target, Player attacker) {
        Unravelled state = unravelled.get(target.getUniqueId());
        return state != null && state.expiresAt() > System.currentTimeMillis();
    }

    @Override
    public SkillType levelSource() {
        return SkillType.WANDS;
    }

    @Override
    public double bonusFor(Player attacker, LivingEntity target, int level) {
        Unravelled state = unravelled.get(target.getUniqueId());
        return state == null ? 0 : state.bonus();
    }

    @Override
    public String debugLabel() {
        return "Unravel=";
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "bonus", String.format("%.1f", unravelBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return unravelBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("bonus", String.format("%.1f", unravelBonus(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The debuff applies through the target debuff pass.
    }

    @Override
    public void shutdown() {
        unravelled.clear();
        super.shutdown();
    }
}
