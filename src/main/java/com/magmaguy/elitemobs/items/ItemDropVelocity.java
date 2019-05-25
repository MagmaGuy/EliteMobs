package com.magmaguy.elitemobs.items;

import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Created by MagmaGuy on 07/10/2016.
 */
public class ItemDropVelocity {

    public static Vector ItemDropVelocity() {

        Random random = new Random();

        Double x = (random.nextDouble() - 0.5) / 3;
        Double y = 0.5;
        Double z = (random.nextDouble() - 0.5) / 3;

        Vector velocity = new org.bukkit.util.Vector(x, y, z);

        return velocity;

    }

}
