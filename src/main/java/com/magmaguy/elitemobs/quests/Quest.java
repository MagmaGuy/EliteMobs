package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestLeaveEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Quest {

    @Getter
    //Player UUID as key
    protected static final HashMap<UUID, Quest> playerQuests = new HashMap<>();
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
    protected String turnInNPC = "";
    @Getter
    @Setter
    private UUID playerUUID;
    @Getter
    @Setter
    private int questLevel;
    @Getter
    @Setter
    private boolean questIsAccepted = false;

    public Quest(Player player, QuestObjectives questObjectives, int questLevel) {
        this.playerUUID = player.getUniqueId();
        this.questObjectives = questObjectives;
        this.questLevel = questLevel;
        if (pendingPlayerQuests.get(playerUUID) == null){
            List<Quest> quests = new ArrayList<>();
            quests.add(this);
            pendingPlayerQuests.put(playerUUID, quests);
        } else
            pendingPlayerQuests.put(playerUUID, pendingPlayerQuests.get(getPlayerUUID())).add(this);
    }

    public static void stopPlayerQuest(Player player) {
        if (!playerQuests.containsKey(player.getUniqueId())) {
            player.sendMessage(QuestsConfig.leaveWhenNoActiveQuestsExist);
            return;
        }
        new QuestLeaveEvent(player, playerQuests.get(player.getUniqueId()));
    }

    public static Quest completeQuest(UUID questUUID, Player player) {
        Quest quest = playerQuests.get(player.getUniqueId());
        if (quest == null) return null;
        if (!quest.getQuestID().equals(questUUID)) return null;
        if (!quest.getQuestObjectives().isOver()) return null;
        QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(player, quest);
        new EventCaller(questCompleteEvent);
        return quest;
    }

}
