package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.World;

public interface SimplePersistentEntityInterface {

    /**
     * Sets the behavior for chunk loads
     */
    void chunkLoad();

    /**
     * Sets the behavior for chunk unloads
     */
    void chunkUnload();

    void worldLoad();

    void worldUnload();

}
