package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.dialog.DialogManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class QuestCheckCommand extends AdvancedCommand {
    private static final int QUEST_DIALOG_WIDTH = 300;

    public QuestCheckCommand() {
        super(List.of("quest"));
        addLiteral("check");
        addArgument("questID", new ListStringCommandArgument("<questID>"));
        setUsage("Internal command.");
        setSenderType(SenderType.PLAYER);
        setDescription("Internal command.");
    }

    private static String processText(String text) {
        if (text == null) return null;
        return ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&',
                        text.replace("§0", "§f")
                                .replace("&0", "&f")
                                .replace("\r", "")
                )
        );
    }

    private static String processSingleLineText(String text) {
        String processedText = processText(text);
        if (processedText == null) return null;

        return processedText
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        UUID questID = UUID.fromString(commandData.getStringArgument("questID"));

        // Get the player's quests
        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());

        if (quests == null || quests.isEmpty()) {
            player.sendMessage(CommandMessagesConfig.getNoActiveQuestsMessage());
            return;
        }

        // Find the specific quest by ID
        Quest targetQuest = null;
        for (Quest quest : quests) {
            if (quest.getQuestID().equals(questID)) {
                targetQuest = quest;
                break;
            }
        }

        if (targetQuest == null) {
            player.sendMessage(CommandMessagesConfig.getQuestNotFoundMessage());
            return;
        }

        // Build and show the quest dialog
        showQuestDialog(targetQuest, player);
    }

    private void showQuestDialog(Quest quest, Player player) {
        QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, null, player);

        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        // Set title
        String title = processSingleLineText(questText.getHeader().toPlainText());
        if (title != null && !title.isEmpty()) {
            builder.title(title);
        }

        // Add quest status to external title
        String statusSuffix = "";
        if (quest.isAccepted()) {
            if (quest.getQuestObjectives().isOver()) {
                statusSuffix = QuestsConfig.getQuestTurnInStatus();
            } else {
                statusSuffix = QuestsConfig.getQuestAcceptedStatus();
            }
        }
        builder.externalTitle(processSingleLineText(title + statusSuffix));

        // Add quest body (description)
        addBodySection(builder, questText.getBody());

        // Add objectives
        addBodySectionWithHeader(builder, questText.getFixedSummary(), questText.getSummary());

        // Add rewards
        com.magmaguy.elitemobs.utils.DialogMaker.addQuestRewardSection(builder, quest, questText);

        // Add action buttons
        addActionButtons(builder, quest, questText);

        // Add back button to return to status menu
        builder.addAction(DialogManager.ActionButton.of(
                processSingleLineText(QuestsConfig.getQuestBackToStatusMenu()),
                new DialogManager.RunCommandAction("/elitemobs")
        ).width(QUEST_DIALOG_WIDTH));

        DialogManager.sendDialog(player, builder);
    }

    private void addBodySection(DialogManager.MultiActionDialogBuilder builder,
                                List<net.md_5.bungee.api.chat.TextComponent> components) {
        if (components == null || components.isEmpty()) return;

        StringBuilder text = new StringBuilder();
        for (net.md_5.bungee.api.chat.TextComponent component : components) {
            if (component.toPlainText() != null) {
                text.append(processText(component.toPlainText())).append("\n");
            }
        }

        if (!text.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(QUEST_DIALOG_WIDTH));
        }
    }

    private void addBodySectionWithHeader(DialogManager.MultiActionDialogBuilder builder,
                                          net.md_5.bungee.api.chat.TextComponent header,
                                          List<net.md_5.bungee.api.chat.TextComponent> items) {
        if (header != null && header.toPlainText() != null) {
            builder.addBody(DialogManager.PlainMessageBody.of(processText(header.toPlainText())).width(QUEST_DIALOG_WIDTH));
        }

        if (items != null && !items.isEmpty()) {
            StringBuilder text = new StringBuilder();
            for (net.md_5.bungee.api.chat.TextComponent item : items) {
                if (item.toPlainText() != null) {
                    text.append("  ").append(processText(item.toPlainText())).append("\n");
                }
            }

            if (!text.isEmpty()) {
                builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(QUEST_DIALOG_WIDTH));
            }
        }
    }

    private void addActionButtons(DialogManager.MultiActionDialogBuilder builder,
                                  Quest quest, QuestMenu.QuestText questText) {
        builder.columns(1);

        // Accept/Leave/Complete button
        addButtonFromComponent(builder, questText.getAccept());

        // Track button if available
        addButtonFromComponent(builder, questText.getTrack());
    }

    private void addButtonFromComponent(DialogManager.MultiActionDialogBuilder builder,
                                        net.md_5.bungee.api.chat.TextComponent component) {
        if (component == null || component.toPlainText() == null || component.toPlainText().isEmpty()) {
            return;
        }

        String text = processSingleLineText(component.toPlainText());
        if (text.contains("[Abandon]")) {
            text = processSingleLineText(QuestsConfig.getQuestAbandonText());
        }

        String command = extractCommandFromComponent(component);
        if (command != null && !command.isEmpty()) {
            builder.addAction(DialogManager.ActionButton.of(
                    text,
                    new DialogManager.RunCommandAction(command)
            ).width(QUEST_DIALOG_WIDTH / 3));
        }
    }

    private String extractCommandFromComponent(net.md_5.bungee.api.chat.TextComponent component) {
        if (component == null) return null;

        try {
            if (component.getClickEvent() != null &&
                    component.getClickEvent().getAction() == net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND) {
                return component.getClickEvent().getValue();
            }
        } catch (Exception e) {
            // Ignore
        }

        return null;
    }

}
