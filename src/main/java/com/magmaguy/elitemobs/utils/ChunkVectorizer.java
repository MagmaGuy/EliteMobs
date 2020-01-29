package com.magmaguy.elitemobs.utils;

import org.bukkit.Chunk;

import java.util.Vector;

public class ChunkVectorizer {

    public static Vector vectorize(Chunk chunk) {
        Vector vector = new Vector(3);
        vector.addElement(chunk.getWorld());
        vector.addElement(chunk.getX());
        vector.addElement(chunk.getZ());
        return vector;
    }

    public static Vector vectorize(double x, double z) {
        Vector vector = new Vector(2);
        vector.addElement(x);
        vector.addElement(z);
        return vector;
    }

}
