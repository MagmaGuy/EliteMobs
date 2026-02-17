package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

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
 * Poise (PASSIVE) - Reduces knockback taken when using swords.
 * Always active when selected.
 * Tier 1 unlock.
 */
public class PoiseSkill extends SkillBonus {

    public static final String SKILL_ID = "swords_poise";
    public static final String MODIFIER_KEY_STRING = "poise_knockback_resistance";
    private static final double BASE_KNOCKBACK_REDUCTION = 0.20; // 20% reduction

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public PoiseSkill() {
        super(SkillType.SWORDS, 10, "Poise",
              "Reduces knockback taken while wielding a sword.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    /**
     * Gets the knockback reduction percentage for a skill level.
     */
    public static double getKnockbackReduction(int skillLevel) {
        // Base 20% + 0.3% per level, max 50%
        return Math.min(0.50, BASE_KNOCKBACK_REDUCTION + (skillLevel * 0.003));
    }

    /**
     * Checks if a player has this skill active.
     */
    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    /**
     * Applies the knockback resistance modifier to the player.
     */
    public static void applyKnockbackResistance(Player player, int skillLevel) {
        AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (attr == null) return;
        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING);
        removeModifierByKey(attr, key);
        double reduction = getKnockbackReduction(skillLevel);
        attr.addModifier(new AttributeModifier(key, reduction, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
    }

    /**
     * Removes the knockback resistance modifier from the player.
     */
    public static void removeKnockbackResistance(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
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

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeKnockbackResistance(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeKnockbackResistance(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double reduction = getKnockbackReduction(skillLevel) * 100;
        return applyLoreTemplates(Map.of("value", String.format("%.1f", reduction)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getKnockbackReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getKnockbackReduction(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // Knockback reduction skill doesn't affect damage
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
