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
    @Getter
    private String bossFilename;
    @Getter
    private int targetAmount = 1;
    @Getter
    private int currentAmount = 0;


    public DungeonKillTargetObjective(DungeonInstance dungeonInstance, String objectiveString) {
        this(dungeonInstance, DungeonObjective.parse(objectiveString));
    }

    DungeonKillTargetObjective(DungeonInstance dungeonInstance, TargetDefinition definition) {
        super(dungeonInstance, null);
        bossFilename = definition.filename();
        targetAmount = definition.amount();
    }

    public void incrementKills() {
        if (completed || dungeonInstance.isDefunct()) return;
        currentAmount++;
        if (currentAmount >= targetAmount) {
            super.completed = true;
            dungeonInstance.checkCompletionStatus();
            unregister();
        }
    }

    public static class DungeonKillTargetObjectiveListener implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            if (!(event.getEliteEntity() instanceof InstancedBossEntity instancedBossEntity)) return;
            if (instancedBossEntity.getDungeonInstance() == null) return;
            List<DungeonObjective> cloneList = instancedBossEntity.getDungeonInstance().objectiveSnapshot();
            for (DungeonObjective objective : cloneList) {
                if (!(objective instanceof DungeonKillTargetObjective dungeonKillTargetObjective)) continue;
                if (dungeonKillTargetObjective.isCompleted()) continue;
                if (dungeonKillTargetObjective.getDungeonInstance() != instancedBossEntity.getDungeonInstance()) continue;
                if (instancedBossEntity.getCustomBossesConfigFields().getFilename().equals(dungeonKillTargetObjective.getBossFilename()) ||
                        instancedBossEntity.getPhaseBossEntity() != null &&
                                instancedBossEntity.getPhaseBossEntity().getPhase1Config().getFilename().equals(dungeonKillTargetObjective.getBossFilename()))
                    dungeonKillTargetObjective.incrementKills();
            }
        }
    }
}
