package com.magmaguy.elitemobs.advancedcombat.constructs;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable packet-block layout for a temporary class construct. */
public record PacketConstructDefinition(
        String id,
        double viewRange,
        List<BlockVisual> blocks) {

    public PacketConstructDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Construct id is required");
        if (!Double.isFinite(viewRange) || viewRange <= 0D)
            throw new IllegalArgumentException("Construct view range must be positive");
        blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks"));
        if (blocks.isEmpty()) throw new IllegalArgumentException("Construct needs at least one visual block");
        Set<Offset> offsets = new HashSet<>();
        for (BlockVisual block : blocks) {
            Offset offset = new Offset(block.relativeX(), block.relativeY(), block.relativeZ());
            if (!offsets.add(offset)) throw new IllegalArgumentException(
                    "Duplicate construct block offset " + offset + " in " + id);
        }
    }

    public static BlockVisual block(int relativeX, int relativeY, int relativeZ, Material material) {
        Objects.requireNonNull(material, "material");
        if (!material.isBlock()) throw new IllegalArgumentException(material + " is not a block");
        return block(relativeX, relativeY, relativeZ, material.createBlockData());
    }

    public static BlockVisual block(int relativeX, int relativeY, int relativeZ, BlockData blockData) {
        Objects.requireNonNull(blockData, "blockData");
        return new BlockVisual(relativeX, relativeY, relativeZ, blockData.getAsString());
    }

    public record BlockVisual(
            int relativeX,
            int relativeY,
            int relativeZ,
            String serializedBlockData) {
        public BlockVisual {
            if (serializedBlockData == null || serializedBlockData.isBlank())
                throw new IllegalArgumentException("Construct block data is required");
        }
    }

    private record Offset(int x, int y, int z) {
    }
}
