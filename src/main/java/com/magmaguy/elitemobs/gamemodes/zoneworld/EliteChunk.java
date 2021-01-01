package com.magmaguy.elitemobs.gamemodes.zoneworld;

import com.magmaguy.elitemobs.utils.ChunkVectorizer;

import java.util.HashMap;
import java.util.Vector;

public class EliteChunk {

    public static HashMap<Vector, EliteChunk> eliteChunks = new HashMap<>();

    public static EliteChunk getEliteChunk(String hashCode) {
        return eliteChunks.get(hashCode);
    }

    public static EliteChunk getEliteChunk(int xCoord, int zCoord) {
        return eliteChunks.get(ChunkVectorizer.hash(xCoord, zCoord));
    }

    private static void registerChunkLocation(EliteChunk eliteChunk) {
        eliteChunks.put(ChunkVectorizer.hash(eliteChunk.getxCoord(), eliteChunk.getzCoord()), eliteChunk);
    }

    private static final int gridSize = 50;

    public static int getGridSize() {
        return gridSize;
    }

    private final int xCoord;
    private final int zCoord;

    public EliteChunk(int xCoord, int zCoord) {
        this.xCoord = xCoord;
        this.zCoord = zCoord;
        registerChunkLocation(this);
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getzCoord() {
        return zCoord;
    }

}
