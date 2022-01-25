package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import lombok.Getter;

public class CustomKillObjective extends KillObjective {

    @Getter
    private final String customBossFilename;

    public CustomKillObjective(String customBossFilename, int targetKillAmount, int questLevel) {
        super(targetKillAmount, CustomBossesConfig.getCustomBosses().get(customBossFilename).getCleanName(questLevel * 10));
        this.customBossFilename = customBossFilename;
    }

    @Override
    public void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives) {
        if (!(event.getEliteEntity() instanceof CustomBossEntity)) return;
        CustomBossEntity customBossEntity = (CustomBossEntity) event.getEliteEntity();
        String filename = customBossEntity.getCustomBossesConfigFields().getFilename();
        if (customBossEntity.getPhaseBossEntity() != null)
            filename = customBossEntity.getPhaseBossEntity().getPhase1Config().getFilename();
        if (!filename.equals(customBossFilename)) return;
        progressObjective(questObjectives);
    }

}
