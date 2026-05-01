package com.magmaguy.elitemobs.instanced.dungeons;

import lombok.Getter;

import java.util.Locale;

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
        if (objectiveString == null) return null;
        String lower = objectiveString.toLowerCase(Locale.ROOT);
        if (lower.contains("filename")) {
            return new DungeonKillTargetObjective(dungeonInstance, objectiveString);
        } else if (lower.contains("clearpercentage")) {
            return new DungeonKillPercentageObjective(dungeonInstance, objectiveString);
        } else if (lower.endsWith(".yml")) {
            // Legacy / typo'd entry: a bare boss filename without the "filename="
            // prefix. Auto-correct so existing servers with the old format keep
            // working instead of NPE-ing during instance teardown.
            return new DungeonKillTargetObjective(dungeonInstance, "filename=" + objectiveString);
        }
        return null;
    }

    protected void initializeObjective(DungeonInstance dungeonInstance) {
        this.dungeonInstance = dungeonInstance;
    }

    public void unregister() {
    }

}
