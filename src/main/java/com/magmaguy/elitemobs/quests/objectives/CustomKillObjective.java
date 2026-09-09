package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import lombok.Getter;

public class CustomKillObjective extends KillObjective {

    @Getter
    private final String customBossFilename;

    public CustomKillObjective(String customBossFilename, int targetKillAmount, int questLevel) {
        super(targetKillAmount, objectiveName(customBossFilename));
        this.customBossFilename = customBossFilename;
    }

    @Override
    public String getObjectiveName() {
        // The objective quantity is rendered separately from the boss's configured name.
        return objectiveName(customBossFilename);
    }

    private static String objectiveName(String filename) {
        var boss = CustomBossesConfig.getCustomBoss(filename);
        if (boss == null) return filename;
        String name = boss.getName();
        for (String token : java.util.List.of("$normalLevel", "$minibossLevel", "$bossLevel",
                "$reinforcementLevel", "$eventBossLevel", "$level")) name = name.replace(token, "");
        return com.magmaguy.magmacore.util.ChatColorConverter.convert(name).strip();
    }

    @Override
    public void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives) {
        if (!(event.getEliteEntity() instanceof CustomBossEntity customBossEntity)) return;
        String filename = customBossEntity.getCustomBossesConfigFields().getFilename();
        if (customBossEntity.getPhaseBossEntity() != null)
            filename = customBossEntity.getPhaseBossEntity().getPhase1Config().getFilename();
        if (!filename.equals(customBossFilename)) return;
        progressObjective(questObjectives);
    }

}
