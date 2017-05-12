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

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerStanceMath {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    private static double a, b, c;
    private final static double X = 0;
    private final static double Y = 1;
    private final static double Z = 0;
    private final static double NUMBER_OF_POINTS_PER_FULL_ROTATION = 30;
    private static int counter;
    private static HashMap<Integer, List<Location>> trackHashMap = new HashMap();

    public static HashMap<Integer, List<Location>> majorPowerLocationConstructor (Entity entity, int trackAmount, int itemsPerTrack) {

        counter = entity.getMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD).get(0).asInt();

        //get the right track
        for (int i = 0; i < trackAmount; i++) {

            //45 degree angle between tracks
            a = cos(i * 180 / trackAmount);
            b = 0;
            c = sin(i * 180 / trackAmount);

            List<Location> locations = new ArrayList<>();

            //get the right location
            for (int j = 0; j < itemsPerTrack; j++) {

                //add current location

                locations.add(GenericRotationMatrixMath.applyRotation(entity, a, b, c, NUMBER_OF_POINTS_PER_FULL_ROTATION, X, Y, Z, (int) Math.ceil(counter + NUMBER_OF_POINTS_PER_FULL_ROTATION / itemsPerTrack * j)));

            }

            //track done, store
            trackHashMap.put(i, locations);

        }

        //increment counter for next run
        Bukkit.getLogger().info("" + counter);
        counter++;
        entity.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, counter));

        return trackHashMap;

    }

}
