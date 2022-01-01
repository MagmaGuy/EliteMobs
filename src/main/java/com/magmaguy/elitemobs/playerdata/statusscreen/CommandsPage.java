package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandsPage {

    private CommandsPage() {
    }

    protected static TextComponent commandsPage() {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.getCommandsTextLines()[i] + "\n");

            if (!PlayerStatusMenuConfig.getCommandsHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getCommandsHoverLines()[i]);

            if (!PlayerStatusMenuConfig.getCommandsCommandLines()[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getCommandsCommandLines()[i]));

            textComponent.addExtra(line);
        }
        return textComponent;
    }
}
