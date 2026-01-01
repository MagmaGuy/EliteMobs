package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Poseidon's Favor (PASSIVE) - On hit, grants water breathing, dolphin's grace, and conduit power.
 * Also provides passive damage bonus.
 * Tier 2 unlock.
 */
public class PoseidonsFavorSkill extends SkillBonus {

    public static final String SKILL_ID = "tridents_poseidons_favor";
    private static final int BUFF_DURATION = 200; // 10 seconds
    private static final double BASE_DAMAGE_BONUS = 0.15;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public PoseidonsFavorSkill() {
        super(SkillType.TRIDENTS, 25, "Poseidon's Favor",
              "On hit, grants water breathing, dolphin's grace, and conduit power. Also provides passive damage bonus.",
              SkillBonusType.PASSIVE, 2, SKILL_ID);
    }

    /**
     * Called when player damages an entity with this skill active.
     * Grants water-based buffs to the player.
     */
    public void onHit(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player)) return;

        int skillLevel = getPlayerSkillLevel(player);
        int amplifier = calculateAmplifier(skillLevel);

        // Grant water breathing and dolphin's grace on hit
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, BUFF_DURATION, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, BUFF_DURATION, amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, BUFF_DURATION, 0));
    }

    private int calculateAmplifier(int skillLevel) {
        if (configFields != null) {
            return (int) Math.floor(configFields.calculateValue(skillLevel));
        }
        return (int) Math.floor(skillLevel * 0.02);
    }

    private double calculateDamageBonus(int skillLevel) {
        if (configFields != null) {
            return BASE_DAMAGE_BONUS * configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BONUS + (skillLevel * 0.002);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
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
        int amplifier = calculateAmplifier(skillLevel);
        double damageBonus = calculateDamageBonus(skillLevel) * 100;
        return List.of(
                "&7On hit: &fWater Breathing",
                "&7Dolphin's Grace &f" + (amplifier + 1),
                "&7Conduit Power",
                "&7Damage Bonus: &f+" + String.format("%.1f", damageBonus) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% Damage", calculateDamageBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
