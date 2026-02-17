package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quick Reload (PASSIVE) - Hitting enemies grants a brief movement speed boost.
 * ATTACK_SPEED has no effect on crossbow reload speed, so this provides a
 * movement speed buff instead, letting the player reposition faster after shooting.
 * Tier 1 unlock.
 */
public class QuickReloadSkill extends SkillBonus {

    public static final String SKILL_ID = "crossbows_quick_reload";
    public static final String MODIFIER_KEY_STRING = "quick_reload_speed";
    private static final int BUFF_DURATION_TICKS = 60; // 3 seconds

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public QuickReloadSkill() {
        super(SkillType.CROSSBOWS, 10, "Quick Reload",
              "Successful hits grant a brief movement speed boost.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    private static void removeModifierByKey(AttributeInstance attr, NamespacedKey key) {
        for (AttributeModifier modifier : attr.getModifiers()) {
            if (modifier.getKey().equals(key)) {
                attr.removeModifier(modifier);
                return;
            }
        }
    }

    public void applyHaste(Player player) {
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        double speedBonus = getSpeedBonus(skillLevel);

        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;

        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING);
        removeModifierByKey(attr, key);
        attr.addModifier(new AttributeModifier(key, speedBonus, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));

        // Remove the modifier after the buff duration
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    AttributeInstance a = player.getAttribute(Attribute.MOVEMENT_SPEED);
                    if (a != null) {
                        removeModifierByKey(a, key);
                    }
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, BUFF_DURATION_TICKS);
    }

    private double getSpeedBonus(int skillLevel) {
        // Base 0.03 + 0.001 per level movement speed bonus (base walk speed is 0.1)
        return 0.03 + (skillLevel * 0.001);
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
        return applyLoreTemplates(Map.of("speedBonus", String.format("%.0f", getSpeedBonus(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) { return getSpeedBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return applyFormattedBonusTemplate(Map.of("speedBonus", String.format("%.0f", getSpeedBonus(skillLevel) * 100))); }
    @Override
    public boolean affectsDamage() { return false; }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
