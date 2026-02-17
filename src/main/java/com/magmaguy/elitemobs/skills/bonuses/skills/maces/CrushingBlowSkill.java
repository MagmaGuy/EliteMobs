package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
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
 * Crushing Blow (PROC) - Chance to ignore enemy armor.
 * Tier 1 unlock.
 */
public class CrushingBlowSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "maces_crushing_blow";
    private static final double BASE_PROC_CHANCE = 0.12;
    private static final double BASE_ARMOR_IGNORE = 0.25; // 25% armor ignored

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public CrushingBlowSkill() {
        super(SkillType.MACES, 10, "Crushing Blow",
              "Chance to ignore enemy armor on hit.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.30, BASE_PROC_CHANCE + (skillLevel * 0.003));
    }

    public double getArmorIgnore(int skillLevel) {
        return Math.min(0.60, BASE_ARMOR_IGNORE + (skillLevel * 0.005));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
        double armorIgnore = getArmorIgnore(skillLevel);

        // Increase damage to simulate armor penetration
        event.setDamage(event.getDamage() * (1.0 + armorIgnore));

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
                "armorIgnore", String.format("%.0f", getArmorIgnore(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getArmorIgnore(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "armorIgnore", String.format("%.0f", getArmorIgnore(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
