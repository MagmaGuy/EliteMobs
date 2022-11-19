package com.magmaguy.elitemobs.utils.shapes;

import org.bukkit.Location;

public class StaticRay extends Ray {
    public StaticRay(boolean ignoresSolidBlocks, double pointRadius, Location centerLocation, Location initialTargetLocation) {
        super(ignoresSolidBlocks, pointRadius, centerLocation, initialTargetLocation);
        locations = drawLine(centerLocation, initialTargetLocation);
    }
}
