/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.mobcustomizer;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobPowersConfig;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPowers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class PowerHandler {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    private static ArrayList<MinorPowers> defensivePowerArray = new ArrayList();
    private static ArrayList<MinorPowers> offensivePowerArray = new ArrayList();
    private static ArrayList<MinorPowers> miscellaneousPowerArray = new ArrayList();

    private static Random random = new Random();

    private static MobPowersConfig mobPowersConfig = new MobPowersConfig();
    private static ZombieTeamRocket zombieTeamRocket = new ZombieTeamRocket();
    private static ZombieNecronomicon zombieNecronomicon = new ZombieNecronomicon();
    private static ZombieParents zombieParents = new ZombieParents();
    private static ZombieFriends zombieFriends = new ZombieFriends();
    private static ZombieBloat zombieBloat = new ZombieBloat();
    private static SkeletonTrackingArrow skeletonTrackingArrow = new SkeletonTrackingArrow();

    public static void powerHandler(Entity entity) {

        if (entity.hasMetadata(MetadataHandler.CUSTOM_POWERS_MD)) return;

        if (defensivePowerArray.isEmpty()) defensivePowerArrayInitializer();
        if (offensivePowerArray.isEmpty()) offensivePowerArrayInitializer();
        if (miscellaneousPowerArray.isEmpty()) miscellaneousPowerArrayInitializer();

        ArrayList<MinorPowers> defensivePowerArrayCopy = (ArrayList<MinorPowers>) defensivePowerArray.clone();
        ArrayList<MinorPowers> offensivePowerArrayCopy = (ArrayList<MinorPowers>) offensivePowerArray.clone();
        ArrayList<MinorPowers> miscellaneousPowerArrayCopy = (ArrayList<MinorPowers>) miscellaneousPowerArray.clone();

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && entity.isValid() && ((LivingEntity) entity).getHealth() > 0) {

            if (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= 10) {

                int eliteMobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

                if (eliteMobLevel >= 50) availableDefensivePowers = 1;
                if (eliteMobLevel >= 100) availableOffensivePowers = 1;
                if (eliteMobLevel >= 150) availableMiscellaneousPowers = 1;
                if (eliteMobLevel >= 200) availableMajorPowers = 1;
                if (eliteMobLevel >= 250) availableDefensivePowers = 2;
                if (eliteMobLevel >= 300) availableOffensivePowers = 2;
                if (eliteMobLevel >= 350) availableMiscellaneousPowers = 2;
                if (eliteMobLevel >= 400) availableMajorPowers = 2;

            }

        }

        //apply defensive powers
        applyMinorPowers(entity, availableDefensivePowers, defensivePowerArrayCopy, MetadataHandler.DEFENSIVE_POWER_AMOUNT_MD);

        //apply offensive powers
        applyMinorPowers(entity, availableOffensivePowers, offensivePowerArrayCopy, MetadataHandler.OFFENSIVE_POWER_AMOUNT_MD);

        //apply miscellaneous powers
        applyMinorPowers(entity, availableMiscellaneousPowers, miscellaneousPowerArrayCopy, MetadataHandler.MISCELLANEOUS_POWER_AMOUNT_MD);

        //apply major powers
        applyMajorPowers(entity, availableMajorPowers);

    }

    private static void applyMinorPowers(Entity entity, int availableMinorPowers, ArrayList<MinorPowers> minorPowerArrayCopy, String minorPowerAmountMetadata) {

        if (availableMinorPowers > 0) {

            int currentMinorPowerAmount = 0;

            if (entity.hasMetadata(minorPowerAmountMetadata)) {

                currentMinorPowerAmount = entity.getMetadata(minorPowerAmountMetadata).get(0).asInt();

                Iterator<MinorPowers> minorPowerIterator = minorPowerArrayCopy.iterator();

                while (minorPowerIterator.hasNext()) {

                    MinorPowers minorPowers = minorPowerIterator.next();

                    if (minorPowers.existingPowers(entity)) {

                        minorPowerIterator.remove();

                    }

                }

            }

            int missingMinorPowerAmount = availableMinorPowers - currentMinorPowerAmount;

            if (missingMinorPowerAmount > 0 && minorPowerArrayCopy.size() > 0) {

                for (int i = 0; i < missingMinorPowerAmount; i++) {

                    if (minorPowerArrayCopy.size() > 0) {

                        int randomizer = random.nextInt(minorPowerArrayCopy.size());
                        MinorPowers selectedMinorPower = minorPowerArrayCopy.get(randomizer);
                        minorPowerArrayCopy.remove(selectedMinorPower);

                        if (entity.hasMetadata(minorPowerAmountMetadata)) {

                            int oldMinorPowerAmount = entity.getMetadata(minorPowerAmountMetadata).get(0).asInt();
                            int newMinorPowerAmount = oldMinorPowerAmount + 1;

                            entity.setMetadata(minorPowerAmountMetadata, new FixedMetadataValue(plugin, newMinorPowerAmount));

                        } else {

                            entity.setMetadata(minorPowerAmountMetadata, new FixedMetadataValue(plugin, 1));

                        }

                        selectedMinorPower.applyPowers(entity);

                    }

                }

            }

        }

    }

    private static void applyMajorPowers(Entity entity, int availableMajorPowers) {

        if (availableMajorPowers >= 1) {

            int currentMajorPowerAmount = 0;

            if (entity.hasMetadata(MetadataHandler.CUSTOM_POWERS_MD)) {

                return;

            }

            ArrayList<MajorPowers> majorPowersArrayList = new ArrayList();

            if (majorPowersArrayList.isEmpty()) {

                if (entity instanceof Zombie) {

                    //These powers can't be intialized like minor powers because the list depends on the mob type
                    //TODO: Add a mob type sensitive power list
                    if (ConfigValues.mobPowerConfig.getBoolean("Powers.Major Powers.ZombieFriends")) {

                        majorPowersArrayList.add(zombieFriends);

                    }

                    if (ConfigValues.mobPowerConfig.getBoolean("Powers.Major Powers.ZombieNecronomicon")) {

                        majorPowersArrayList.add(zombieNecronomicon);

                    }

                    if (ConfigValues.mobPowerConfig.getBoolean("Powers.Major Powers.ZombieTeamRocket")) {

                        majorPowersArrayList.add(zombieTeamRocket);

                    }

                    if (ConfigValues.mobPowerConfig.getBoolean("Powers.Major Powers.ZombieParents")) {

                        majorPowersArrayList.add(zombieParents);

                    }

                    if (ConfigValues.mobPowerConfig.getBoolean("Powers.Major Powers.ZombieBloat")) {

                        majorPowersArrayList.add(zombieBloat);

                    }

                }

                if (entity instanceof Skeleton) {

                    if (ConfigValues.mobPowerConfig.getBoolean("Powers.Major Powers.SkeletonTrackingArrow")) {

                        majorPowersArrayList.add(skeletonTrackingArrow);

                    }

                }

            }

            if (entity.hasMetadata(MetadataHandler.MAJOR_POWER_AMOUNT_MD)) {

                currentMajorPowerAmount = entity.getMetadata(MetadataHandler.MAJOR_POWER_AMOUNT_MD).get(0).asInt();

                Iterator<MajorPowers> majorPowerIterator = majorPowersArrayList.iterator();

                while (majorPowerIterator.hasNext()) {

                    MajorPowers majorPowers = majorPowerIterator.next();

                    if (majorPowers.existingPowers(entity)) {

                        majorPowerIterator.remove();

                    }

                }

            }

            int missingMajorPowerAmount = availableMajorPowers - currentMajorPowerAmount;

            if (missingMajorPowerAmount > 0 && majorPowersArrayList.size() > 0) {

                for (int i = 0; i < missingMajorPowerAmount; i++) {

                    if (majorPowersArrayList.size() > 0) {

                        int randomizer = random.nextInt(majorPowersArrayList.size());
                        MajorPowers selectedMajorPower = majorPowersArrayList.get(randomizer);
                        majorPowersArrayList.remove(majorPowersArrayList.get(randomizer));

                        if (entity.hasMetadata(MetadataHandler.MAJOR_POWER_AMOUNT_MD)) {

                            int oldMajorPowerAmount = entity.getMetadata(MetadataHandler.MAJOR_POWER_AMOUNT_MD).get(0).asInt();
                            int newMajorPowerAmount = oldMajorPowerAmount + 1;

                            entity.setMetadata(MetadataHandler.MAJOR_POWER_AMOUNT_MD, new FixedMetadataValue(plugin, newMajorPowerAmount));

                        } else {

                            entity.setMetadata(MetadataHandler.MAJOR_POWER_AMOUNT_MD, new FixedMetadataValue(plugin, 1));

                        }

                        selectedMajorPower.applyPowers(entity);

                    }

                }

            }

        }

    }

    public static void defensivePowerArrayInitializer() {

        for (String string : MetadataHandler.defensivePowerList) {

            if (ConfigValues.mobPowerConfig.getBoolean("Powers.Defensive Powers." + string)) {

                try {

                    String earlyPath = "com.magmaguy.elitemobs.mobpowers.defensivepowers.";

                    String finalString = earlyPath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    defensivePowerArray.add((MinorPowers) instance);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public static void offensivePowerArrayInitializer() {

        for (String string : MetadataHandler.offensivePowerList) {

            if (ConfigValues.mobPowerConfig.getBoolean("Powers.Offensive Powers." + string)) {

                try {

                    String earlyPath = "com.magmaguy.elitemobs.mobpowers.offensivepowers.";

                    String finalString = earlyPath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    offensivePowerArray.add((MinorPowers) instance);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public static void miscellaneousPowerArrayInitializer() {

        for (String string : MetadataHandler.miscellaneousPowerList) {

            if (ConfigValues.mobPowerConfig.getBoolean("Powers.Miscellaneous Powers." + string)) {

                try {

                    String earlyPath = "com.magmaguy.elitemobs.mobpowers.miscellaneouspowers.";

                    String finalString = earlyPath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    miscellaneousPowerArray.add((MinorPowers) instance);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }

    }

}
