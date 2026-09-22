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
    private static final long serialVersionUID = -2272809707714188608L;

    @Getter
    //Player UUID as key
    //protected static final HashMap<UUID, Quest> playerQuests = new HashMap<>();
    //Temporarily stores a list of quests a player might be considering joining
    protected static final HashMap<UUID, List<Quest>> pendingPlayerQuests = new HashMap<>();

    public static void shutdown() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) releasePlayerSession(player.getUniqueId());
        pendingPlayerQuests.clear();
    }

    public static void releasePlayerSession(UUID playerId) {
        com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective.cancelRefresh(playerId);
        pendingPlayerQuests.remove(playerId);
        if (!PlayerData.isInMemory(playerId)) return;
        for (Quest quest : PlayerData.getQuests(playerId))
            if (quest instanceof CustomQuest customQuest) customQuest.releaseTemporaryPermissions();
    }

    static void replaceOffers(Player player, List<? extends Quest> offers) {
        List<Quest> pending = new ArrayList<>();
        for (Quest quest : offers)
            if (!quest.isAccepted() && !quest.getQuestObjectives().isTurnedIn()) pending.add(quest);
        if (pending.isEmpty()) pendingPlayerQuests.remove(player.getUniqueId());
        else pendingPlayerQuests.put(player.getUniqueId(), pending);
    }

    public static boolean canAccept(Player player, Quest offer) {
        if (!PlayerData.isInMemory(player) || !player.isOnline() || offer == null
                || !player.getUniqueId().equals(offer.getPlayerUUID()) || offer.isAccepted()
                || offer.getQuestObjectives().isTurnedIn()) return false;
        if (offer instanceof CustomQuest custom && !custom.hasPermissionForQuest(player)) return false;
        for (Quest active : PlayerData.getQuests(player.getUniqueId())) {
            if (active.getQuestID().equals(offer.getQuestID())) return false;
            if (offer instanceof CustomQuest custom && active instanceof CustomQuest current
                    && custom.getConfigurationFilename().equals(current.getConfigurationFilename())) return false;
            if (offer instanceof DynamicQuest && active instanceof DynamicQuest
                    && offer.getQuestObjectives().getUuid().equals(active.getQuestObjectives().getUuid())) return false;
        }
        return true;
    }

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
        if (quest.getQuestObjectives().isTurnedIn()) return null;
        if (!quest.getQuestObjectives().isOver()) return null;
        QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(player, quest);
        new EventCaller(questCompleteEvent);
        return questCompleteEvent.isCancelled() ? null : quest;
    }

    public String getQuestTaker() {
        return questTaker.isEmpty() ? questGiver : questTaker;
    }

}
