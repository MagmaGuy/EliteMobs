package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Handles quest lockouts - tracking completions and preventing re-acceptance for locked out players.
 */
public class QuestLockoutHandler implements Listener {

    /**
     * Checks if a player is locked out from a quest and notifies them.
     *
     * @param player        The player trying to accept the quest
     * @param questFilename The quest filename
     * @param questName     The display name of the quest
     * @return true if locked out, false otherwise
     */
    public static boolean isLockedOut(Player player, String questFilename, String questName) {
        QuestLockout questLockout = PlayerData.getQuestLockout(player.getUniqueId());
        if (questLockout == null) return false;

        if (questLockout.isLockedOut(questFilename)) {
            notifyLockout(player, questName, questLockout, questFilename);
            return true;
        }

        return false;
    }

    /**
     * Notifies a player that they are locked out from accepting a quest.
     */
    private static void notifyLockout(Player player, String questName, QuestLockout questLockout, String questFilename) {
        String remainingTime = questLockout.getFormattedRemainingTime(questFilename);

        // Show action bar subtitle
        String subtitle = QuestsConfig.getQuestLockoutSubtitle();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(subtitle));

        // Send title with empty title and just subtitle
        player.sendTitle("", subtitle, 10, 70, 20);

        // Send chat message
        String chatMessage = QuestsConfig.getQuestLockoutChatMessage()
                        .replace("$questName", questName)
                        .replace("$remainingTime", remainingTime);
        player.sendMessage(chatMessage);
    }

    /**
     * Adds a lockout when a player completes a quest (if configured).
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestReward(QuestRewardEvent event) {
        if (!(event.getQuest() instanceof CustomQuest customQuest)) return;

        CustomQuestsConfigFields config = customQuest.getCustomQuestsConfigFields();
        if (config == null) return;

        int lockoutMinutes = config.getQuestLockoutMinutes();
        if (lockoutMinutes <= 0) return; // No lockout configured

        Player player = event.getPlayer();
        String questFilename = customQuest.getConfigurationFilename();

        // Get or create quest lockout tracker for this player
        QuestLockout questLockout = PlayerData.getQuestLockout(player.getUniqueId());
        if (questLockout == null) {
            questLockout = new QuestLockout();
        }

        // Add the lockout
        questLockout.addLockout(questFilename, lockoutMinutes);
        PlayerData.updateQuestLockout(player.getUniqueId(), questLockout);
    }
}
