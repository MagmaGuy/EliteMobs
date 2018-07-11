package com.magmaguy.elitemobs.powerstances;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisualItemProcessor {

    public static int adjustTrackPosition(int effectQuantity, int globalPositionCounter, int individualPositionCounter, double numberOfPointsPerRotation, int itemsPerTrack) {
        return (int) ((numberOfPointsPerRotation / itemsPerTrack) / effectQuantity * individualPositionCounter + globalPositionCounter);
    }

    public static void itemProcessor (HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker, ItemStack itemStack,
                               int effectIteration, Entity entity, HashMap<Integer, List<Vector>> trackHashMap,
                               int trackAmount, int itemsPerTrack) {

        boolean effectAlreadyPresent = false;

        if (!powerItemLocationTracker.isEmpty()) {

            for (int i = 0; i < powerItemLocationTracker.size(); i++) {

                if (powerItemLocationTracker.get(i).get(0).get(0).getItemStack().getType().equals(itemStack.getType())) {

                    effectAlreadyPresent = true;
                    break;

                }

            }

        }

        Location centerLocation = entity.getLocation().add(new Vector(0, 1, 0));

        if (!effectAlreadyPresent) {

            HashMap<Integer, List<Item>> tempMap = new HashMap<>();

            for (int i = 0; i < trackAmount; i++) {

                List<Item> newItemList = new ArrayList<>();

                for (int j = 0; j < itemsPerTrack; j++) {

                    newItemList.add(VisualItemInitializer.intializeItem(itemStack, entity.getLocation()));

                }

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
                    item.setVelocity(vector);

                }

            }

        }

    }

}
