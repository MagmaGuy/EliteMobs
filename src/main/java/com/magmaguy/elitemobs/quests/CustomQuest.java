package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CustomQuest extends Quest {

    @Getter
    private final CustomQuestsConfigFields customQuestsConfigFields;

    public CustomQuest(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
        super(player, new QuestObjectives(new QuestReward(customQuestsConfigFields, player)), customQuestsConfigFields.getQuestLevel());
        this.customQuestsConfigFields = customQuestsConfigFields;
        super.questObjectives.setQuest(this);
        super.questName = customQuestsConfigFields.getQuestName();
        super.turnInNPC = customQuestsConfigFields.getTurnInNPC();
    }

    public static CustomQuest getQuest(String questFilename, Player player) {
        if (CustomQuestsConfig.getCustomQuests().get(questFilename) == null) return null;
        Quest quest = Quest.getPlayerQuests().get(player.getUniqueId());
        if (quest instanceof CustomQuest && ((CustomQuest) quest).getCustomQuestsConfigFields().getFilename().equals(questFilename))
            return (CustomQuest) quest;
        else
            return new CustomQuest(player, CustomQuestsConfig.getCustomQuests().get(questFilename));
    }

    public static Quest startQuest(String questID, Player player) {
        Quest quest = null;
        for (Quest iteratedQuest : pendingPlayerQuests.get(player.getUniqueId()))
            if (iteratedQuest.getQuestID().equals(UUID.fromString(questID))) {
                quest = iteratedQuest;
                break;
            }
        if (quest == null) player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cInvalid quest ID!"));
        QuestAcceptEvent questAcceptEvent = new QuestAcceptEvent(player, quest);
        new EventCaller(questAcceptEvent);
        if (questAcceptEvent.isCancelled()) return null;
        return quest;
    }

}
