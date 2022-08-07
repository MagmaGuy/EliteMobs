package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.config.dungeonpackager.InstancedDungeonConfig;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DungeonInstance extends MatchInstance {
    @Getter
    private static final HashMap<String, DungeonInstance> dungeonInstances = new HashMap<>();
    private List<DungeonObjective> dungeonObjectives = new ArrayList<>();

    public DungeonInstance(InstancedDungeonConfig instancedDungeonConfig, Location corner1, Location corner2, Location startLocation, Location exitLocation) {
        super(corner1, corner2, startLocation, exitLocation, instancedDungeonConfig.getMinPlayers(), instancedDungeonConfig.getMaxPlayers());
        //todo: add dungeon objectives from the configuration
    }

    public void checkCompletionStatus() {
        for (DungeonObjective dungeonObjective : dungeonObjectives)
            if (!dungeonObjective.isCompleted())
                return;
        //todo: add logic for completing the dungeon here
    }
}
