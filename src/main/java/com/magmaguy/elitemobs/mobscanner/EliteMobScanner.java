package com.magmaguy.elitemobs.mobscanner;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Iterator;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

public class EliteMobScanner {

    private static int aggressiveRange = ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.AGGRESSIVE_STACK_RANGE);

    public static void scanElites() {

        for (World world : validWorldList) {

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext()) {

                LivingEntity livingEntity = iterator.next();

                if (!EliteMobProperties.isValidEliteMobType(livingEntity)) continue;

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_NATURAL_MOBS))
                    if (EntityTracker.isNaturalEntity(livingEntity))
                        scanValidAggressiveLivingEntity(livingEntity);

            }

        }

    }

    public static void scanValidAggressiveLivingEntity(LivingEntity livingEntity) {

        for (Entity secondEntity : livingEntity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange)) {

            if (!secondEntity.getType().equals(livingEntity.getType())) continue;
            if (!livingEntity.isValid() || !secondEntity.isValid()) continue;

            EliteMobEntity eliteMobEntity1 = null;
            EliteMobEntity eliteMobEntity2 = null;

            int level1 = 1;
            int level2 = 1;

            if (EntityTracker.isEliteMob(livingEntity)) {
                eliteMobEntity1 = EntityTracker.getEliteMobEntity(livingEntity);
                if (!eliteMobEntity1.canStack()) continue;
                level1 = eliteMobEntity1.getLevel();
            }

            if (EntityTracker.isEliteMob(secondEntity)) {
                eliteMobEntity2 = EntityTracker.getEliteMobEntity((LivingEntity) secondEntity);
                if (!eliteMobEntity2.canStack()) continue;
                level2 = eliteMobEntity2.getLevel();
            }

            int newLevel = level1 + level2;

            if (newLevel > ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.ELITEMOB_STACKING_CAP))
                return;

            /*
            This is a bit of a dirty hack
            It won't register the entity twice, but it will overwrite the existing method
             */
            eliteMobEntity1 = EliteMobConstructor.constructEliteMob(livingEntity, newLevel);
            secondEntity.remove();

        }

    }

}
