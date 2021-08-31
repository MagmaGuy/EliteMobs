package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class TransitiveBlock {
    @Getter
    private BlockData blockData;
    @Getter
    private Location location;
    @Getter
    @Setter
    private boolean isAir;

    public TransitiveBlock(BlockData blockData, Location location) {
        this.blockData = blockData;
        this.location = location;
    }
}