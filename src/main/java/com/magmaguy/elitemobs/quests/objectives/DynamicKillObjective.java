package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;

public class DynamicKillObjective extends KillObjective {

    @Getter
    private final EntityType entityType;
    @Getter
    @Setter
    private int minMobLevel;

    public DynamicKillObjective(int targetAmount, EntityType entityType, int questLevel) {
        super(targetAmount, EliteMobProperties.getPluginData(entityType).getName(questLevel * 10));
        this.minMobLevel = questLevel * 10;
        this.entityType = entityType;
    }

    /**
     * Adapts this objective to a new dungeon level.
     * Updates the minimum mob level requirement and regenerates the objective name.
     *
     * @param newMobLevel The new mob level (from dynamic dungeon selection)
     */
    public void adaptToLevel(int newMobLevel) {
        this.minMobLevel = newMobLevel;
        // Update the objective name to reflect the new level
        this.objectiveName = EliteMobProperties.getPluginData(entityType).getName(newMobLevel);
    }

    @Override
    public void checkProgress(EliteMobDeathEvent event, QuestObjectives questObjectives) {
        if (event.getEliteEntity().getLevel() < minMobLevel) return;
        if (event.getEliteEntity().getUnsyncedLivingEntity().getType() != this.entityType) return;
        progressObjective(questObjectives);
    }
}
