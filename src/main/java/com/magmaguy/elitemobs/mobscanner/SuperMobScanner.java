package com.magmaguy.elitemobs.mobscanner;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

public class SuperMobScanner {

    private static int passiveRange = ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.PASSIVE_STACK_RANGE);

    public static void scanSuperMobs() {

        for (World world : validWorldList) {

            if (world.getLivingEntities().isEmpty()) continue;

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            new BukkitRunnable() {

                @Override
                public void run() {

                    while (iterator.hasNext()) {

                        LivingEntity livingEntity = iterator.next();

                        if (!SuperMobProperties.isValidSuperMobType(livingEntity)) continue;

                        /*
                        Re-register lost passive mob
                        */
                        checkLostSuperMob(livingEntity);

                        /*
                       Check passive mobs to register new super mobs
                       */
                        newSuperMobScan(livingEntity);

                    }

                }

            }.runTaskAsynchronously(MetadataHandler.PLUGIN);

        }

    }

    private static void checkLostSuperMob(LivingEntity livingEntity) {

        if (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() !=
                EliteMobProperties.getPluginData(livingEntity).getSuperMobMaxHealth())
            return;

        if (!EntityTracker.isSuperMob(livingEntity))
            EntityTracker.registerSuperMob(livingEntity);

    }

    public static void newSuperMobScan(LivingEntity livingEntity) {

        if (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() ==
                EliteMobProperties.getPluginData(livingEntity).getSuperMobMaxHealth())
            return;

        if (ThreadLocalRandom.current().nextDouble() > 1 / ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT))
            return;

        ArrayList<LivingEntity> livingEntities = new ArrayList<>();

        for (Entity entity : livingEntity.getNearbyEntities(passiveRange, passiveRange, passiveRange)) {

            if (!entity.getType().equals(livingEntity.getType())) continue;
            if (livingEntities.size() >= ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT))
                break;

        }

        if (livingEntities.size() < ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT))
            return;


    }

}
