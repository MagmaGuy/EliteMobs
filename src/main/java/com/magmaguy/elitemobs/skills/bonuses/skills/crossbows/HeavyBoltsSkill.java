package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Heavy Bolts (PASSIVE) - Bolts deal bonus damage and apply knockback.
 * Tier 3 unlock.
 */
public class HeavyBoltsSkill extends SkillBonus {

    public static final String SKILL_ID = "crossbows_heavy_bolts";
    private static final double BASE_DAMAGE_BONUS = 0.15; // 15% bonus
    private static final double BASE_KNOCKBACK = 1.2;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public HeavyBoltsSkill() {
        super(SkillType.CROSSBOWS, 50, "Heavy Bolts",
              "Bolts deal bonus damage and apply knockback.",
              SkillBonusType.PASSIVE, 3, SKILL_ID);
    }

    public void applyKnockback(Player player, LivingEntity target) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        double knockbackStrength = BASE_KNOCKBACK + (skillLevel * 0.01);
        Vector knockback = target.getLocation().toVector()
                .subtract(player.getLocation().toVector());
        // Check for zero/near-zero vector to avoid NaN from normalize()
        if (knockback.lengthSquared() < 0.001) {
            // Player and target at same location - skip knockback
            return;
        }
        knockback.normalize().multiply(knockbackStrength);
        knockback.setY(0.6);
        target.setVelocity(target.getVelocity().add(knockback));
    }

    private double getDamageBonus(int skillLevel) {
        return BASE_DAMAGE_BONUS + (skillLevel * 0.003); // 15% base + 0.3% per level
    }

    private double getKnockbackStrength(int skillLevel) {
        return BASE_KNOCKBACK + (skillLevel * 0.01);
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
                "damage", String.format("%.1f", getDamageBonus(skillLevel) * 100),
                "knockback", String.format("%.1f", getKnockbackStrength(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getDamageBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("damage", String.format("%.1f", getDamageBonus(skillLevel) * 100))); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
