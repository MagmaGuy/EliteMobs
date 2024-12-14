package com.magmaguy.elitemobs.utils;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class AttributeManager {
    private static Attribute getAttributeFromString(String string) {
        try {
            return Attribute.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void setAttribute(LivingEntity livingEntity, String attributeKey, double value) {
        Attribute attribute = getAttributeFromString(attributeKey);
        if (attribute == null) {
            Logger.warn("Failed to set attribute " + attributeKey + "!");
            return;
        }
        livingEntity.getAttribute(attribute).setBaseValue(value);
    }

    public static double getAttributeBaseValue(LivingEntity livingEntity, String attributeKey) {
        Attribute attribute = getAttributeFromString(attributeKey);

        if (attribute == null) {
            Logger.warn("Failed to get attribute " + attributeKey + "!");
            return 1;
        }
        return livingEntity.getAttribute(attribute).getBaseValue();
    }

    public static double getAttributeDefaultValue(LivingEntity livingEntity, String attributeKey) {
        Attribute attribute = getAttributeFromString(attributeKey);

        return livingEntity.getAttribute(attribute).getDefaultValue();
    }

    public static boolean containsKey(LivingEntity livingEntity, String attributeKey) {
        Attribute attribute = getAttributeFromString(attributeKey);

        return livingEntity.getAttribute(attribute) != null;
    }

    public static boolean containsKey(ItemMeta itemMeta, String attributeKey) {
        Attribute attribute = getAttributeFromString(attributeKey);

        if (!itemMeta.hasAttributeModifiers()) return false;
        return itemMeta.getAttributeModifiers().containsKey(attribute);
    }

    public static Attribute getAttribute(String attributeKey) {
        return getAttributeFromString(attributeKey);

    }

    public static AttributeInstance getAttributeInstance(LivingEntity livingEntity, String attributeKey) {
        return livingEntity.getAttribute(getAttribute(attributeKey));
    }
}
