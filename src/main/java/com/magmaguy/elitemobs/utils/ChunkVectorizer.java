package com.magmaguy.elitemobs.utils;

import org.bukkit.Chunk;
import java.util.UUID;

/** Full chunk identity without retaining or loading a Chunk/World object. */
public final class ChunkVectorizer {
    private ChunkVectorizer() {}
    public record Key(UUID worldId, int x, int z) {}
    public static Key key(Chunk chunk) {
        return key(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
    }
    public static Key key(int x, int z, UUID worldId) { return new Key(worldId, x, z); }
}
