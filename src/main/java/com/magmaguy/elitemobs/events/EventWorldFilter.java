package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.EliteMobs;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.util.ArrayList;

public class EventWorldFilter {

    public static ArrayList<World> getValidWorlds(WorldType worldType) {
        if (EliteMobs.validWorldList.isEmpty()) return null;
        ArrayList<World> validWorldList = new ArrayList<>();
        for (World world : EliteMobs.validWorldList) {
            if (world.getWorldType().equals(worldType))
                validWorldList.add(world);
        }

        if (validWorldList.isEmpty()) return null;

        return validWorldList;
    }

}
