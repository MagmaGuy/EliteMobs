package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frostbite (PROC) - Chance to slow and weaken enemies on hit.
 * Tier 1 unlock.
 */
public class FrostbiteSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "bows_frostbite";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% chance
    private static final int SLOW_DURATION = 60; // 3 seconds

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public FrostbiteSkill() {
        super(SkillType.BOWS, 10, "Frostbite",
              "Arrows have a chance to slow and weaken enemies.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
        int amplifier = Math.min(2, skillLevel / 30); // 0-2 based on level

        event.getEliteMobEntity().getLivingEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.SLOWNESS, SLOW_DURATION, amplifier + 1));
        event.getEliteMobEntity().getLivingEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.MINING_FATIGUE, SLOW_DURATION, amplifier));

        // Visual effect
        event.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                Particle.SNOWFLAKE, event.getEliteMobEntity().getLivingEntity().getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.05);
    }

    private double getDamageBonus(int skillLevel) {
        return 1.1 + (skillLevel * 0.005); // 10% base + 0.5% per level
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
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getDamageBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "procChance", String.format("%.0f", getProcChance(skillLevel) * 100)
        ));
    }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
