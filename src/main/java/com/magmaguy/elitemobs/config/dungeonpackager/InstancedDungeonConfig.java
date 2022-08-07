package com.magmaguy.elitemobs.config.dungeonpackager;

import lombok.Getter;

public class InstancedDungeonConfig extends DungeonPackagerConfigFields {
    @Getter
    private int minPlayers;
    @Getter
    private int maxPlayers;

    public InstancedDungeonConfig(String fileName, boolean isEnabled) {
        super(fileName, isEnabled);
    }

    @Override
    public void processAdditionalFields() {
        this.minPlayers = processInt("minPlayers", minPlayers, 1, false);
        this.maxPlayers = processInt("maxPlayers", maxPlayers, 5, false);

    }

}
