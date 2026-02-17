package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polearm Mastery (PASSIVE) - Significant attack speed and damage increase.
 * Tier 4 unlock.
 */
public class PolearmMasterySkill extends SkillBonus {

    public static final String SKILL_ID = "spears_polearm_mastery";
    public static final String MODIFIER_KEY_STRING = "polearm_mastery_speed";
    private static final double BASE_DAMAGE_BONUS = 0.20; // 20% damage bonus
    private static final double BASE_ATTACK_SPEED_BONUS = 0.40; // 40% attack speed

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public PolearmMasterySkill() {
        super(SkillType.SPEARS, 75, "Polearm Mastery",
              "Significant attack speed and damage increase.",
              SkillBonusType.PASSIVE, 4, SKILL_ID);
    }

    public double getDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BONUS + (skillLevel * 0.003);
    }

    /**
     * Checks if a player has this skill active.
     */
    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    /**
     * Gets the damage multiplier for this passive.
     * Applied to all spear attacks when active.
     */
    public double getDamageMultiplier(int skillLevel) {
        if (!activePlayers.isEmpty()) {
            return 1.0 + getDamageBonus(skillLevel);
        }
        return 1.0;
    }

    /**
     * Applies the attack speed attribute modifier.
     * Uses MULTIPLY_SCALAR_1 so the 0.40 base value gives a real 40% increase
     * (ADD_NUMBER 0.40 on base 4.0 was only a 10% effective increase).
     */
    public static void applyAttackSpeedBonus(Player player, int skillLevel) {
        if (!activePlayers.contains(player.getUniqueId())) return;
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;
        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING);
        removeModifierByKey(attr, key);
        double bonus = BASE_ATTACK_SPEED_BONUS + (skillLevel * 0.005);
        attr.addModifier(new AttributeModifier(key, bonus, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY));
    }

    /**
     * Removes the attack speed attribute modifier.
     */
    public static void removeAttackSpeedBonus(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;
        removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING));
    }

    private static void removeModifierByKey(AttributeInstance attr, NamespacedKey key) {
        for (AttributeModifier modifier : attr.getModifiers()) {
            if (modifier.getKey().equals(key)) {
                attr.removeModifier(modifier);
                return;
            }
        }
    }

    public double getAttackSpeedBonus(int skillLevel) {
        return BASE_ATTACK_SPEED_BONUS + (skillLevel * 0.005);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
        // Apply attack speed bonus if player is already holding a spear
        if (player.getInventory().getItemInMainHand().getType().name().endsWith("_SPEAR")) {
            applyAttackSpeedBonus(player, skillLevel);
        }
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeAttackSpeedBonus(player);
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
                "damage", String.format("%.0f", getDamageBonus(skillLevel) * 100),
                "speed", String.format("%.0f", getAttackSpeedBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damage", String.format("%.0f", getDamageBonus(skillLevel) * 100),
                "speed", String.format("%.0f", getAttackSpeedBonus(skillLevel) * 100)));
    }

    @Override
    public TestStrategy getTestStrategy() {
        return TestStrategy.ATTRIBUTE_CHECK;
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
