package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Devastating Blow (PROC) - Chance to deal massive bonus damage.
 * Tier 1 unlock.
 */
public class DevastatingBlowSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "axes_devastating_blow";
    private static final double BASE_PROC_CHANCE = 0.10; // 10% chance
    private static final double BASE_DAMAGE_MULTIPLIER = 2.0; // Double damage

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public DevastatingBlowSkill() {
        super(SkillType.AXES, 10, "Devastating Blow",
              "Attacks have a chance to deal massive bonus damage.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.30, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
        double multiplier = getDamageMultiplier(skillLevel);

        event.setDamage(event.getDamage() * multiplier);

        // Visual effect
        if (event.getEliteMobEntity().getLivingEntity() != null) {
            event.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                    Particle.EXPLOSION, event.getEliteMobEntity().getLivingEntity().getLocation(), 1
            );
        }
    }

    private double getDamageMultiplier(int skillLevel) {
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02);
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
                "damageMultiplier", String.format("%.1f", getDamageMultiplier(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getDamageMultiplier(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("damageMultiplier", String.format("%.1f", getDamageMultiplier(skillLevel)))); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
