package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.quests.dynamic.KillAmountQuest;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class QuestRandomizer {

    public static PlayerQuest generateQuest(int questTier) {

        return generateKillAmountQuest(questTier);

    }

    private static KillAmountQuest generateKillAmountQuest(int questTier) {
        PlayerQuest.QuestType questType = PlayerQuest.QuestType.DYNAMIC_KILL_AMOUNT_QUEST;
        QuestObjective questObjective = generateQuestObjective(generateEntityType(), questTier);
        QuestReward questReward = generateQuestReward(questObjective);
        return new KillAmountQuest(questType, questObjective, questReward);
    }

    private static EntityType generateEntityType() {

        ArrayList<EntityType> validEntities = new ArrayList<>();

        if (EliteMobProperties.isValidEliteMobType(EntityType.BLAZE))
            validEntities.add(EntityType.BLAZE);
        if (EliteMobProperties.isValidEliteMobType(EntityType.CAVE_SPIDER))
            validEntities.add(EntityType.CAVE_SPIDER);
        if (EliteMobProperties.isValidEliteMobType(EntityType.CREEPER))
            validEntities.add(EntityType.CREEPER);
        if (EliteMobProperties.isValidEliteMobType(EntityType.ENDERMAN))
            validEntities.add(EntityType.ENDERMAN);
        if (EliteMobProperties.isValidEliteMobType(EntityType.PIG_ZOMBIE))
            validEntities.add(EntityType.PIG_ZOMBIE);
        if (EliteMobProperties.isValidEliteMobType(EntityType.SKELETON))
            validEntities.add(EntityType.SKELETON);
        if (EliteMobProperties.isValidEliteMobType(EntityType.SPIDER))
            validEntities.add(EntityType.SPIDER);
        if (EliteMobProperties.isValidEliteMobType(EntityType.WITHER_SKELETON))
            validEntities.add(EntityType.WITHER_SKELETON);
        if (EliteMobProperties.isValidEliteMobType(EntityType.ZOMBIE))
            validEntities.add(EntityType.ZOMBIE);
        if (EliteMobProperties.isValidEliteMobType(EntityType.STRAY))
            validEntities.add(EntityType.STRAY);
        if (EliteMobProperties.isValidEliteMobType(EntityType.HUSK))
            validEntities.add(EntityType.HUSK);

        return validEntities.get(ThreadLocalRandom.current().nextInt(validEntities.size()));

    }

    private static QuestObjective generateQuestObjective(EntityType entityType, int questTier) {
        return new QuestObjective(
                ThreadLocalRandom.current().nextInt(4) * questTier + 4
                , questTier * 10,
                entityType,
                questTier);
    }

    private static QuestReward generateQuestReward(QuestObjective questObjective) {
        return new QuestReward(questObjective.getQuestTier(), questObjective.getQuestDifficulty());
    }

}
