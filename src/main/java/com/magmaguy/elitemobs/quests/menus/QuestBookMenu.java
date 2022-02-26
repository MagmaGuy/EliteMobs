package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestBookMenu {
    private QuestBookMenu() {
    }

    //NPC entity is null when the entry is generated based on a command and not a npc interaction
    public static TextComponent[] generateQuestEntry(Quest quest, Player player, NPCEntity npcEntity) {
        QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);

        //Condense all page elements
        List<TextComponent> elements = new ArrayList<>();
        elements.add(questText.getHeader());
        if (quest instanceof CustomQuest && !quest.getQuestObjectives().isOver())
            elements.addAll(questText.getBody());
        elements.add(questText.getFixedSummary());
        elements.addAll(questText.getSummary());
        elements.add(questText.getFixedRewards());
        elements.addAll(questText.getRewards());
        elements.add(questText.getAccept());
        if (quest instanceof CustomQuest)
            elements.add(questText.getTrack());

        //Arrange them into pages, taking character count into account
        List<TextComponent> pagesList = new ArrayList<>();
        int pageIndex = 0;
        int characterCount = 0;
        int characterLimit = 185;
        for (TextComponent textComponent : elements) {
            characterCount += ChatColor.stripColor(textComponent.getText()).length();
            if (pagesList.isEmpty()) {
                textComponent.addExtra("\n");
                pagesList.add(textComponent);
            } else if (characterCount > characterLimit) {
                characterCount = 0;
                characterCount += ChatColor.stripColor(textComponent.getText()).length();
                pageIndex++;
                textComponent.addExtra("\n");
                pagesList.add(textComponent);
            } else {
                textComponent.addExtra("\n");
                pagesList.get(pageIndex).addExtra(textComponent);
            }
        }

        TextComponent[] pages = new TextComponent[pagesList.size()];

        int pageCounter = 0;
        for (TextComponent textComponent : pagesList) {
            pages[pageCounter] = textComponent;
            pageCounter++;
        }

        return pages;
    }
}
