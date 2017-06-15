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

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerStanceMath {

    private static double a, b, c;
    private final static double X = 0;
    private final static double Y = 1;
    private final static double Z = 0;
    public final static double NUMBER_OF_POINTS_PER_FULL_ROTATION = 10;
//    private static HashMap<Integer, List<Vector>> trackHashMap = new HashMap();
    private static HashMap<Integer, HashMap<Integer, List<Vector>>> storedRotatedValues = new HashMap();

    public static HashMap<Integer, List<Vector>> majorPowerLocationConstructor (int trackAmount, int itemsPerTrack, int counter) {

        if (storedRotatedValues.containsKey(counter)) {

            return storedRotatedValues.get(counter);

        }

        HashMap<Integer, List<Vector>> trackHashMap = new HashMap();

        //get the right track
        for (int i = 0; i < trackAmount; i++) {

            //45 degree angle between tracks
            a = cos(i * 2 * Math.PI / trackAmount);
            b = 0;
            c = sin(i * 2 * Math.PI / trackAmount);

            List<Vector> rotations = new ArrayList<>();

            //get the right location
            for (int j = 0; j < itemsPerTrack; j++) {

                //add current location
                Vector rotationVector = GenericRotationMatrixMath.applyRotation(a, b, c, NUMBER_OF_POINTS_PER_FULL_ROTATION, X, Y, Z, (int) Math.ceil(counter + NUMBER_OF_POINTS_PER_FULL_ROTATION / itemsPerTrack * j));
                rotations.add(rotationVector);

            }

            //track done, store
            trackHashMap.put(i, rotations);

        }

        storedRotatedValues.put(counter, trackHashMap);

        return trackHashMap;

    }

}
