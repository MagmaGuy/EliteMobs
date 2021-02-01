package com.magmaguy.elitemobs.powerstances;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import static java.lang.Math.*;

/**
 * Created by MagmaGuy on 11/05/2017.
 */
public class GenericRotationMatrixMath {

    /*
        Logic: Rotation matrix makes something rotate counter-clockwise by theta. The result of this calc is a rotated location.
        The a b c values define the vector in question. The theta defines the angle of the rotation. The x y z define a
        point that will be rotated counter-clockwise around the aforementioned vector by theta degrees. The radius of the
        rotation is defined by the distance of the values x y z from 0, 0, 0.

        In the case of Minor Powers, the vector is 0, 1, 0 so locations can rotate around the vertical axis. The initial
        point is 0.5, 0, 0 so the radius of the location is 0.5 away. Subsequent points are calculated by getting the
        previous point and applying the same rotation (using the counter). It doesn't ever change the vector, as the
        vertical location is changed differently elsewhere to attain a cylindrical effect instead of concentric circles.

        In the case of Major Powers , the vector is 1, 0, 0 so the locations rotate counter-clockwise around an horizontal
        vector. The initial point is 0, 1, 0 so the radius of the rotated location is 1 unit away vertically from the entity.
        The vector gets switched around in the x and z axis so that the effect pivots in the horizontal axis while still
        keeping a concentric nature and the same point of origin.

        The rotation matrix multiplication with coordinates is as follows:
        M(v, theta) = |cos(theta) + (1 - cos(theta)) * a^2    (1 - cos(theta)) * a * b + (sin(theta)) * c    (1 - cos(theta)) * a * c - (sin(theta)) * b| |x|
                      |(1 - cos(theta)) * b * a - (sin (theta)) * c    cos(theta) + (1 - cos(theta)) * b^2    (1 - cos(theta) * b * c + (sin(theta)) * a| |y|
                      |(1 - cos(theta)) * c * a + (sin(theta)) * b    (1 - cos(theta)) * c * b - (sin(theta)) * a    cos(theta) + (1 - cos(theta)) * c^2| |z|

        newX = x * (cos(theta) + (1 - cos(theta)) * a^2) + y * ((1 - cos(theta)) * a * b + (sin(theta))) * c) + z * ((1 - cos(theta)) * a * c - (sin(theta)) * b)
        newY = x * ((1 - cos(theta)) * b * a - (sin (theta)) * c) + y * (cos(theta) + (1 - cos(theta)) * b^2) + z * ((1 - cos(theta) * b * c + (sin(theta)) * a)
        newZ = x * ((1 - cos(theta)) * c * a + (sin(theta)) * b) + y * ((1 - cos(theta)) * c * b - (sin(theta)) * a) + z * (cos(theta) + (1 - cos(theta)) * c^2)

        Quick tweak list:
        a, b, c: Total should be 1. Changes the angle of vector around which the items rotate.
        Theta: Changes the speed at which a location will rotate around the reference point.
        x, y, z: Changes the radius and initial location of the location to be rotated around the vector.
        counter: increments so there can be full rotations. Can be used to predict future or past locations (to add more
        elements per track for example).

        Instead of dealing with theta directly, by doing 360/theta you can pick how many points a single rotation has making
        it easier to predict where things are supposed to be.
    */

    public static Vector applyRotation(double a, double b, double c, double numberOfPointsPerFullRotation, double x, double y, double z, int counter) {

        double theta;
        //Adapt numberOfPointsPerFullRotation to 360 degrees
        //FUCKING RADIAN (2*pi is 360 degrees for a radius of 1)
        theta = 2 * Math.PI / numberOfPointsPerFullRotation;
        //Get the next rotation
        theta = theta + theta * counter;

        double newX = x * (cos(theta) + (1 - cos(theta)) * pow(a, 2)) + y * ((1 - cos(theta)) * a * b + (sin(theta)) * c) + z * ((1 - cos(theta)) * a * c - (sin(theta)) * b);
        double newY = x * ((1 - cos(theta)) * b * a - (sin(theta)) * c) + y * (cos(theta) + (1 - cos(theta)) * pow(b, 2)) + z * ((1 - cos(theta)) * b * c + (sin(theta)) * a);
        double newZ = x * ((1 - cos(theta)) * c * a + (sin(theta)) * b) + y * ((1 - cos(theta)) * c * b - (sin(theta)) * a) + z * (cos(theta) + (1 - cos(theta)) * pow(c, 2));

        //adjust rotated point
        Vector newVector = new Vector(newX, newY, newZ);

        return newVector;

    }

    private static Vector rotateSpecificLocation(double a, double b, double c, double theta, Vector relativeLocation) {
        double x = relativeLocation.getX();
        double y = relativeLocation.getY();
        double z = relativeLocation.getZ();
        //Convert radian to degree
        theta *= PI / 180;

        double newX = x * (cos(theta) + (1 - cos(theta)) * pow(a, 2)) + y * ((1 - cos(theta)) * a * b + (sin(theta)) * c) + z * ((1 - cos(theta)) * a * c - (sin(theta)) * b);
        double newY = x * ((1 - cos(theta)) * b * a - (sin(theta)) * c) + y * (cos(theta) + (1 - cos(theta)) * pow(b, 2)) + z * ((1 - cos(theta)) * b * c + (sin(theta)) * a);
        double newZ = x * ((1 - cos(theta)) * c * a + (sin(theta)) * b) + y * ((1 - cos(theta)) * c * b - (sin(theta)) * a) + z * (cos(theta) + (1 - cos(theta)) * pow(c, 2));

        return new Vector(newX, newY, newZ);
    }

    public static Location rotateLocationYAxis(double rotationAngleInDegrees, Location anchorPoint, Vector relativeLocation) {
        //todo: fix this
        if (rotationAngleInDegrees == 90)
            rotationAngleInDegrees = -90;
        else if (rotationAngleInDegrees == -90)
            rotationAngleInDegrees = 90;
        Vector rotatedVector = relativeLocation.clone().rotateAroundY(Math.toRadians(rotationAngleInDegrees)).add(anchorPoint.toVector());
        return new Location(anchorPoint.getWorld(), rotatedVector.getX(), rotatedVector.getY(), rotatedVector.getZ());
    }

    public static Vector rotateVectorYAxis(double rotationAngleInDegrees, Location anchorPoint, Vector relativeLocation) {
        return rotateLocationYAxis(rotationAngleInDegrees, anchorPoint, relativeLocation).toVector();
    }

}
