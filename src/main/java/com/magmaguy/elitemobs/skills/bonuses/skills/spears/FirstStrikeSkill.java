package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
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
 * First Strike (PASSIVE) - Bonus damage against full-health enemies.
 * Tier 1 unlock.
 */
public class FirstStrikeSkill extends SkillBonus {

    public static final String SKILL_ID = "spears_first_strike";
    private static final double BASE_DAMAGE_BONUS = 0.30; // 30% bonus damage

    private static final Set<UUID> activePlayers = new HashSet<>();

    public FirstStrikeSkill() {
        super(SkillType.SPEARS, 10, "First Strike",
              "Deal bonus damage to full-health enemies.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    /**
     * Checks and applies First Strike damage bonus.
     * Returns the damage multiplier to apply.
     */
    public double checkAndApply(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player)) return 1.0;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return 1.0;

        LivingEntity target = eliteEntity.getLivingEntity();

        // Check if target is at full health
        if (target.getHealth() < target.getMaxHealth()) return 1.0;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double bonus = getDamageBonus(skillLevel);

        // Visual feedback
        target.getWorld().spawnParticle(Particle.CRIT,
            target.getLocation().add(0, 1.5, 0), 10, 0.3, 0.3, 0.3, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.5f);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7b\u00A7lFIRST STRIKE!"));

        incrementProcCount(player);
        return 1.0 + bonus;
    }

    public double getDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BONUS + (skillLevel * 0.005);
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
            "&7Bonus vs Full HP: &f+" + String.format("%.0f", getDamageBonus(skillLevel) * 100) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% vs Full HP", getDamageBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
