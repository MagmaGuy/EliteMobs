package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CoverPage {
    protected static TextComponent coverPage(int statsPage, int gearPage, int teleportsPage, int commandsPage, int questsPage, int bossTrackingPage) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {
            TextComponent line = new TextComponent(
                    PlayerStatusMenuConfig.getIndexTextLines()[i]
                            .replace("$statsPage", statsPage + "")
                            .replace("$gearPage", gearPage + "")
                            .replace("$teleportsPage", teleportsPage + "")
                            .replace("$commandsPage", commandsPage + "")
                            .replace("$questsPage", questsPage + "")
                            .replace("$bossTrackingPage", bossTrackingPage + "")
                            + "\n");

            if (!PlayerStatusMenuConfig.getIndexHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getIndexHoverLines()[i]);

            if (PlayerStatusMenuConfig.getIndexCommandLines()[i].contains("$statsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.getIndexCommandLines()[i].replace("$statsPage", statsPage + "")));
            else if (PlayerStatusMenuConfig.getIndexCommandLines()[i].contains("$gearPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.getIndexCommandLines()[i].replace("$gearPage", gearPage + "")));
            else if (PlayerStatusMenuConfig.getIndexCommandLines()[i].contains("$teleportsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.getIndexCommandLines()[i].replace("$teleportsPage", teleportsPage + "")));
            else if (PlayerStatusMenuConfig.getIndexCommandLines()[i].contains("$commandsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.getIndexCommandLines()[i].replace("$commandsPage", commandsPage + "")));
            else if (PlayerStatusMenuConfig.getIndexCommandLines()[i].contains("$questsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.getIndexCommandLines()[i].replace("$questsPage", questsPage + "")));
            else if (PlayerStatusMenuConfig.getIndexCommandLines()[i].contains("$bossTrackingPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.getIndexCommandLines()[i].replace("$bossTrackingPage", bossTrackingPage + "")));

            else
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getIndexCommandLines()[i]));

            textComponent.addExtra(line);
        }

        return textComponent;

    }
}
