package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Stagger (PROC) - Chance to stagger enemies, slowing and weakening them.
 * Tier 2 unlock.
 */
public class StaggerSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "axes_stagger";
    private static final double BASE_PROC_CHANCE = 0.12;
    private static final int STAGGER_DURATION = 40; // 2 seconds

    private static final Set<UUID> activePlayers = new HashSet<>();

    public StaggerSkill() {
        super(SkillType.AXES, 25, "Stagger",
              "Attacks can stagger enemies, slowing and weakening them.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
        applyStagger(target.getLivingEntity(), skillLevel);
    }

    private void applyStagger(LivingEntity entity, int skillLevel) {
        int duration = STAGGER_DURATION + (skillLevel / 10);
        int slowLevel = Math.min(2, skillLevel / 30);
        int weaknessLevel = Math.min(1, skillLevel / 50);

        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, slowLevel));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, weaknessLevel));
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
                "&7Effect: &fSlowness + Weakness",
                "&7Duration: &f2 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getProcChance(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.1f%% Stagger Chance", getProcChance(skillLevel) * 100); }
    @Override
    public boolean affectsDamage() { return false; } // Applies debuffs, not damage
    @Override
    public void shutdown() { activePlayers.clear(); }
}
