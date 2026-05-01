package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.magmacore.location.DungeonLocator;
import org.bukkit.Location;

/**
 * {@link DungeonLocator} implementation backed by {@link EMPackage}'s static world
 * registry. A location counts as being inside EliteMobs content whenever its world is
 * owned by any {@link WorldPackage} subclass — open-world dungeons, instanced
 * dungeons, dynamic dungeons, and hub/story worlds all qualify. Non-world package
 * types (items, events, meta, models) aren't keyed by world name and never match.
 *
 * <p>Intended to gate anti-exploit checks that should cover every EM-managed world,
 * not just the narrow "dungeon" subset, since content-pack scripts usually want to
 * block interaction across every EliteMobs content world. The query is an O(1)
 * HashMap lookup against the world name.
 */
public class EMDungeonLocator implements DungeonLocator {

    @Override
    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) return false;
        EMPackage pkg = EMPackage.getContent(location.getWorld().getName());
        return pkg instanceof WorldPackage;
    }
}
