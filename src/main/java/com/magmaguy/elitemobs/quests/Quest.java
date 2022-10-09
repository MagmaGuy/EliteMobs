package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestLeaveEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Quest implements Serializable {

    @Getter
    //Player UUID as key
    //protected static final HashMap<UUID, Quest> playerQuests = new HashMap<>();
    //Temporarily stores a list of quests a player might be considering joining
    protected static final HashMap<UUID, List<Quest>> pendingPlayerQuests = new HashMap<>();
    @Getter
    protected final QuestObjectives questObjectives;
    @Getter
    private final UUID questID = UUID.randomUUID();
    @Getter
    protected String questName;
    @Getter
    @Setter
    //NPC the quest originates from
    protected String questGiver = "";
    @Setter
    //NPC the quest is turned in to
    protected String questTaker = "";
    @Getter
    @Setter
    private UUID playerUUID;
    @Getter
    @Setter
    private int questLevel;
    @Getter
    @Setter
    private boolean accepted = false;

    public Quest(Player player, QuestObjectives questObjectives, int questLevel) {
        this.playerUUID = player.getUniqueId();
        this.questObjectives = questObjectives;
        this.questLevel = questLevel;
        if (pendingPlayerQuests.get(playerUUID) == null) {
            List<Quest> quests = new ArrayList<>();
            quests.add(this);
            pendingPlayerQuests.put(playerUUID, quests);
        } else {
            List<Quest> quests = pendingPlayerQuests.get(playerUUID);
            quests.add(this);
            pendingPlayerQuests.put(playerUUID, quests);
        }
    }

    public static void stopPlayerQuest(Player player, String questID) {
        if (PlayerData.getQuests(player.getUniqueId()) == null ||
                PlayerData.getQuest(player.getUniqueId(), questID) == null) {
            player.sendMessage(QuestsConfig.getLeaveWhenNoActiveQuestsExist());
            return;
        }
        QuestLeaveEvent questLeaveEvent = new QuestLeaveEvent(player, PlayerData.getQuest(player.getUniqueId(), questID));
        new EventCaller(questLeaveEvent);
    }

    public static Quest completeQuest(UUID questUUID, Player player) {
        Quest quest = PlayerData.getQuest(player.getUniqueId(), questUUID);
        if (quest == null) return null;
        if (!quest.getQuestID().equals(questUUID)) return null;
        if (!quest.getQuestObjectives().isOver()) return null;
        QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(player, quest);
        new EventCaller(questCompleteEvent);
        return quest;
    }

    public String getQuestTaker() {
        return questTaker.isEmpty() ? questGiver : questTaker;
    }

}
