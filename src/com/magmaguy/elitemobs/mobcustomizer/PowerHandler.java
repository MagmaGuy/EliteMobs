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

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.minorpowers.MinorPowers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class PowerHandler {

    private EliteMobs plugin;

    public PowerHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    public void powerHandler(Entity entity) {

        int availableMinorPowers = 0;
        int availableMajorPowers = 0;

        MetadataHandler metadataHandler = new MetadataHandler(plugin);
        if (entity.hasMetadata(metadataHandler.eliteMobMD) && entity.isValid() && ((LivingEntity) entity).getHealth() > 0) {

            if (entity.getMetadata(metadataHandler.eliteMobMD).get(0).asInt() >= 5) {

                int EliteMobLevel = entity.getMetadata(metadataHandler.eliteMobMD).get(0).asInt();

                availableMinorPowers = (EliteMobLevel - 5) / 10 + 1;
                availableMajorPowers = EliteMobLevel / 10;

            }

        }

        if (availableMinorPowers >= 1) {

            int currentMinorPowerAmount = 0;

            if (entity.hasMetadata("custom")) {

                return;

            }

            ArrayList<MinorPowers> minorPowerArray = new ArrayList();

            for (String string : metadataHandler.minorPowerList()) {

                if (plugin.getConfig().getList("Valid aggressive EliteMobs powers").contains(string)){

                    try {

                        String earlyPath = "com.magmaguy.elitemobs.minorpowers.";

                        String finalString = earlyPath + string;

                        Class<?> clazz = Class.forName(finalString);

                        Constructor <?> constructor = clazz.getConstructor(Plugin.class);

                        Object instance = constructor.newInstance(plugin);

                        minorPowerArray.add((MinorPowers) instance);

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }

            }

            if (entity.hasMetadata("MinorPowerAmount")) {

                currentMinorPowerAmount = entity.getMetadata("MinorPowerAmount").get(0).asInt();

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

                        Random random = new Random();
                        int randomizer = random.nextInt(minorPowerArray.size());
                        MinorPowers selectedMinorPower = minorPowerArray.get(randomizer);
                        minorPowerArray.remove(minorPowerArray.get(randomizer));

                        if (entity.hasMetadata(metadataHandler.minorPowerAmountMD)) {

                            int oldMinorPowerAmount = entity.getMetadata(metadataHandler.minorPowerAmountMD).get(0).asInt();
                            int newMinorPowerAmount = oldMinorPowerAmount + 1;

                            entity.setMetadata(metadataHandler.minorPowerAmountMD, new FixedMetadataValue(plugin, newMinorPowerAmount));

                        } else {

                            entity.setMetadata(metadataHandler.minorPowerAmountMD, new FixedMetadataValue(plugin, 1));

                        }

                        selectedMinorPower.applyPowers(entity);

                    }

                }

            }

        }

        if (availableMajorPowers >= 1) {

            //Todo: Add major powers

        }

    }
}
