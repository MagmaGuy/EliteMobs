package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sunfall (COOLDOWN) - Every 20 seconds, your next fireball lands with triple force.
 * Tier 4 unlock.
 * <p>
 * Every target caught in the Sunfall fireball takes the bonus. Power budget: a 20 second cooldown
 * is one fireball in ten, so +200% at level 50 is E = 2.0 / 10 = 0.20.
 */
public class SunfallSkill extends MagicWeaponSkill implements CooldownSkill {

    public static final String SKILL_ID = "staves_sunfall";
    private static final long COOLDOWN_SECONDS = 20;

    private final Map<UUID, UUID> sunfallAttack = new ConcurrentHashMap<>();

    public SunfallSkill() {
        super(SkillType.STAVES, 4, "Sunfall", "Your next fireball every 20 seconds lands with triple force.",
                SkillBonusType.COOLDOWN, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return COOLDOWN_SECONDS;
    }

    @Override
    public boolean tryActivate(Player player, Object event) {
        EliteMobDamagedByPlayerEvent hit = strike(event, MagicStrike.STAFF_FIREBALL);
        if (hit == null) return false;
        sunfallAttack.put(player.getUniqueId(), hit.getMagicHit().attackId());
        LivingEntity target = hit.getEliteMobEntity().getLivingEntity();
        if (target != null) {
            target.getWorld().spawnParticle(Particle.EXPLOSION, chest(target), 2, 0.5, 0.5, 0.5, 0);
            target.getWorld().spawnParticle(Particle.FLAME, chest(target).add(0, 2, 0), 60, 1.2, 2.0, 1.2, 0.04);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1F, 0.6F);
        }
        return true;
    }

    @Override
    public boolean sharesActivation(Player player, Object event) {
        EliteMobDamagedByPlayerEvent hit = strike(event, MagicStrike.STAFF_FIREBALL);
        return hit != null && hit.getMagicHit().attackId().equals(sunfallAttack.get(player.getUniqueId()));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return scaled(1.5, 0.01, skillLevel); // +200% at level 50
    }

    @Override
    protected void clearPlayer(UUID playerId) {
        sunfallAttack.remove(playerId);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bonus", String.format("%.0f", getBonusValue(skillLevel) * 100),
                "cooldown", String.valueOf(COOLDOWN_SECONDS)));
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonus", String.format("%.0f", getBonusValue(skillLevel) * 100),
                "cooldown", String.valueOf(COOLDOWN_SECONDS)));
    }

    @Override
    public void shutdown() {
        sunfallAttack.clear();
        super.shutdown();
    }
}
