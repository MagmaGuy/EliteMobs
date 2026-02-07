package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Precision Thrust (PROC) - Chance to deal critical damage.
 * Tier 1 unlock.
 */
public class PrecisionThrustSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "spears_precision_thrust";
    private static final double BASE_PROC_CHANCE = 0.10;
    private static final double BASE_CRIT_MULTIPLIER = 1.5; // 150% damage

    private static final Set<UUID> activePlayers = new HashSet<>();

    public PrecisionThrustSkill() {
        super(SkillType.SPEARS, 10, "Precision Thrust",
              "Chance to deal critical damage.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.30, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double critMultiplier = getCritMultiplier(skillLevel);

        // Apply critical damage
        event.setDamage(event.getDamage() * critMultiplier);

        LivingEntity target = eliteEntity.getLivingEntity();

        // Visual effects - precise strike
        target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
            target.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
        target.getWorld().spawnParticle(Particle.ENCHANTED_HIT,
            target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.2);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7b\u00A7lPRECISION THRUST!"));

        incrementProcCount(player);
    }

    public double getCritMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_CRIT_MULTIPLIER + (skillLevel * 0.01);
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
        return List.of(
            "&7Proc Chance: &f" + String.format("%.1f", getProcChance(skillLevel) * 100) + "%",
            "&7Crit Damage: &f" + String.format("%.0f", getCritMultiplier(skillLevel) * 100) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getCritMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% for %.0f%% Crit", getProcChance(skillLevel) * 100, getCritMultiplier(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
