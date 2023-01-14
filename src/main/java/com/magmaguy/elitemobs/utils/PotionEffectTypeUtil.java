package com.magmaguy.elitemobs.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectTypeUtil {

    public static PotionEffectType getByKey(String key) {
        try {
            PotionEffectType potionEffectType = PotionEffectType.getByKey(NamespacedKey.fromString(key));
            if (potionEffectType != null)
                return potionEffectType;
            else
                return getByName(key);
        } catch (NoSuchMethodError noSuchMethodError) {
            //1.16 workaround
            return getByName(key);
        }
    }

    private static PotionEffectType getByName(String name) {
        PotionEffectType potionEffectType = PotionEffectType.getByName(name);
        if (potionEffectType != null)
            return potionEffectType;
        else {
            new WarningMessage("Failed to get potion effect type in script for key " + name + "!");
            return null;
        }
    }
}
