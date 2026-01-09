package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Parry (CONDITIONAL) - Reduce damage taken while blocking with a sword.
 * Condition: Player is blocking.
 * Tier 3 unlock.
 */
public class ParrySkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "swords_parry";
    private static final double BASE_DAMAGE_REDUCTION = 0.30; // 30% extra reduction

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ParrySkill() {
        super(SkillType.SWORDS, 50, "Parry",
              "Block attacks with your sword to reduce damage further.",
              SkillBonusType.CONDITIONAL, 3, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof PlayerDamagedByEliteMobEvent)) return false;

        // Check if player is blocking with a sword
        if (!player.isBlocking()) return false;

        Material mainHand = player.getInventory().getItemInMainHand().getType();
        return mainHand.name().endsWith("_SWORD");
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // Base 30% + 0.5% per level damage reduction
        return Math.min(0.60, BASE_DAMAGE_REDUCTION + (skillLevel * 0.005));
    }

    /**
     * Applies parry damage reduction if conditions are met.
     */
    public static double applyParryReduction(Player player, PlayerDamagedByEliteMobEvent event, double currentDamage) {
        if (!activePlayers.contains(player.getUniqueId())) return currentDamage;
        if (!player.isBlocking()) return currentDamage;

        Material mainHand = player.getInventory().getItemInMainHand().getType();
        if (!mainHand.name().endsWith("_SWORD")) return currentDamage;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double reduction = Math.min(0.60, BASE_DAMAGE_REDUCTION + (skillLevel * 0.005));

        // Visual feedback
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aParry!"));

        return currentDamage * (1 - reduction);
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
        double reduction = getConditionalBonus(skillLevel) * 100;
        return List.of(
                "&7Damage Reduction: &f" + String.format("%.1f", reduction) + "%",
                "&7Condition: Blocking with sword",
                "&7Stacks with normal blocking"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("-%.1f%% Damage when Parrying", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public boolean affectsDamage() {
        return false; // Defensive skill - reduces damage taken, doesn't affect damage dealt
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
