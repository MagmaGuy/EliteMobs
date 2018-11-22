package com.magmaguy.elitemobs.mobscanner;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

public class EliteMobScanner {

    private static int aggressiveRange = ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.AGGRESSIVE_STACK_RANGE);

    public static void scanElites() {

        for (World world : validWorldList) {

            if (world.getLivingEntities().isEmpty()) continue;

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            new BukkitRunnable() {

                @Override
                public void run() {

                    while (iterator.hasNext()) {

                        LivingEntity livingEntity = iterator.next();

                        if (!EliteMobProperties.isValidEliteMobType(livingEntity)) continue;

                        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_NATURAL_MOBS))
                            if (EntityTracker.isNaturalEntity(livingEntity))
                                scanValidAggressiveLivingEntity(livingEntity);

                    }

                }

            }.runTask(MetadataHandler.PLUGIN);

        }

    }

    public static void scanValidAggressiveLivingEntity(LivingEntity livingEntity) {

        if (!EntityTracker.isNaturalEntity(livingEntity) &&
                !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_SPAWNER_MOBS))
            return;
        if (EntityTracker.isNaturalEntity(livingEntity) &&
                !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_NATURAL_MOBS))
            return;

        for (Entity secondEntity : livingEntity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange)) {

            if (!secondEntity.getType().equals(livingEntity.getType())) continue;
            if (!livingEntity.isValid() || !secondEntity.isValid()) continue;

            if (!EntityTracker.isNaturalEntity(secondEntity) &&
                    !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_SPAWNER_MOBS))
                return;
            if (EntityTracker.isNaturalEntity(secondEntity) &&
                    !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_NATURAL_MOBS))
                return;


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
                eliteMobEntity2 = EntityTracker.getEliteMobEntity(secondEntity);
                if (!eliteMobEntity2.canStack()) continue;
                level2 = eliteMobEntity2.getLevel();
            }

            int newLevel = level1 + level2;

            if (newLevel > ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.ELITEMOB_STACKING_CAP))
                return;

            double healthPercentage = (livingEntity.getHealth() / livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() +
                    ((LivingEntity) secondEntity).getHealth() / ((LivingEntity) secondEntity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) / 2;

            /*
            This is a bit of a dirty hack
            It won't register the entity twice, but it will overwrite the existing method
             */
            eliteMobEntity1 = new EliteMobEntity(livingEntity, newLevel, healthPercentage);
            secondEntity.remove();

        }

    }

}
