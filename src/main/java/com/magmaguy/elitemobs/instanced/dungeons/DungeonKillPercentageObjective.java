package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class DungeonKillPercentageObjective extends DungeonObjective {
    private static final List<DungeonKillPercentageObjective> dungeonKillPercentageObjectiveList = new ArrayList<>();
    private final int currentAmount = 0;
    private int targetAmount;
    private double percentage;

    public DungeonKillPercentageObjective(DungeonInstance dungeonInstance, String objectiveString) {
        super(dungeonInstance, objectiveString);
        String[] strings = objectiveString.split(":");
        for (String seperatedByColon : strings) {
            String[] separatedByEquals = seperatedByColon.split("=");
            if (separatedByEquals[0].equalsIgnoreCase("percentage")) {
                try {
                    this.percentage = Double.parseDouble(separatedByEquals[1]);
                } catch (Exception ex) {
                    Logger.warn("Value " + separatedByEquals[1] + " is not a valid integer amount!");
                }
            } else {
                Logger.warn("Invalid entry for objective string! " + objectiveString + " could not be parsed correctly.");
            }
        }
    }

    @Override
    protected void initializeObjective(DungeonInstance dungeonInstance) {
        super.initializeObjective(dungeonInstance);
        dungeonKillPercentageObjectiveList.add(this);
    }

}