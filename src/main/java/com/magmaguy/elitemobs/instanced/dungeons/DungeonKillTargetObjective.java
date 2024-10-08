package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class DungeonKillTargetObjective extends DungeonObjective {
    private static final List<DungeonKillTargetObjective> dungeonKillTargetObjectiveList = new ArrayList<>();
    @Getter
    private String bossFilename;
    @Getter
    private int targetAmount = 1;
    @Getter
    private int currentAmount = 0;


    public DungeonKillTargetObjective(DungeonInstance dungeonInstance, String objectiveString) {
        super(dungeonInstance, objectiveString);
        String[] strings = objectiveString.split(":");
        for (String seperatedByColon : strings) {
            String[] separatedByEquals = seperatedByColon.split("=");
            if (separatedByEquals[0].equalsIgnoreCase("filename")) {
                this.bossFilename = separatedByEquals[1];
            } else if (separatedByEquals[0].equalsIgnoreCase("amount")) {
                try {
                    this.targetAmount = Integer.parseInt(separatedByEquals[1]);
                } catch (Exception ex) {
                    Logger.warn("Value " + separatedByEquals[1] + " is not a valid integer amount!");
                }
            } else {
                Logger.warn("Invalid entry for objective string! " + objectiveString + " could not be parsed correctly.");
            }
        }
        initializeObjective(dungeonInstance);
    }

    @Override
    protected void initializeObjective(DungeonInstance dungeonInstance) {
        super.initializeObjective(dungeonInstance);
        dungeonKillTargetObjectiveList.add(this);
    }

    public void incrementKills() {
        currentAmount++;
        if (currentAmount >= targetAmount) {
            super.completed = true;
            dungeonInstance.checkCompletionStatus();
            dungeonKillTargetObjectiveList.remove(this);
        }
    }

    public static class DungeonKillTargetObjectiveListener implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            if (!(event.getEliteEntity() instanceof InstancedBossEntity instancedBossEntity)) return;
            List<DungeonKillTargetObjective> cloneList = new ArrayList<>(dungeonKillTargetObjectiveList);
            for (DungeonKillTargetObjective dungeonKillTargetObjective : cloneList) {
                if (instancedBossEntity.getCustomBossesConfigFields().getFilename().equals(dungeonKillTargetObjective.getBossFilename()) ||
                        instancedBossEntity.getPhaseBossEntity() != null &&
                                instancedBossEntity.getPhaseBossEntity().getPhase1Config().getFilename().equals(dungeonKillTargetObjective.getBossFilename()))
                    dungeonKillTargetObjective.incrementKills();
            }
        }
    }
}