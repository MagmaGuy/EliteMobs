package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Finishing Flourish (CONDITIONAL) - Execute: Deal bonus damage to low HP enemies.
 * Condition: Target below 30% HP.
 * Tier 3 unlock.
 */
public class FinishingFlourishSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "swords_finishing_flourish";
    private static final double BASE_EXECUTE_BONUS = 0.50; // 50% bonus damage
    private static final double HEALTH_THRESHOLD = 0.30; // 30% HP threshold

    private static final Set<UUID> activePlayers = new HashSet<>();

    public FinishingFlourishSkill() {
        super(SkillType.SWORDS, 50, "Finishing Flourish",
              "Deal bonus damage to enemies below 30% health.",
              SkillBonusType.CONDITIONAL, 3, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;

        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return false;

        LivingEntity entity = target.getLivingEntity();
        double healthPercent = entity.getHealth() / entity.getMaxHealth();

        return healthPercent <= HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // Base 50% + 1% per level
        return BASE_EXECUTE_BONUS + (skillLevel * 0.01);
    }

    /**
     * Applies execute damage if conditions are met.
     */
    public static double applyExecuteBonus(Player player, EliteMobDamagedByPlayerEvent event, double currentDamage) {
        if (!activePlayers.contains(player.getUniqueId())) return currentDamage;

        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return currentDamage;

        LivingEntity entity = target.getLivingEntity();
        double healthPercent = entity.getHealth() / entity.getMaxHealth();

        if (healthPercent > HEALTH_THRESHOLD) return currentDamage;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double bonus = BASE_EXECUTE_BONUS + (skillLevel * 0.01);

        // Visual feedback
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c§lFINISHING FLOURISH!"));

        return currentDamage * (1 + bonus);
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
        double bonus = getConditionalBonus(skillLevel) * 100;
        return List.of(
                "&7Execute Damage: &f+" + String.format("%.0f", bonus) + "%",
                "&7Threshold: &fBelow 30% HP",
                "&7Finish off weakened enemies!"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% Execute Damage", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
