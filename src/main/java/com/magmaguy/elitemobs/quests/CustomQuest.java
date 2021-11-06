package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestLeaveEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.quests.objectives.CustomQuestObjectives;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class CustomQuest implements Serializable {

    @Getter
    private static HashMap<UUID, CustomQuest> playerQuests = new HashMap<>();
    @Getter
    @Setter
    private UUID playerUUID;
    @Getter
    private CustomQuestObjectives customQuestObjectives;
    @Getter
    @Setter
    private int questLevel;
    @Getter
    private CustomQuestsConfigFields customQuestsConfigFields;
    @Getter
    private String questID;
    @Getter
    @Setter
    private boolean questIsAccepted = false;

    public CustomQuest(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
        this.playerUUID = player.getUniqueId();
        this.customQuestsConfigFields = customQuestsConfigFields;
        this.customQuestObjectives = new CustomQuestObjectives(this, new CustomQuestReward(customQuestsConfigFields));
        this.questLevel = getCustomQuestsConfigFields().getQuestLevel();
        this.questID = customQuestsConfigFields.getFilename();
    }

    public static void stopPlayerQuest(Player player) {
        if (!playerQuests.containsKey(player.getUniqueId())) {
            player.sendMessage(QuestsConfig.leaveWhenNoActiveQuestsExist);
            return;
        }
        new QuestLeaveEvent(player, playerQuests.get(player.getUniqueId()));
    }

    public static CustomQuest getQuest(String questFilename, Player player) {
        if (CustomQuestsConfig.getCustomQuests().get(questFilename) == null) return null;
        return new CustomQuest(player, CustomQuestsConfig.getCustomQuests().get(questFilename));
    }

    public static CustomQuest startQuest(String questFilename, Player player){
        CustomQuest customQuest = getQuest(questFilename, player);
        QuestAcceptEvent questAcceptEvent = new QuestAcceptEvent(player, customQuest);
        new EventCaller(questAcceptEvent);
        if (questAcceptEvent.isCancelled()) return null;
        return customQuest;
    }


}
