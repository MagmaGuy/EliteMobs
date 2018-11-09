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

package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.majorpowers.*;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerPowerStance implements Listener {

    private static int trackAmount = 4;
    private static int itemsPerTrack = 2;

    public void itemEffect(Entity entity) {

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS))
            return;
        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS)
                && !EntityTracker.isNaturalEntity(entity))
            return;
        if (!EntityTracker.isEliteMob(entity)) return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity((LivingEntity) entity);

        if (eliteMobEntity.hasMajorVisualEffect()) return;

        eliteMobEntity.setHasMajorVisualEffect(true);

        //First integer counts the visual effects it's in, hashmap is from MajorPowerStance's trackHashMap
        HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker = new HashMap<>();

        new BukkitRunnable() {

            int globalPositionCounter = 1;
            int effectQuantity = eliteMobEntity.getMajorPowers().size();

            public void run() {

                int itemStackHashMapPosition = 0;
                int individualPositionCounter = 0;
                int effectQuantityChecksum = 0;

                if (!entity.isValid() ||
                        (!eliteMobEntity.isNaturalEntity() &&
                                ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS))) {

                    VisualItemRemover.removeItems(powerItemLocationTracker, trackAmount, itemsPerTrack);
                    cancel();
                    return;

                }

                effectQuantityChecksum = eliteMobEntity.getMajorPowers().size();

                if (effectQuantity != effectQuantityChecksum) {
                    VisualItemRemover.removeItems(powerItemLocationTracker, trackAmount, itemsPerTrack);
                    powerItemLocationTracker.clear();
                    effectQuantity = effectQuantityChecksum;
                }

                if (globalPositionCounter >= MinorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION)
                    globalPositionCounter = 0;

                for (MajorPower majorPower : eliteMobEntity.getMajorPowers()) {

                    if (majorPower instanceof ZombieFriends) {

                        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (majorPower instanceof ZombieNecronomicon) {

                        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (majorPower instanceof ZombieTeamRocket) {

                        ItemStack itemStack = new ItemStack(Material.FIREWORK, 1);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (majorPower instanceof ZombieParents) {

                        ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) 0, (byte) 54);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (majorPower instanceof ZombieBloat) {

                        ItemStack itemStack = new ItemStack(Material.RED_MUSHROOM, 1);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (majorPower instanceof SkeletonTrackingArrow) {

                        ItemStack itemStack = new ItemStack(Material.TIPPED_ARROW, 1);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                    if (majorPower instanceof SkeletonPillar) {

                        ItemStack itemStack = new ItemStack(Material.BONE, 1);

                        applyRotation(powerItemLocationTracker, itemStack, entity, effectQuantity, globalPositionCounter,
                                individualPositionCounter, itemStackHashMapPosition);

                        individualPositionCounter++;
                        itemStackHashMapPosition++;

                    }

                }

                globalPositionCounter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);

    }

    private void applyRotation(HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker, ItemStack itemStack,
                               Entity entity, int effectQuantity, int globalPositionCounter,
                               int individualPositionCounter, int itemStackHashMapPosition) {

        int counter = VisualItemProcessor.adjustTrackPosition(effectQuantity, globalPositionCounter,
                individualPositionCounter, MajorPowerStanceMath.NUMBER_OF_POINTS_PER_FULL_ROTATION, itemsPerTrack);
        VisualItemProcessor.itemProcessor(powerItemLocationTracker, itemStack, itemStackHashMapPosition, entity,
                MajorPowerStanceMath.majorPowerLocationConstructor(trackAmount, itemsPerTrack, counter), trackAmount,
                itemsPerTrack);

    }

}
