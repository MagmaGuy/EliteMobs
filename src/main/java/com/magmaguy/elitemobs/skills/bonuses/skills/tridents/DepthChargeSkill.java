package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Depth Charge (CONDITIONAL) - When target is in water, creates an explosion that damages nearby aquatic enemies.
 * Tier 4 unlock.
 */
public class DepthChargeSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "tridents_depth_charge";
    private static final double AOE_RADIUS = 4.0;
    private static final double BASE_DAMAGE_BONUS = 0.5;
    private static final double BASE_AOE_DAMAGE = 0.4;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public DepthChargeSkill() {
        super(SkillType.TRIDENTS, 75, "Depth Charge",
              "When target is in water, creates an explosion that damages nearby aquatic enemies.",
              SkillBonusType.CONDITIONAL, 4, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return false;

        // Active when target is in water
        return eliteEntity.getLivingEntity().isInWater();
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        if (configFields != null) {
            return BASE_DAMAGE_BONUS + configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BONUS + (skillLevel * 0.01);
    }

    /**
     * Called when the skill activates (target is in water).
     * Creates depth charge explosion effect.
     */
    public void onActivate(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player)) return;
        if (!conditionMet(player, event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = getPlayerSkillLevel(player);

        // Create depth charge explosion
        target.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, target.getLocation(), 50, 2, 2, 2, 0.1);

        // Damage nearby aquatic enemies
        // Use bypass to prevent recursive skill processing
        double aoeDamage = event.getDamage() * calculateAoeDamage(skillLevel);
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.getNearbyEntities(AOE_RADIUS, AOE_RADIUS, AOE_RADIUS).stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player) && ((LivingEntity) e).isInWater())
                    .forEach(e -> ((LivingEntity) e).damage(aoeDamage, player));
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }

        // Send feedback to player
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§9DEPTH CHARGE!"));
    }

    private double calculateAoeDamage(int skillLevel) {
        if (configFields != null) {
            return BASE_AOE_DAMAGE * configFields.calculateValue(skillLevel);
        }
        return BASE_AOE_DAMAGE + (skillLevel * 0.005);
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
        double damageBonus = getConditionalBonus(skillLevel) * 100;
        double aoeDamage = calculateAoeDamage(skillLevel) * 100;
        return List.of(
                "&7Active: &fTarget in water",
                "&7Damage Bonus: &f+" + String.format("%.1f", damageBonus) + "%",
                "&7AOE Damage: &f" + String.format("%.1f", aoeDamage) + "%",
                "&7Radius: &f" + AOE_RADIUS + " blocks"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% (In Water)", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
