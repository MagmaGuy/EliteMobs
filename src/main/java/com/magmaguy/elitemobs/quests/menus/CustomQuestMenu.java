package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.config.menus.premade.CustomQuestMenuConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;


public class CustomQuestMenu {

    public CustomQuestMenu(CustomQuest customQuest, Player player) {
        BookMaker.generateBook(player, generateQuestPages(customQuest));
    }

    public static TextComponent[] generateQuestPages(CustomQuest customQuest) {
        TextComponent[] pages = new TextComponent[1];
        TextComponent header = SpigotMessage.simpleMessage(CustomQuestMenuConfig.headerTextLines.replace("$questName", customQuest.getCustomQuestsConfigFields().getQuestName()));
        TextComponent body = new TextComponent();
        body.setText(customQuest.getCustomQuestsConfigFields().getQuestLore());

        TextComponent summary = new TextComponent();

        for (Objective objective : customQuest.getCustomQuestObjectives().getObjectives())
            if (objective instanceof KillObjective) {
                //todo: hover should show more boss details, command string should allow players to try to track the boss
                summary.addExtra(SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.getKillQuestDefaultSummaryLine(objective), "", ""));
            }

        TextComponent accept;
        if (!customQuest.isQuestIsAccepted())
            accept = SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.acceptTextLines,
                    CustomQuestMenuConfig.acceptHoverLines,
                    CustomQuestMenuConfig.acceptCommandLines.replace("$questID", customQuest.getQuestID()));
        else
            accept =SpigotMessage.commandHoverMessage(CustomQuestMenuConfig.acceptedTextLines,
                    CustomQuestMenuConfig.acceptedHoverLines,
                    CustomQuestMenuConfig.acceptedCommandLines.replace("$questID", customQuest.getQuestID()));

        TextComponent page = new TextComponent();
        page.addExtra(header);
        page.addExtra("\n");
        page.addExtra(body);
        page.addExtra("\n");
        page.addExtra(summary);
        page.addExtra("\n");
        page.addExtra(accept);

        pages[0] = page;

        return pages;
    }

    public static TextComponent[] generateQuestPages(Player player) {
        if (CustomQuest.getPlayerQuests().get(player.getUniqueId()) == null) return new TextComponent[0];
        return generateQuestPages(CustomQuest.getPlayerQuests().get(player.getUniqueId()));
    }

}
