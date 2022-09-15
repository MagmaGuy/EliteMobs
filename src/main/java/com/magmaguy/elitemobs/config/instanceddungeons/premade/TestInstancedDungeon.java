package com.magmaguy.elitemobs.config.instanceddungeons.premade;

import com.magmaguy.elitemobs.config.instanceddungeons.InstancedDungeonsConfigFields;
import org.bukkit.World;

import java.util.List;

public class TestInstancedDungeon extends InstancedDungeonsConfigFields {
    public TestInstancedDungeon() {
        super("test_dungeon",
                "Test Dungeon",
                World.Environment.NORMAL,
                "em_test_dungeon_world",
                List.of(""),
                "test_dungeon_world,0,0,0,0,0",
                "test_dungeon_world,0,0,0,0,0");
        setMinimumPlayerCount(1);
        setMaximumPlayerCount(5);
    }
}
