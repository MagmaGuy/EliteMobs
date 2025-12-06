package com.magmaguy.elitemobs.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.dialog.DialogManager;
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
        return text.replace("§0", "§f").replace("&0", "&f");
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        UUID questID = UUID.fromString(commandData.getStringArgument("questID"));

        // Get the player's quests
        List<Quest> quests = PlayerData.getQuests(player.getUniqueId());

        if (quests == null || quests.isEmpty()) {
            player.sendMessage("You have no active quests.");
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
            player.sendMessage("Quest not found.");
            return;
        }

        // Build and show the quest dialog
        showQuestDialog(targetQuest, player);
    }

    private void showQuestDialog(Quest quest, Player player) {
        QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, null, player);

        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        // Set title
        String title = processText(questText.getHeader().getText());
        if (title != null && !title.isEmpty()) {
            builder.title(title);
        }

        // Add quest status to external title
        String statusSuffix = "";
        if (quest.isAccepted()) {
            if (quest.getQuestObjectives().isOver()) {
                statusSuffix = " §f| §2Turn in!";
            } else {
                statusSuffix = " §f| §aAccepted";
            }
        }
        builder.externalTitle(title + statusSuffix);

        // Add quest body (description)
        addBodySection(builder, questText.getBody());

        // Add objectives
        addBodySectionWithHeader(builder, questText.getFixedSummary(), questText.getSummary());

        // Add rewards
        addBodySectionWithRewards(builder, questText.getFixedRewards(), questText.getRewards(), quest);

        // Add action buttons
        addActionButtons(builder, quest, questText);

        // Add back button to return to status menu
        builder.addAction(DialogManager.ActionButton.of(
                "← Back to Status Menu",
                new DialogManager.RunCommandAction("/elitemobs")
        ).width(QUEST_DIALOG_WIDTH));

        DialogManager.sendDialog(player, builder);
    }

    private void addBodySection(DialogManager.MultiActionDialogBuilder builder,
                                List<net.md_5.bungee.api.chat.TextComponent> components) {
        if (components == null || components.isEmpty()) return;

        StringBuilder text = new StringBuilder();
        for (net.md_5.bungee.api.chat.TextComponent component : components) {
            if (component.getText() != null) {
                text.append(processText(component.getText())).append("\n");
            }
        }

        if (!text.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(QUEST_DIALOG_WIDTH));
        }
    }

    private void addBodySectionWithHeader(DialogManager.MultiActionDialogBuilder builder,
                                          net.md_5.bungee.api.chat.TextComponent header,
                                          List<net.md_5.bungee.api.chat.TextComponent> items) {
        if (header != null && header.getText() != null) {
            builder.addBody(DialogManager.PlainMessageBody.of(processText(header.getText())).width(QUEST_DIALOG_WIDTH));
        }

        if (items != null && !items.isEmpty()) {
            StringBuilder text = new StringBuilder();
            for (net.md_5.bungee.api.chat.TextComponent item : items) {
                if (item.getText() != null) {
                    text.append("  ").append(processText(item.getText())).append("\n");
                }
            }

            if (!text.isEmpty()) {
                builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(QUEST_DIALOG_WIDTH));
            }
        }
    }

    private void addBodySectionWithRewards(DialogManager.MultiActionDialogBuilder builder,
                                           net.md_5.bungee.api.chat.TextComponent header,
                                           List<net.md_5.bungee.api.chat.TextComponent> items,
                                           Quest quest) {
        if (header != null && header.getText() != null) {
            builder.addBody(DialogManager.PlainMessageBody.of(processText(header.getText())).width(QUEST_DIALOG_WIDTH));
        }

        // Try to show actual item rewards if available
        if (quest != null &&
                quest.getQuestObjectives().getQuestReward().previewRewards() != null &&
                !quest.getQuestObjectives().getQuestReward().previewRewards().isEmpty()) {

            List<org.bukkit.inventory.ItemStack> previewRewards =
                    quest.getQuestObjectives().getQuestReward().previewRewards();

            for (int i = 0; i < previewRewards.size(); i++) {
                org.bukkit.inventory.ItemStack itemStack = previewRewards.get(i);
                if (itemStack != null) {
                    String itemId = itemStack.getType().getKey().toString();

                    String description = "";
                    if (items != null && i < items.size() && items.get(i).getText() != null) {
                        description = processText(items.get(i).getText());
                    }

                    DialogManager.ItemBody itemBody = DialogManager.ItemBody.of(itemId, itemStack.getAmount())
                            .showTooltip(true)
                            .showDecoration(true);

                    JsonObject components = DialogManager.serializeItemComponents(itemStack);
                    if (components != null && !components.entrySet().isEmpty()) {
                        fixCustomModelDataFormat(components);
                        itemBody.components(components);
                    }

                    if (!description.isEmpty()) {
                        itemBody.description(description);
                    }

                    builder.addBody(itemBody);
                }
            }
        } else {
            // Fallback to text
            addBodySection(builder, items);
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
        if (component == null || component.getText() == null || component.getText().isEmpty()) {
            return;
        }

        String text = processText(component.getText());
        if (text.contains("[Abandon]")) {
            text = "§l§c[Abandon]";
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

    /**
     * Fixes the custom_model_data format for Minecraft 1.21.4+
     * Old format: "minecraft:custom_model_data": 36004
     * New format: "minecraft:custom_model_data": {"floats": [36004.0]}
     */
    private void fixCustomModelDataFormat(JsonObject components) {
        String key = "minecraft:custom_model_data";
        if (components.has(key)) {
            JsonElement element = components.get(key);
            if (element.isJsonPrimitive()) {
                double value = element.getAsDouble();
                JsonObject newFormat = new JsonObject();
                JsonArray floats = new JsonArray();
                floats.add(value);
                newFormat.add("floats", floats);
                components.add(key, newFormat);
            }
        }
    }
}