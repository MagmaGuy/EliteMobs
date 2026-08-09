package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Crushing Blow (PROC) - Chance to land a crushing hit for heavy bonus damage.
 * Tier 1 unlock.
 * <p>
 * The skill used to advertise armor penetration and was entirely cosmetic as a result: its
 * {@code getArmorIgnore} value reached no damage code, {@code affectsDamage()} returned false, and
 * {@code onProc} only played particles and a sound. Armor penetration is also unimplementable
 * here - elite armor is cosmetic and every vanilla damage modifier except BASE is zeroed before
 * the formula runs (see {@code EliteMobDamagedByPlayerEvent.onEliteMobAttacked}) - so the proc was
 * converted into the bonus damage it was always meant to represent, priced to the standard budget.
 */
public class CrushingBlowSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "maces_crushing_blow";
    private static final double BASE_PROC_CHANCE = 0.12;
    private static final double BASE_DAMAGE_MULTIPLIER = 1.34;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public CrushingBlowSkill() {
        super(SkillType.MACES, 10, "Crushing Blow",
              "Chance to land a crushing hit that deals bonus damage.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        return scaled(BASE_PROC_CHANCE, 0.003, 0.30, skillLevel);
    }

    /**
     * Power budget: a 27% proc rate at level 50 earns a 1.74x hit (E = 0.27 * 0.74 = 0.20).
     * Hardcoded rather than read from config so every server runs the same numbers while the
     * rebalance is being validated.
     */
    public double getDamageMultiplier(int skillLevel) {
        return scaled(BASE_DAMAGE_MULTIPLIER, 0.008, skillLevel);
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        // Visual effect
        if (event.getEliteMobEntity().getLivingEntity() != null) {
            event.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                Particle.BLOCK,
                event.getEliteMobEntity().getLivingEntity().getLocation().add(0, 1, 0),
                20, 0.3, 0.3, 0.3, 0.1,
                Material.IRON_BLOCK.createBlockData()
            );
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
        }
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
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "multiplier", String.format("%.1f", getDamageMultiplier(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Return the bonus portion only (e.g., 0.74 for a 1.74x multiplier).
        // processOffensiveSkill adds 1.0 + this, so total = the damage multiplier.
        return getDamageMultiplier(skillLevel) - 1.0;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "multiplier", String.format("%.1f", getDamageMultiplier(skillLevel)),
                "procChance", String.format("%.0f", getProcChance(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
