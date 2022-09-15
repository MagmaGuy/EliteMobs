package com.magmaguy.elitemobs.instanced.dungeons;

import lombok.Getter;

public class DungeonObjective {

    @Getter
    protected boolean completed = false;
    @Getter
    protected DungeonInstance dungeonInstance;
    /*
    Valid configuration string formats:
    Kill target: filename=boss.yml:amount=X
    Kill percentage: clearpercentage=X.Y
     */
    public DungeonObjective(String objectiveString) {

    }

    public static DungeonObjective registerObjective(String objectiveString) {
        if (objectiveString.toLowerCase().contains("filename")) {
            return new DungeonKillTargetObjective(objectiveString);
        } else if (objectiveString.toLowerCase().contains("clearpercentage")) {
            return new DungeonKillPercentageObjective(objectiveString);
        }
        return null;
    }

    protected void initializeObjective(DungeonInstance dungeonInstance) {
        this.dungeonInstance = dungeonInstance;
    }

}
