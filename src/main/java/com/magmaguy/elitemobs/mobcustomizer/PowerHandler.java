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
import com.magmaguy.elitemobs.config.MobPowersCustomConfig;
import com.magmaguy.elitemobs.majorpowers.*;
import com.magmaguy.elitemobs.minorpowers.MinorPowers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    private static ArrayList<MinorPowers> minorPowerArray = new ArrayList();

    private static Random random = new Random();

    private static MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
    private static ZombieTeamRocket zombieTeamRocket = new ZombieTeamRocket();
    private static ZombieNecronomicon zombieNecronomicon = new ZombieNecronomicon();
    private static ZombieParents zombieParents = new ZombieParents();
    private static ZombieFriends zombieFriends = new ZombieFriends();

    public static void powerHandler(Entity entity) {

        int availableMinorPowers = 0;
        int availableMajorPowers = 0;

        if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && entity.isValid() && ((LivingEntity) entity).getHealth() > 0) {

            if (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= 5) {

                int EliteMobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

                availableMinorPowers = (EliteMobLevel - 5) / 10 + 1;
                availableMajorPowers = EliteMobLevel / 10;

            }

        }

        if (availableMinorPowers >= 1) {

            int currentMinorPowerAmount = 0;

            if (entity.hasMetadata(MetadataHandler.CUSTOM_POWERS_MD)) {

                return;

            }

            if (minorPowerArray.isEmpty()) {

                minorPowerArrayInitializer();

            }

            if (entity.hasMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD)) {

                currentMinorPowerAmount = entity.getMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD).get(0).asInt();

                //TODO: this is probably not working as intended
                Iterator<MinorPowers> minorPowerIterator = minorPowerArray.iterator();

                while (minorPowerIterator.hasNext()) {

                    MinorPowers minorPower = minorPowerIterator.next();

                    if (minorPower.existingPowers(entity)) {

                        minorPowerIterator.remove();

                    }

                }

            }

            int missingMinorPowerAmount = availableMinorPowers - currentMinorPowerAmount;

            if (missingMinorPowerAmount > 0 && minorPowerArray.size() > 0) {

                for (int i = 0; i < missingMinorPowerAmount; i++) {

                    if (minorPowerArray.size() > 0) {

                        int randomizer = random.nextInt(minorPowerArray.size());
                        MinorPowers selectedMinorPower = minorPowerArray.get(randomizer);
                        minorPowerArray.remove(minorPowerArray.get(randomizer));

                        if (entity.hasMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD)) {

                            int oldMinorPowerAmount = entity.getMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD).get(0).asInt();
                            int newMinorPowerAmount = oldMinorPowerAmount + 1;

                            entity.setMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD, new FixedMetadataValue(plugin, newMinorPowerAmount));

                        } else {

                            entity.setMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD, new FixedMetadataValue(plugin, 1));

                        }

                        selectedMinorPower.applyPowers(entity);

                    }

                }

            }

        }

        if (availableMajorPowers >= 1) {

            int currentMajorPowerAmount = 0;

            if (entity.hasMetadata(MetadataHandler.CUSTOM_POWERS_MD)){

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

            //TODO: pretty sure this doesn't check for level 10?
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


    public static void minorPowerArrayInitializer () {

        for (String string : MetadataHandler.minorPowerList()) {

            if (mobPowersCustomConfig.getMobPowersConfig().getBoolean("Powers.Minor Powers." + string)){

                try {

                    String earlyPath = "com.magmaguy.elitemobs.minorpowers.";

                    String finalString = earlyPath + string;

                    Class<?> clazz = Class.forName(finalString);

                    Object instance = clazz.newInstance();

                    minorPowerArray.add((MinorPowers) instance);

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
