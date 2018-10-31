package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class EliteMobConstructor {

    /*
    This class assumes that the entity has already been filtered and validated before being invoked
     */
    public static LivingEntity constructEliteMob(LivingEntity livingEntity, int mobLevel) {

        if (!EliteMobProperties.isValidEliteMobType(livingEntity)) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to construct an invalid supermob. Report this to the dev!");
            return null;
        }
        
        String name = EliteMobProperties.getPluginData(livingEntity).getName().replace("$level", mobLevel+"");
        EntityTracker.registerEliteMob(new EliteMobEntity(livingEntity, mobLevel));

        return livingEntity;

    }

}
