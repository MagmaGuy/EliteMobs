package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataType;

public class ProjectileTagger {

    public static final NamespacedKey customDamageKey = new NamespacedKey(MetadataHandler.PLUGIN, "custom_damage");

    public static void tagProjectileWithCustomDamage(Projectile projectile, double customDamage){
        projectile.getPersistentDataContainer().set(customDamageKey, PersistentDataType.DOUBLE, customDamage);
    }

    public static boolean projectileHasCustomDamage(Projectile projectile){
        return projectile.getPersistentDataContainer().has(customDamageKey, PersistentDataType.DOUBLE);
    }

    public static double getProjectileCustomDamage(Projectile projectile){
        if (!projectileHasCustomDamage(projectile))
            return -1;
        return projectile.getPersistentDataContainer().get(customDamageKey, PersistentDataType.DOUBLE);
    }

}
