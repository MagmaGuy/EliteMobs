package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import lombok.Getter;
import org.bukkit.entity.EntityType;

public class DynamicKillObjective extends KillObjective {

    @Getter
    private final EntityType entityType;
    @Getter
    private final int minMobLevel;

    public DynamicKillObjective(int targetAmount, EntityType entityType, int questLevel) {
        super(targetAmount, EliteMobProperties.getPluginData(entityType).getName(questLevel * 10));
        this.minMobLevel = questLevel * 10;
        this.entityType = entityType;
    }

    @Override
    public void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives) {
        if (event.getEliteEntity().getLevel() < minMobLevel) return;
        if (event.getEliteEntity().getUnsyncedLivingEntity().getType() != this.entityType) return;
        progressObjective(questObjectives);
    }
}
