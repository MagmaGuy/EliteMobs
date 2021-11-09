package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.config.menus.premade.CustomQuestMenuConfig;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class CustomQuestMenu {

    public static void generateCustomQuestMenu(List<CustomQuest> customQuest, Player player, NPCEntity npcEntity) {
        BookMaker.generateBook(player, generateQuestEntries(customQuest, player, npcEntity));
    }

    public static TextComponent[] generateQuestEntries(List<CustomQuest> customQuestList, Player player, NPCEntity npcEntity) {
        List<TextComponent[]> textComponents = new ArrayList<>();
        int counter = 0;
        for (CustomQuest customQuest : customQuestList) {
            TextComponent[] iteratedTextComponent = generateQuestEntry(customQuest, player, npcEntity);
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
    public static TextComponent[] generateQuestEntry(CustomQuest customQuest, Player player, NPCEntity npcEntity) {

        TextComponent header = SpigotMessage.simpleMessage(CustomQuestMenuConfig.headerTextLines.replace("$questName", customQuest.getCustomQuestsConfigFields().getQuestName()));
        TextComponent body = new TextComponent();
        body.addExtra(customQuest.getCustomQuestsConfigFields().getQuestLore());

        TextComponent fixedSummary = new TextComponent(CustomQuestMenuConfig.objectivesLine);

        TextComponent summary = new TextComponent();

        for (Objective objective : customQuest.getCustomQuestObjectives().getObjectives())
            if (objective instanceof KillObjective) {
                //todo: hover should show more boss details, command string should allow players to try to track the boss
                summary.addExtra(SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getKillQuestDefaultSummaryLine(objective), "", ""));
            }

        TextComponent fixedRewards = new TextComponent(CustomQuestMenuConfig.rewardsLine);

        TextComponent rewards = CustomQuestMenuConfig.getRewardsDefaultSummaryLine(
                customQuest.getCustomQuestObjectives().getCustomQuestReward(), customQuest.getQuestLevel(), player);

        TextComponent accept;
        if (!customQuest.isQuestIsAccepted())
            accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.acceptTextLines,
                    CustomQuestMenuConfig.acceptHoverLines,
                    CustomQuestMenuConfig.acceptCommandLines.replace("$questID", customQuest.getQuestID()));
        else if (!customQuest.getCustomQuestObjectives().isOver() ||
                npcEntity != null &&
                customQuest.getCustomQuestObjectives().isOver() &&
                        !customQuest.getCustomQuestsConfigFields().getTurnInNPC().isEmpty() &&
                        !customQuest.getCustomQuestsConfigFields().getTurnInNPC().equals(npcEntity.getNpCsConfigFields().getFilename()))
            accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.acceptedTextLines,
                    CustomQuestMenuConfig.acceptedHoverLines,
                    CustomQuestMenuConfig.acceptedCommandLines.replace("$questID", customQuest.getQuestID()));
        else
            accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.completedTextLines,
                    CustomQuestMenuConfig.completedHoverLines,
                    CustomQuestMenuConfig.completedCommandLines.replace("$questID", customQuest.getQuestID()));

        List<TextComponent> pagesList = new ArrayList<>();
        pagesList.add(header);

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
        if (CustomQuest.getPlayerQuests().get(player.getUniqueId()) == null) return new TextComponent[0];
        return generateQuestEntry(CustomQuest.getPlayerQuests().get(player.getUniqueId()), player, npcEntity);
    }

}
