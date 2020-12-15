package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.quests.EliteQuest;
import com.magmaguy.elitemobs.quests.PlayerQuests;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class QuestsPage {
    protected static TextComponent[] questsPage(Player targetPlayer) {

        TextComponent configTextComponent = new TextComponent();

        for (int i = 0; i < 3; i++) {

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.questTrackerTextLines[i] + "\n");

            if (!PlayerStatusMenuConfig.questTrackerHoverLines[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.questTrackerHoverLines[i]);

            if (!PlayerStatusMenuConfig.questTrackerCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.questTrackerCommandLines[i]));

            configTextComponent.addExtra(line);
        }

        ArrayList<TextComponent> textComponents = new ArrayList<>();
        int counter = 0;

        if (PlayerQuests.getData(targetPlayer) != null && PlayerQuests.getData(targetPlayer).quests != null)
            for (EliteQuest eliteQuest : PlayerQuests.getData(targetPlayer).quests) {
                TextComponent quest = new TextComponent(ChatColor.BLACK + ChatColor.stripColor(eliteQuest.getQuestStatus()) + " \n");
                quest.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs quest cancel " + eliteQuest.getUuid()));
                PlayerStatusScreen.setHoverText(quest, PlayerStatusMenuConfig.onQuestTrackHover);
                textComponents.add(quest);
                counter++;
            }

        if (counter == 0) {
            TextComponent[] textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
            return textComponent;
        } else {
            TextComponent[] textComponent = new TextComponent[(int) Math.ceil(counter / 6d)];
            int internalCounter = 0;
            for (TextComponent text : textComponents) {
                if (internalCounter % 6 == 0)
                    textComponent[(int) Math.floor(internalCounter / 6d)] = configTextComponent;
                textComponent[(int) Math.floor(internalCounter / 6d)].addExtra(text);
                internalCounter++;
            }
            return textComponent;
        }
    }
}
