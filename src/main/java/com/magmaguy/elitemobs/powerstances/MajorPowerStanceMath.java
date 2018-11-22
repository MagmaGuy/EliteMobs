package com.magmaguy.elitemobs.powerstances;

import org.bukkit.util.Vector;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class MajorPowerStanceMath {

    public final static int NUMBER_OF_POINTS_PER_FULL_ROTATION = 30;
    private final static double X = 0;
    private final static double Y = 1;
    private final static double Z = 0;
    private static double a, b, c;
    public static Vector[][] cachedVectors = new Vector[MajorPowerPowerStance.trackAmount][NUMBER_OF_POINTS_PER_FULL_ROTATION];

    public static void initializeVectorCache() {

        for (int i = 0; i < MajorPowerPowerStance.trackAmount; i++) {

            //45 degree angle between tracks
            a = cos(i * 2 * Math.PI / MajorPowerPowerStance.trackAmount);
            b = 0;
            c = sin(i * 2 * Math.PI / MajorPowerPowerStance.trackAmount);

            //get the right location
            for (int j = 0; j < NUMBER_OF_POINTS_PER_FULL_ROTATION; j++) {

                //add current location
                Vector rotationVector = GenericRotationMatrixMath.applyRotation(a, b, c,
                        NUMBER_OF_POINTS_PER_FULL_ROTATION, X, Y, Z, j);
                cachedVectors[i][j] = rotationVector;

            }

        }

    }

}
