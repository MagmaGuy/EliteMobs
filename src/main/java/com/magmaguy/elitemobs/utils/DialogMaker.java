package com.magmaguy.elitemobs.utils;

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

    public static void sendDialogMessage(Player player, String Title, String message) {
        JsonObject dialogJson = new DialogManager
                .NoticeDialogBuilder()
                .title(processText(Title))
                .addBody(DialogManager.PlainMessageBody.of(processText(message)))
                .build();
        player.performCommand("minecraft:dialog show @s " + dialogJson.toString());
    }

    public static void sendQuestMessage(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        if (quests.isEmpty()) return;
        showQuestDialog(quests, 0, player, npcEntity);
    }

    private static void showQuestDialog(List<? extends Quest> quests, int questIndex, Player player, NPCEntity npcEntity) {
        DialogManager.MultiActionDialogBuilder builder = buildQuestDialogBuilder(quests, questIndex, player, npcEntity);
        JsonObject dialogJson = builder.build();
        player.performCommand("minecraft:dialog show @s " + dialogJson.toString());
    }

    private static DialogManager.MultiActionDialogBuilder buildQuestDialogBuilder(
            List<? extends Quest> quests, int questIndex, Player player, NPCEntity npcEntity) {

        Quest quest = quests.get(questIndex);
        QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);

        DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();

        // Set title and external title
        setDialogTitles(builder, questText, questIndex, quests.size());

        // Add all quest body sections
        addQuestBodySections(builder, quest, questText);

        // Add action buttons (accept/complete/track)
        addActionButtons(builder, quest, questText);

        // Add navigation buttons (previous/next)
        addNavigationButtons(builder, quests, questIndex, player, npcEntity);

        // Configure dialog properties
        configureDialogBehavior(builder);

        return builder;
    }

    private static void setDialogTitles(DialogManager.MultiActionDialogBuilder builder,
                                        QuestMenu.QuestText questText, int questIndex, int totalQuests) {
        // Main title
        String title = processText(questText.getHeader().getText());
        if (title != null && !title.isEmpty()) {
            builder.title(title);
        }

        // Quest counter as external title
        if (totalQuests > 1) {
            builder.externalTitle("Quest " + (questIndex + 1) + " of " + totalQuests);
        }
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
            if (component.getText() != null) {
                text.append(processText(component.getText())).append("\n");
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
        if (header != null && header.getText() != null) {
            builder.addBody(DialogManager.PlainMessageBody.of(processText(header.getText())).width(questDialogWidth));
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
                    if (i < items.size() && items.get(i).getText() != null) {
                        description = processText(items.get(i).getText());
                    }

                    // Create and add the ItemBody
                    DialogManager.ItemBody itemBody = DialogManager.ItemBody.of(itemId, itemStack.getAmount())
                            .showTooltip(true)
                            .showDecoration(true);

                    // Add custom item components (lore, enchantments, etc.)
                    JsonObject components = DialogManager.serializeItemComponents(itemStack);
                    if (components != null && !components.entrySet().isEmpty()) {
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
                    if (item.getText() != null) {
                        text.append("  ").append(processText(item.getText())).append("\n");
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
        if (quest instanceof CustomQuest) {
            builder.columns(2);
        }

        // Accept/Leave/Complete button
        addButtonFromComponent(builder, questText.getAccept());

        // Track button (for CustomQuests)
        if (quest instanceof CustomQuest) {
            addButtonFromComponent(builder, questText.getTrack());
        }
    }

    private static void addButtonFromComponent(DialogManager.MultiActionDialogBuilder builder,
                                               TextComponent component) {
        if (component == null || component.getText() == null || component.getText().isEmpty()) {
            return;
        }

        String text = processText(component.getText());
        if (text.contains("[Abandon]")) text = ChatColor.BOLD + "" + ChatColor.RED + "[Abandon]";

        String command = extractCommandFromComponent(component);
        if (command != null && !command.isEmpty()) {
            builder.addAction(DialogManager.ActionButton.of(
                    text,
                    new DialogManager.RunCommandAction(command)
            ).width((int) (questDialogWidth / 3d)));
        }
    }

    private static void addNavigationButtons(DialogManager.MultiActionDialogBuilder builder,
                                             List<? extends Quest> quests, int questIndex,
                                             Player player, NPCEntity npcEntity) {
        if (quests.size() <= 1) return;

        if (questIndex > 0 && questIndex < quests.size() - 1) builder.columns(2);

        // Previous quest button
        if (questIndex > 0) {
            final int prevIndex = questIndex - 1;
            builder.addAction(DialogManager.ActionButton.of(
                    "← Previous Quest",
                    new DialogManager.ShowDialogAction(
                            DialogManager.DialogReference.inline(
                                    buildQuestDialogBuilder(quests, prevIndex, player, npcEntity)
                            )
                    )
            ).width(questDialogWidth));
        }

        // Next quest button
        if (questIndex < quests.size() - 1) {
            final int nextIndex = questIndex + 1;
            builder.addAction(DialogManager.ActionButton.of(
                    "Next Quest →",
                    new DialogManager.ShowDialogAction(
                            DialogManager.DialogReference.inline(
                                    buildQuestDialogBuilder(quests, nextIndex, player, npcEntity)
                            )
                    )
            ).width(questDialogWidth));
        }
    }

    private static void configureDialogBehavior(DialogManager.MultiActionDialogBuilder builder) {
//        builder.canCloseWithEscape(true); this is default, honestly defaults are better here
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
}