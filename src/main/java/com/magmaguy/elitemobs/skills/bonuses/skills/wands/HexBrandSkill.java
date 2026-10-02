package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.SkillTargetMarks;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hex Brand (PROC) - Missiles can brand the target; branded targets take more of your damage.
 * Tier 1 unlock.
 * <p>
 * Power budget: priced on the brand's uptime, the same correction as Hunter's Mark. A wand fires
 * every 0.55 seconds, so a 6 second brand covers ~11 missiles; at 25% each the brand is up ~95% of
 * a sustained fight, and +21% at level 50 gives E = 0.95 * 0.21 = 0.20.
 */
public class HexBrandSkill extends MagicWeaponSkill implements ProcSkill, TargetDebuffBonus {

    public static final String SKILL_ID = "wands_hex_brand";
    private static final long BRAND_MILLIS = 6000;

    private final SkillTargetMarks brands = new SkillTargetMarks();

    public HexBrandSkill() {
        super(SkillType.WANDS, 1, "Hex Brand", "Missiles can brand enemies to take more of your damage.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.WAND_MISSILE) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.15, 0.002, 0.45, skillLevel); // 25% at level 50
    }

    private double brandBonus(int skillLevel) {
        return scaled(0.11, 0.002, skillLevel); // 21% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        brands.mark(player.getUniqueId(), target.getUniqueId(), BRAND_MILLIS);
        target.getWorld().spawnParticle(Particle.WITCH, chest(target), 12, 0.3, 0.4, 0.3, 0);
    }

    @Override
    public boolean appliesTo(LivingEntity target, Player attacker) {
        return brands.isMarkedBy(target.getUniqueId(), attacker.getUniqueId());
    }

    @Override
    public SkillType levelSource() {
        return SkillType.WANDS;
    }

    @Override
    public double bonusFor(Player attacker, LivingEntity target, int level) {
        return brandBonus(level);
    }

    @Override
    public String debugLabel() {
        return "HexBrand=";
    }

    @Override
    protected void clearPlayer(UUID playerId) {
        brands.clearSource(playerId);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "brandBonus", String.format("%.1f", brandBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return brandBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("brandBonus", String.format("%.1f", brandBonus(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The brand applies through the target debuff pass, not to the branding hit.
    }

    @Override
    public void shutdown() {
        brands.clear();
        super.shutdown();
    }
}
