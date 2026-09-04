package com.magmaguy.elitemobs.experimentalcombat.minions;

/** World-independent block coordinate used by the gate planner and ownership ledger. */
public record GateBlockPosition(int x, int y, int z) {

    GateBlockPosition offset(int xOffset, int yOffset, int zOffset) {
        return new GateBlockPosition(x + xOffset, y + yOffset, z + zOffset);
    }
}
