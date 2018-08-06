package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.Item;

import java.util.HashMap;
import java.util.List;

public class VisualItemRemover {

    public static void removeItems(HashMap<Integer, HashMap<Integer, List<Item>>> powerItemLocationTracker, int trackAmount, int itemsPerTrack) {

        for (int i = 0; i < powerItemLocationTracker.size(); i++) {

            for (int j = 0; j < trackAmount; j++) {

                for (int h = 0; h < itemsPerTrack; h++) {

                    powerItemLocationTracker.get(i).get(j).get(h).remove();
                    MetadataHandler.fullMetadataFlush(powerItemLocationTracker.get(i).get(j).get(h));

                }

            }

        }

    }

}
