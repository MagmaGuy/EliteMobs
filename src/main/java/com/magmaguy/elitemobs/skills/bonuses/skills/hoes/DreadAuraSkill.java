package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ToggleSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dread Aura (TOGGLE) - Emit an aura that weakens nearby enemies.
 * While active, nearby enemies receive debuffs and player deals bonus damage.
 * Tier 3 unlock.
 */
public class DreadAuraSkill extends SkillBonus implements ToggleSkill {

    public static final String SKILL_ID = "hoes_dread_aura";
    private static final double AURA_RADIUS = 5.0;
    private static final double BASE_DAMAGE_BONUS = 0.25; // 25% bonus
    private static final double BASE_NEGATIVE_EFFECT = 0.10; // 10% penalty (not used currently)

    private static final Set<UUID> toggledPlayers = new HashSet<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public DreadAuraSkill() {
        super(SkillType.HOES, 50, "Dread Aura",
              "Toggle an aura that weakens nearby enemies.",
              SkillBonusType.TOGGLE, 3, SKILL_ID);
    }

    @Override
    public void toggle(Player player) {
        if (isToggled(player)) {
            disable(player);
        } else {
            enable(player);
        }
    }

    @Override
    public boolean isToggled(Player player) {
        return toggledPlayers.contains(player.getUniqueId());
    }

    @Override
    public void enable(Player player) {
        toggledPlayers.add(player.getUniqueId());
    }

    @Override
    public void disable(Player player) {
        toggledPlayers.remove(player.getUniqueId());
    }

    @Override
    public double getPositiveBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 25% + 0.5% per level
        return BASE_DAMAGE_BONUS + (skillLevel * 0.005);
    }

    @Override
    public double getNegativeEffect(int skillLevel) {
        // No negative effect for this skill
        return BASE_NEGATIVE_EFFECT;
    }

    /**
     * Applies the aura effects to nearby enemies.
     * Should be called periodically while the aura is active.
     */
    public static void applyAuraEffects(Player player) {
        if (!toggledPlayers.contains(player.getUniqueId())) return;

        int skillLevel = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry
            .getPlayerSkillLevel(player, SkillType.HOES);

        // Apply debuffs to nearby enemies
        player.getNearbyEntities(AURA_RADIUS, AURA_RADIUS, AURA_RADIUS).stream()
            .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
            .forEach(e -> {
                LivingEntity mob = (LivingEntity) e;
                int amplifier = Math.min(2, (int) Math.floor(skillLevel / 25.0));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, amplifier));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            });

        // Visual effect
        if (player.getTicksLived() % 20 == 0) { // Once per second
            player.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP,
                player.getLocation(), 5, 2, 0.5, 2, 0.01);
        }
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        disable(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        disable(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonusPercent = getPositiveBonus(skillLevel) * 100;
        return List.of(
                "&7Damage Bonus: &f+" + String.format("%.1f", bonusPercent) + "%",
                "&7Radius: &f" + String.format("%.1f", AURA_RADIUS) + " blocks",
                "&7Weakens nearby enemies",
                "&7Toggle to activate/deactivate"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getPositiveBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Damage (Aura)", getPositiveBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        toggledPlayers.clear();
        activePlayers.clear();
    }
}
