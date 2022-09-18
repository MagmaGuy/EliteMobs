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
    public DungeonObjective(DungeonInstance dungeonInstance, String objectiveString) {
        this.dungeonInstance = dungeonInstance;
    }

    public static DungeonObjective registerObjective(DungeonInstance dungeonInstance, String objectiveString) {
        if (objectiveString.toLowerCase().contains("filename")) {
            return new DungeonKillTargetObjective(dungeonInstance, objectiveString);
        } else if (objectiveString.toLowerCase().contains("clearpercentage")) {
            return new DungeonKillPercentageObjective(dungeonInstance, objectiveString);
        }
        return null;
    }

    protected void initializeObjective(DungeonInstance dungeonInstance) {
        this.dungeonInstance = dungeonInstance;
    }

}
