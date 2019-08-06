package com.magmaguy.elitemobs.zoneworld;

import java.util.HashMap;

public class EliteChunk {

    public static HashMap<String, EliteChunk> eliteChunks = new HashMap<>();

    public static EliteChunk getEliteChunk(String hashCode) {
        return eliteChunks.get(hashCode);
    }

    public static EliteChunk getEliteChunk(int xCoord, int zCoord) {
        return eliteChunks.get(xCoord + ":" + zCoord);
    }

    private static void registerChunkLocation(EliteChunk eliteChunk) {
        eliteChunks.put(eliteChunk.getHashCode(), eliteChunk);
    }

    private static int gridSize = 50;

    public static int getGridSize() {
        return gridSize;
    }

    private int xCoord;
    private int zCoord;

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

    public String getHashCode() {
        return getxCoord() + ":" + getzCoord();
    }

}
