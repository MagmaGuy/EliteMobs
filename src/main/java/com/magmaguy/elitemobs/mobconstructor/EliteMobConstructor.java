package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class EliteMobConstructor {

    /*
    This class assumes that the entity has already been filtered and validated before being invoked
     */
    public static LivingEntity constructEliteMob(LivingEntity livingEntity, int mobLevel) {

        if (!EliteMobProperties.isValidEliteMobType(livingEntity)) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to construct an invalid Elite Mob. Report this to the dev!");
            Bukkit.getLogger().warning("[EliteMobs] Elite Mob type: " + livingEntity.getType());
            return null;
        }

        new EliteMobEntity(livingEntity, mobLevel);

        return livingEntity;

    }

}
