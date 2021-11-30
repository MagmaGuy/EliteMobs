package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.config.menus.premade.CustomQuestMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.DynamicQuestMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.objectives.DynamicKillObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class QuestMenu {

    private QuestMenu() {
    }

    public static void generateCustomQuestMenu(List<CustomQuest> customQuestList, Player player, NPCEntity npcEntity) {
        BookMaker.generateBook(player, generateQuestEntries(customQuestList, player, npcEntity));
    }

    public static void generateDynamicQuestMenu(List<DynamicQuest> dynamicQuests, Player player) {
        BookMaker.generateBook(player, generateQuestEntries(dynamicQuests, player, null));
    }

    public static TextComponent[] generateQuestEntries(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
        List<TextComponent[]> textComponents = new ArrayList<>();
        int counter = 0;
        for (Quest quest : quests) {
            TextComponent[] iteratedTextComponent = generateQuestEntry(quest, player, npcEntity);
            counter += iteratedTextComponent.length;
            textComponents.add(iteratedTextComponent);
        }

        TextComponent[] allQuests = new TextComponent[counter];

        int counter2 = 0;
        for (TextComponent[] textComponentsArray : textComponents)
            for (TextComponent textComponent : textComponentsArray) {
                allQuests[counter2] = textComponent;
                counter2++;
            }

        return allQuests;
    }

    //NPC entity is null when the entry is generated based on a command and not a npc interaction
    public static TextComponent[] generateQuestEntry(Quest quest, Player player, NPCEntity npcEntity) {

        TextComponent header = generateHeader(quest);
        TextComponent body = generateBody(quest);
        TextComponent fixedSummary = generateFixedSummary(quest);
        TextComponent summary = generateSummary(quest);
        TextComponent fixedRewards = generateFixedRewards(quest);
        TextComponent rewards = generateRewards(quest, player);
        TextComponent accept = generateAccept(quest, npcEntity);

        List<TextComponent> pagesList = new ArrayList<>();
        pagesList.add(header);

        if (quest instanceof CustomQuest)
            compilePages(pagesList, body);
        compilePages(pagesList, fixedSummary);
        compilePages(pagesList, summary);
        compilePages(pagesList, fixedRewards);
        compilePages(pagesList, rewards);
        compilePages(pagesList, accept);

        TextComponent[] pages = new TextComponent[pagesList.size()];

        int pageCounter = 0;
        for (TextComponent textComponent : pagesList) {
            pages[pageCounter] = textComponent;
            pageCounter++;
        }

        return pages;
    }

    private static TextComponent generateHeader(Quest quest) {
        if (quest instanceof CustomQuest)
            return SpigotMessage.simpleMessage(CustomQuestMenuConfig.headerTextLines.replace("$questName", quest.getQuestName()));
        else
            return SpigotMessage.simpleMessage(DynamicQuestMenuConfig.getHeaderTextLines().replace("$questName", quest.getQuestName()));
    }

    private static TextComponent generateBody(Quest quest) {
        TextComponent body = new TextComponent();
        if (quest instanceof CustomQuest)
            body.addExtra(((CustomQuest) quest).getCustomQuestsConfigFields().getQuestLore());
        else if (quest instanceof DynamicQuest)
            body.addExtra(DynamicQuestMenuConfig.getDefaultLoreTextLines()
                    .replace("$amount", quest.getQuestObjectives().getObjectives().get(0).getTargetAmount() + "")
                    .replace("$name", EliteMobProperties.getPluginData(((DynamicKillObjective) quest.getQuestObjectives().getObjectives().get(0)).getEntityType()).getName(quest.getQuestLevel() * 10)));
        return body;
    }

    private static TextComponent generateFixedSummary(Quest quest) {
        TextComponent fixedSummary = new TextComponent();
        if (quest instanceof CustomQuest)
            fixedSummary.setText(CustomQuestMenuConfig.objectivesLine);
        else if (quest instanceof DynamicQuest)
            fixedSummary.setText(DynamicQuestMenuConfig.getObjectivesLine());
        return fixedSummary;
    }

    private static TextComponent generateSummary(Quest quest) {
        TextComponent summary = new TextComponent();
        if (quest instanceof CustomQuest) {
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof KillObjective) {
                    //todo: hover should show more boss details, command string should allow players to try to track the boss
                    summary.addExtra(SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getKillQuestDefaultSummaryLine(objective), "", ""));
                }
        } else if (quest instanceof DynamicQuest) {
            for (Objective objective : quest.getQuestObjectives().getObjectives())
                if (objective instanceof KillObjective) {
                    //todo: hover should show more boss details, command string should allow players to try to track the boss
                    summary.addExtra(SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getKillQuestDefaultSummaryLine(objective), "", ""));
                }
        }
        return summary;
    }

    private static TextComponent generateFixedRewards(Quest quest) {
        TextComponent questRewards = new TextComponent();
        if (quest instanceof CustomQuest)
            questRewards.setText(CustomQuestMenuConfig.rewardsLine);
        else if (quest instanceof DynamicQuest)
            questRewards.setText(DynamicQuestMenuConfig.getRewardsLine());
        return questRewards;
    }

    private static TextComponent generateRewards(Quest quest, Player player) {
        TextComponent questRewards = null;
        if (quest instanceof CustomQuest)
            questRewards = CustomQuestMenuConfig.getRewardsDefaultSummaryLine(quest.getQuestObjectives().getQuestReward(), quest.getQuestLevel(), player);
        else if (quest instanceof DynamicQuest)
            questRewards = DynamicQuestMenuConfig.getRewardsDefaultSummaryLine(quest.getQuestObjectives().getQuestReward(), quest.getQuestLevel(), player);
        return questRewards;
    }

    private static TextComponent generateAccept(Quest quest, NPCEntity npcEntity) {
        TextComponent accept = new TextComponent();
        if (quest instanceof CustomQuest) {
            if (!quest.isAccepted()) {
                accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.acceptTextLines,
                        CustomQuestMenuConfig.acceptHoverLines,
                        CustomQuestMenuConfig.acceptCommandLines.replace("$questID", quest.getQuestID().toString()));
            }else if (!quest.getQuestObjectives().isOver())
                accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.acceptedTextLines,
                        CustomQuestMenuConfig.acceptedHoverLines,
                        CustomQuestMenuConfig.acceptedCommandLines.replace("$questID", quest.getQuestID().toString()));
            else if (quest.getQuestObjectives().isOver() && quest.getQuestObjectives().isTurnedIn() &&
                    (npcEntity == null && quest.getTurnInNPC().isEmpty() ||
                            npcEntity != null && quest.getTurnInNPC().isEmpty() ||
                            npcEntity != null && !quest.getTurnInNPC().isEmpty() && quest.getTurnInNPC().equals(npcEntity.getNpCsConfigFields().getFilename())))
                accept = SpigotMessage.hoverMessage(DynamicQuestMenuConfig.getTurnedInTextLines(),
                        DynamicQuestMenuConfig.getTurnedInHoverLines());
            else if (quest.getQuestObjectives().isOver() &&
                    (npcEntity == null && quest.getTurnInNPC().isEmpty() ||
                            npcEntity != null && quest.getTurnInNPC().isEmpty() ||
                            npcEntity != null && !quest.getTurnInNPC().isEmpty() && quest.getTurnInNPC().equals(npcEntity.getNpCsConfigFields().getFilename())))
                accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.completedTextLines,
                        CustomQuestMenuConfig.completedHoverLines,
                        CustomQuestMenuConfig.completedCommandLines.replace("$questID", quest.getQuestID().toString()));
            else {
                //todo: add a way to tell players where to go before this ever runs
                accept = SpigotMessage.simpleMessage("");
            }
        } else if (quest instanceof DynamicQuest) {
            if (!quest.isAccepted())
                accept = SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getAcceptTextLines(),
                        DynamicQuestMenuConfig.getAcceptedHoverLines(),
                        DynamicQuestMenuConfig.getAcceptCommandLines().replace("$questID", quest.getQuestID().toString()));
            else if (!quest.getQuestObjectives().isOver())
                accept = SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getAcceptedTextLines(),
                        DynamicQuestMenuConfig.getAcceptedHoverLines(),
                        DynamicQuestMenuConfig.getAcceptedCommandLines().replace("$questID", quest.getQuestID().toString()));
            else if (quest.getQuestObjectives().isOver() && quest.getQuestObjectives().isTurnedIn())
                accept = SpigotMessage.hoverMessage(DynamicQuestMenuConfig.getTurnedInTextLines(),
                        DynamicQuestMenuConfig.getTurnedInHoverLines());
            else if (quest.getQuestObjectives().isOver() &&
                    (npcEntity == null && quest.getTurnInNPC().isEmpty() ||
                            npcEntity != null && quest.getTurnInNPC().isEmpty() ||
                            npcEntity != null && !quest.getTurnInNPC().isEmpty() && quest.getTurnInNPC().equals(npcEntity.getNpCsConfigFields().getFilename())))
                accept = SpigotMessage.commandHoverMessage(DynamicQuestMenuConfig.getCompletedTextLines(),
                        DynamicQuestMenuConfig.getCompletedHoverLines(),
                        DynamicQuestMenuConfig.getCompletedCommandLines().replace("$questID", quest.getQuestID().toString()));
            else {
                //todo: add a way to tell players where to go before this ever runs
                accept = SpigotMessage.simpleMessage("");
            }
        }
        return accept;
    }

    private static List<TextComponent> compilePages(List<TextComponent> pages, TextComponent newComponent) {
        if (!isOverCharacterCount(pages.get(pages.size() - 1), newComponent)) {
            pages.get(pages.size() - 1).addExtra(newComponent);
            pages.get(pages.size() - 1).addExtra("\n");
        } else {
            pages.add(newComponent);
            pages.get(pages.size() - 1).addExtra("\n");
        }
        return pages;
    }

    private static boolean isOverCharacterCount(TextComponent page, TextComponent newComponent) {
        return page.getText().length() + newComponent.getText().length() > 220;
    }

    public static TextComponent[] generateQuestEntry(Player player, NPCEntity npcEntity) {
        if (PlayerData.getQuest(player.getUniqueId()) == null) return new TextComponent[0];
        return generateQuestEntry(PlayerData.getQuest(player.getUniqueId()), player, npcEntity);
    }

}
