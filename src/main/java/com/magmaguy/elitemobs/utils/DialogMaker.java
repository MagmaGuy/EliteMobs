package com.magmaguy.elitemobs.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.magmacore.dialog.DialogManager;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class DialogMaker {
    private static final int questDialogWidth = 300;

    private DialogMaker() {
    }

    /**
     * Processes text by replacing black color codes with white color codes
     * to ensure readability in dialogs
     */
    private static String processText(String text) {
        if (text == null) return null;
        // Replace both section symbol and ampersand variants
        return text.replace("§0", "§f").replace("&0", "&f");
    }

    public static void sendQuestMessage(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        if (quests.isEmpty()) return;
        showQuestListDialog(quests, player, npcEntity);
    }

    private static void showQuestListDialog(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        DialogManager.DialogListDialogBuilder listBuilder = new DialogManager.DialogListDialogBuilder();

        // Add title
        listBuilder.title("Available Quests");

        // Add each quest as a dialog reference
        for (int i = 0; i < quests.size(); i++) {
            DialogManager.MultiActionDialogBuilder questDialog =
                    buildQuestDialogBuilder(quests, i, player, npcEntity);

            listBuilder.addDialog(DialogManager.DialogReference.inline(questDialog));
        }

        // Optional: configure columns and button width
        listBuilder.columns(1);
        listBuilder.buttonWidth(questDialogWidth);

        DialogManager.sendDialog(player, listBuilder);
    }

    private static DialogManager.MultiActionDialogBuilder buildQuestDialogBuilder(
            List<? extends Quest> quests, int questIndex, Player player, NPCEntity npcEntity) {

        Quest quest = quests.get(questIndex);
        QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);

        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        setDialogTitles(quest, builder, questText, questIndex, quests.size());
        addQuestBodySections(builder, quest, questText);
        addActionButtons(builder, quest, questText);

        // Always add back button to return to quest list
        String backCommand = "/elitemobs npc questList " + npcEntity.getUuid();
        builder.addAction(DialogManager.ActionButton.of(
                "← Back to Quest List",
                new DialogManager.RunCommandAction(backCommand)
        ).width(questDialogWidth));

        return builder;
    }

    private static void setDialogTitles(Quest quest, DialogManager.MultiActionDialogBuilder builder,
                                        QuestMenu.QuestText questText, int questIndex, int totalQuests) {
        // Main title
        String title = processText(questText.getHeader().toPlainText());
        if (title != null && !title.isEmpty()) {
            builder.title(title);
        }

        builder.externalTitle(title + (quest.isAccepted() ? quest.getQuestObjectives().isOver() ? ChatColor.WHITE + " | " + ChatColor.DARK_GREEN + "Turn in!" : ChatColor.WHITE + " | " + ChatColor.GREEN + "Accepted" : ""));
    }

    private static void addQuestBodySections(DialogManager.MultiActionDialogBuilder builder,
                                             Quest quest, QuestMenu.QuestText questText) {
        // Add lore/description
        addBodySection(builder, questText.getBody());

        // Add objectives (text only, no items)
        addBodySectionWithHeader(builder, questText.getFixedSummary(), questText.getSummary(), quest, false);

        // Add rewards (with items if available)
        addBodySectionWithHeader(builder, questText.getFixedRewards(), questText.getRewards(), quest, true);
    }

    private static void addBodySection(DialogManager.MultiActionDialogBuilder builder,
                                       List<TextComponent> components) {
        if (components == null || components.isEmpty()) return;

        StringBuilder text = new StringBuilder();
        for (TextComponent component : components) {
            if (component.toPlainText() != null) {
                text.append(processText(component.toPlainText())).append("\n");
            }
        }

        if (!text.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()));
        }
    }

    private static void addBodySectionWithHeader(DialogManager.MultiActionDialogBuilder builder,
                                                 TextComponent header, List<TextComponent> items,
                                                 Quest quest, boolean showItemsIfAvailable) {
        // Add header as text if present
        if (header != null && header.toPlainText() != null) {
            builder.addBody(DialogManager.PlainMessageBody.of(processText(header.toPlainText())).width(questDialogWidth));
        }

        // Check if we should and can display items
        boolean hasPreviewItems = showItemsIfAvailable
                && quest != null
                && quest.getQuestObjectives().getQuestReward().previewRewards() != null
                && !quest.getQuestObjectives().getQuestReward().previewRewards().isEmpty();

        if (hasPreviewItems) {
            // Display rewards as actual item icons
            List<ItemStack> previewRewards = quest.getQuestObjectives().getQuestReward().previewRewards();

            for (int i = 0; i < previewRewards.size() && items != null && i < items.size(); i++) {
                ItemStack itemStack = previewRewards.get(i);
                if (itemStack != null) {
                    String itemId = itemStack.getType().getKey().toString();

                    // Get the description from the corresponding TextComponent
                    String description = "";
                    if (i < items.size() && items.get(i).toPlainText() != null) {
                        description = processText(items.get(i).toPlainText());
                    }

                    // Create and add the ItemBody
                    DialogManager.ItemBody itemBody = DialogManager.ItemBody.of(itemId, itemStack.getAmount())
                            .showTooltip(true)
                            .showDecoration(true);

                    // Add custom item components (lore, enchantments, etc.)
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
            // Fall back to text-based display
            if (items != null && !items.isEmpty()) {
                StringBuilder text = new StringBuilder();
                for (TextComponent item : items) {
                    if (item.toPlainText() != null) {
                        text.append("  ").append(processText(item.toPlainText())).append("\n");
                    }
                }

                if (!text.isEmpty()) {
                    builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(questDialogWidth));
                }
            }
        }
    }

    private static void addActionButtons(DialogManager.MultiActionDialogBuilder builder,
                                         Quest quest, QuestMenu.QuestText questText) {
        if (quest instanceof CustomQuest && quest.isAccepted()) {
            builder.columns(2);
        } else builder.columns(1);

        // Accept/Leave/Complete button
        addButtonFromComponent(builder, questText.getAccept());

        // Track button (for CustomQuests)
        if (quest instanceof CustomQuest) {
            addButtonFromComponent(builder, questText.getTrack());
        }
    }

    private static void addButtonFromComponent(DialogManager.MultiActionDialogBuilder builder,
                                               TextComponent component) {
        if (component == null || component.toPlainText() == null || component.toPlainText().isEmpty()) {
            return;
        }

        String text = processText(component.toPlainText());
        if (text.contains("[Abandon]")) text = ChatColor.BOLD + "" + ChatColor.RED + "[Abandon]";

        String command = extractCommandFromComponent(component);
        if (command != null && !command.isEmpty()) {
            builder.addAction(DialogManager.ActionButton.of(
                    text,
                    new DialogManager.RunCommandAction(command)
            ).width((int) (questDialogWidth / 3d)));
        }
    }

    private static String extractCommandFromComponent(TextComponent component) {
        if (component == null) return null;

        // Try to get the click event command from the Spigot TextComponent
        try {
            if (component.getClickEvent() != null &&
                    component.getClickEvent().getAction() == net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND) {
                return component.getClickEvent().getValue();
            }
        } catch (Exception e) {
            // If extraction fails, return null
        }

        return null;
    }

    /**
     * Fixes the custom_model_data format for Minecraft 1.21.4+
     * Old format: "minecraft:custom_model_data": 36004
     * New format: "minecraft:custom_model_data": {"floats": [36004.0]}
     */
    private static void fixCustomModelDataFormat(JsonObject components) {
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