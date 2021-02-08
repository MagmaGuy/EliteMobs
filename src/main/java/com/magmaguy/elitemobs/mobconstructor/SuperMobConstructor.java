package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;


public class SuperMobConstructor {

    /*
    This class assumes that the entity has already been filtered and validated before being invoked
     */
    public static LivingEntity constructSuperMob(LivingEntity livingEntity) {

        if (!SuperMobProperties.isValidSuperMobType(livingEntity)) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to construct an invalid supermob. Report this to the dev!");
            return null;
        }

        String name = ChatColorConverter.convert(SuperMobProperties.getDataInstance(livingEntity).getName());
        double newMaxHealth = SuperMobProperties.getDataInstance(livingEntity).getDefaultMaxHealth() * DefaultConfig.superMobStackAmount;

        livingEntity.setCustomName(name);
        livingEntity.setCustomNameVisible(true);
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
        livingEntity.setHealth(newMaxHealth);

        EntityTracker.registerSuperMob(livingEntity);

        return livingEntity;

    }

}
