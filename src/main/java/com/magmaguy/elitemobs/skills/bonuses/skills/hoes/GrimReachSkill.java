package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

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
 * Grim Reach (PASSIVE) - Passive damage bonus simulating extended reach.
 * Increases base damage with hoes.
 * Tier 1 unlock.
 */
public class GrimReachSkill extends SkillBonus {

    public static final String SKILL_ID = "hoes_grim_reach";
    public static final String MODIFIER_KEY_STRING = "grim_reach_range";
    private static final double BASE_DAMAGE_BONUS = 0.10; // 10% bonus

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public GrimReachSkill() {
        super(SkillType.HOES, 10, "Grim Reach",
              "Extended reach allows you to deal more damage.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) return configFields.calculateValue(skillLevel);
        return scaled(BASE_DAMAGE_BONUS, 0.002, skillLevel); // 10% base + 0.2% per level
    }

    /**
     * Gets the reach bonus for a skill level (extra blocks of interaction range).
     */
    public static double getReachBonus(int skillLevel) {
        // 1.0 to 2.0 extra blocks
        return scaled(1.0, 0.013, 2.0, skillLevel);
    }

    /**
     * Applies the entity interaction range modifier to the player.
     */
    public static void applyReachBonus(Player player, int skillLevel) {
        if (!activePlayers.contains(player.getUniqueId())) return;
        try {
            AttributeInstance attr = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (attr == null) return;
            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING);
            removeModifierByKey(attr, key);
            double bonus = getReachBonus(skillLevel);
            attr.addModifier(new AttributeModifier(key, bonus, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        } catch (NoSuchFieldError | IllegalArgumentException e) {
            // ENTITY_INTERACTION_RANGE doesn't exist pre-1.20.5
        }
    }

    /**
     * Removes the entity interaction range modifier from the player.
     */
    public static void removeReachBonus(Player player) {
        try {
            AttributeInstance attr = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
            if (attr == null) return;
            removeModifierByKey(attr, new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING));
        } catch (NoSuchFieldError | IllegalArgumentException e) {
            // ENTITY_INTERACTION_RANGE doesn't exist pre-1.20.5
        }
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
        // Apply reach bonus if player is already holding a hoe
        if (player.getInventory().getItemInMainHand().getType().name().endsWith("_HOE")) {
            applyReachBonus(player, skillLevel);
        }
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeReachBonus(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeReachBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bonusPercent", String.format("%.1f", getDamageMultiplier(skillLevel) * 100),
                "reachBlocks", String.format("%.1f", getReachBonus(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Already a bonus fraction (e.g., 0.15 for +15%), not a multiplier.
        // processOffensiveSkill adds 1.0 + this, so total = 1.15x.
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonusPercent", String.format("%.1f", getDamageMultiplier(skillLevel) * 100),
                "reachBlocks", String.format("%.1f", getReachBonus(skillLevel))
        ));
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
