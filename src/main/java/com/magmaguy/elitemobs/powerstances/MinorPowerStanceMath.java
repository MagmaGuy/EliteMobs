package com.magmaguy.elitemobs.powerstances;

import org.bukkit.util.Vector;

import static com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance.trackAmount;

/**
 * Created by MagmaGuy on 29/05/2017.
 */
public class MinorPowerStanceMath {

    public final static int NUMBER_OF_POINTS_PER_FULL_ROTATION = 30;
    private final static double X = 1;
    private static double Y = 0;
    private final static double Z = 0;
    private static double a, b, c;
    public static Vector[][] cachedVectors = new Vector[trackAmount][NUMBER_OF_POINTS_PER_FULL_ROTATION];

    public static void initializeVectorCache() {

        for (int i = 0; i < trackAmount; i++) {
            //45 degree angle between tracks
            if (i == 0) {
                a = -0.6;
                Y = 0.5;
            } else {
                a = 0.6;
                Y = -0.5;
            }
            b = 1;
            c = 0;
            for (int j = 0; j < NUMBER_OF_POINTS_PER_FULL_ROTATION; j++) {
                Vector rotationVector = GenericRotationMatrixMath.applyRotation(a, b, c,
                        NUMBER_OF_POINTS_PER_FULL_ROTATION, X, Y, Z, j);

                cachedVectors[i][j] = rotationVector;
            }
        }

//        for (int counter = 0; counter < NUMBER_OF_POINTS_PER_FULL_ROTATION; counter++) {
//
//            //get the right track
//            for (int track = 0; track < trackAmount; track++) {
//
//                //45 degree angle between tracks
//                if (track == 0) {
//                    a = -0.6;
//                    Y = 0.5;
//                } else {
//                    a = 0.6;
//                    Y = -0.5;
//                }
//
//                b = 1;
//                c = 0;
//
//                //get the right location
//                for (int item = 0; item < MinorPowerPowerStance.individualEffectsPerTrack; item++) {
//
//                    //add current location
//                    Vector rotationVector = GenericRotationMatrixMath.applyRotation(a, b, c, NUMBER_OF_POINTS_PER_FULL_ROTATION, X, Y, Z,
//                            (int) Math.ceil(counter + NUMBER_OF_POINTS_PER_FULL_ROTATION / MinorPowerPowerStance.individualEffectsPerTrack * item));
//
//                    cachedVectors[counter][track][item] = rotationVector;
//
//                }
//
//            }
//
//        }

    }

}
