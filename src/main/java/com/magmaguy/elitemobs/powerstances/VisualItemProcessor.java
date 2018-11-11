package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisualItemProcessor {

    private boolean hasValidEffect;

    public VisualItemProcessor(Object[][] multiDimensionalTrailTracker, Vector[][] cachedVectorPositions,
                               boolean visualEffectBoolean, int pointsPerRotation, int effectsPerTrack, EliteMobEntity eliteMobEntity) {

        this.hasValidEffect = visualEffectBoolean;
        if (multiDimensionalTrailTracker.length < 1)
            return;

        /*
        This shouldn't happen but if an entity already has visual effects going, don't start a second repeating task with it
         */
        if (this.hasValidEffect)
            return;

        rotateExistingEffects(multiDimensionalTrailTracker, cachedVectorPositions, pointsPerRotation, effectsPerTrack, eliteMobEntity);

    }

    private void rotateExistingEffects(Object[][] multiDimensionalTrailTracker, Vector[][] cachedVectorPositions,
                                       int pointsPerRotation, int individualEffectsPerTrack, EliteMobEntity eliteMobEntity) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (!eliteMobEntity.getLivingEntity().isValid() || !hasValidEffect) {
                    VisualItemRemover.removeItems(multiDimensionalTrailTracker);
                    cancel();
                    return;
                }

                for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                    int sectionCounter = 0;
                    for (int j = 0; j < multiDimensionalTrailTracker[i].length; j++) {

                        int adjustedEffectPositionInRotation = adjustTrackPosition(
                                pointsPerRotation,
                                individualEffectsPerTrack,
                                multiDimensionalTrailTracker[i].length / individualEffectsPerTrack,
                                sectionCounter,
                                counter);

                        Bukkit.getLogger().warning("I:" + i);
                        Bukkit.getLogger().warning("Adjusted:" + adjustedEffectPositionInRotation);
                        Vector vector = cachedVectorPositions[i][adjustedEffectPositionInRotation];

                        if (multiDimensionalTrailTracker[i][j] instanceof Item)
                            rotateItem(multiDimensionalTrailTracker[i][j], vector, eliteMobEntity);

                        if (multiDimensionalTrailTracker[i][j] instanceof Particle)
                            rotateParticle(multiDimensionalTrailTracker[i][j], vector);

                        sectionCounter++;
                        if (sectionCounter >= pointsPerRotation / individualEffectsPerTrack)
                            sectionCounter = 0;

                    }
                }

                counter++;
                if (counter >= pointsPerRotation)
                    counter = 0;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);

    }

    /*
    Adjusts the position in each item track based on the amount of powers currently in that track
     */
    public static int adjustTrackPosition(double pointsPerRotation, int individualEffectsPerTrack, int totalEffectQuantity,
                                          int sectionCounter, int globalCounter) {
        int location = (int) ((pointsPerRotation / individualEffectsPerTrack) / totalEffectQuantity * sectionCounter
                + globalCounter);
        if (location >= 30)
            location = 0;
        return location;
    }

    private void rotateItem(Object itemObject, Vector vector, EliteMobEntity eliteMobEntity) {

        Item item = (Item) itemObject;

        if (!item.isValid())
            return;

        Location currentLocation = item.getLocation().clone();
        Location newLocation = eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 1, 0)).add(vector);

        if (currentLocation.distanceSquared(newLocation) > Math.pow(3, 2)) {
            item.teleport(newLocation);
            item.setVelocity(new Vector(0.01, 0.01, 0.01));
            return;
        }

        Vector movementVector = (newLocation.subtract(currentLocation)).toVector();
        movementVector = movementVector.multiply(0.3);

        if (Math.abs(movementVector.getX()) > 3 || Math.abs(movementVector.getY()) > 3 || Math.abs(movementVector.getZ()) > 3) {
            item.teleport(newLocation);
        } else {
            item.setVelocity(movementVector);
        }

    }

    private void rotateParticle(Object particleObject, Vector vector) {
        Particle particle = (Particle) particleObject;
    }


    public static void itemProcessor(HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker, ItemStack itemStack,
                                     int effectIteration, Entity entity, HashMap<Integer, List<Vector>> trackHashMap,
                                     int trackAmount, int itemsPerTrack) {

        boolean effectAlreadyPresent = false;

        if (!powerItemLocationTracker.isEmpty())
            for (int i = 0; i < powerItemLocationTracker.size(); i++)
                if (powerItemLocationTracker.get(i).get(0).get(0).getItemStack().getType().equals(itemStack.getType())) {
                    effectAlreadyPresent = true;
                    break;
                }

        Location centerLocation = entity.getLocation().add(new Vector(0, 1, 0));

        if (!effectAlreadyPresent) {

            HashMap<Integer, List<Item>> tempMap = new HashMap<>();

            for (int i = 0; i < trackAmount; i++) {

                List<Item> newItemList = new ArrayList<>();

                for (int j = 0; j < itemsPerTrack; j++)
                    newItemList.add(VisualItemInitializer.intializeItem(itemStack, entity.getLocation()));

                //same format as trackHashMap
                tempMap.put(i, newItemList);

            }

            powerItemLocationTracker.put(effectIteration, tempMap);

        } else {

            //iterate through the tracks
            for (int i = 0; i < trackAmount; i++) {

                for (int j = 0; j < itemsPerTrack; j++) {

                    Item item = powerItemLocationTracker.get(effectIteration).get(i).get(j);
                    Location newLocation = new Location(entity.getWorld(), trackHashMap.get(i).get(j).getX(),
                            trackHashMap.get(i).get(j).getY(), trackHashMap.get(i).get(j).getZ()).add(centerLocation);
                    Location currentLocation = item.getLocation();

                    if (item.getWorld() != entity.getWorld()) {

                        item.teleport(item.getLocation());

                    }

                    Vector vector = (newLocation.subtract(currentLocation)).toVector();
                    vector = vector.multiply(0.3);
                    if (Math.abs(vector.getX()) > 3 || Math.abs(vector.getY()) > 3 || Math.abs(vector.getZ()) > 3) {
                        item.teleport(entity.getLocation());
                        item.setVelocity(new Vector(0.01, 0.01, 0.01));
                    } else
                        item.setVelocity(vector);

                }

            }

        }

    }

}
