package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
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
 * Long Reach (PASSIVE) - Increased attack range with spears.
 * Tier 1 unlock.
 */
public class LongReachSkill extends SkillBonus {

    public static final String SKILL_ID = "spears_long_reach";
    public static final String MODIFIER_KEY_STRING = "long_reach_range";
    private static final double BASE_REACH_BONUS = 1.0; // 1.0 block extra reach

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public LongReachSkill() {
        super(SkillType.SPEARS, 10, "Long Reach",
              "Attacks have slightly increased range.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    public double getReachBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_REACH_BONUS + (skillLevel * 0.01);
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
            SkillBonus skill = SkillBonusRegistry.getSkillById(SKILL_ID);
            double bonus = (skill instanceof LongReachSkill lr) ? lr.getReachBonus(skillLevel) : BASE_REACH_BONUS;
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
        // Apply reach bonus if player is already holding a spear
        if (player.getInventory().getItemInMainHand().getType().name().endsWith("_SPEAR")) {
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
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("value", String.format("%.1f", getReachBonus(skillLevel))));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getReachBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getReachBonus(skillLevel))));
    }

    @Override
    public boolean affectsDamage() {
        return false; // This is a utility skill
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
