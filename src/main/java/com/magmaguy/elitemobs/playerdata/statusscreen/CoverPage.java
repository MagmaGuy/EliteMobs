package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CoverPage {
    protected static TextComponent coverPage(int statsPage, int gearPage, int teleportsPage, int commandsPage, int questsPage, int bossTrackingPage) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {
            TextComponent line = new TextComponent(
                    PlayerStatusMenuConfig.indexTextLines[i]
                            .replace("$statsPage", statsPage + "")
                            .replace("$gearPage", gearPage + "")
                            .replace("$teleportsPage", teleportsPage + "")
                            .replace("$commandsPage", commandsPage + "")
                            .replace("$questsPage", questsPage + "")
                            .replace("$bossTrackingPage", bossTrackingPage + "")
                            + "\n");

            if (!PlayerStatusMenuConfig.indexHoverLines[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.indexHoverLines[i]);

            if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$statsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$statsPage", statsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$gearPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$gearPage", gearPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$teleportsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$teleportsPage", teleportsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$commandsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$commandsPage", commandsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$questsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$questsPage", questsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$bossTrackingPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$bossTrackingPage", bossTrackingPage + "")));

            else
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.indexCommandLines[i]));

            textComponent.addExtra(line);
        }

        return textComponent;

    }
}
