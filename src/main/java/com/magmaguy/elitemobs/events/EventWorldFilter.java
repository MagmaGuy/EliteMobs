package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.EliteMobs;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EventWorldFilter {

    public static World randomizeValidWorld(WorldType worldType) {
        if (EliteMobs.validWorldList.isEmpty()) return null;
        List<World> validWorldList = new ArrayList<>();
        for (World world : EliteMobs.validWorldList)
            if (world.getWorldType().equals(worldType))
                validWorldList.add(world);

        if (validWorldList.isEmpty()) return null;

        return validWorldList.get(ThreadLocalRandom.current().nextInt(validWorldList.size()));

    }

}
