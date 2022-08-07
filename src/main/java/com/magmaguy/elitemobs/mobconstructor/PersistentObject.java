package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.Location;
import org.bukkit.World;

public interface PersistentObject {

    /**
     * Sets the behavior for chunk loads
     */
    void chunkLoad();

    /**
     * Sets the behavior for chunk unloads
     */
    void chunkUnload();

    void worldLoad(World world);

    void worldUnload();

    Location getPersistentLocation();

    String getWorldName();

}
