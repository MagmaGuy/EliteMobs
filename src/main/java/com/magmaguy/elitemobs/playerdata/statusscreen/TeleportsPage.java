package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;


public class TeleportsPage {

    protected static TextComponent[] teleportsPage() {
        TextComponent configTextComponent = new TextComponent();
        int textLineCounter = 0;
        for (String string : PlayerStatusMenuConfig.teleportTextLines) {
            if (string == null || string.equals("null"))
                continue;
            TextComponent line = new TextComponent(string + "\n");
            if (PlayerStatusMenuConfig.teleportHoverLines[textLineCounter] != null && !PlayerStatusMenuConfig.teleportHoverLines[textLineCounter].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.teleportHoverLines[textLineCounter]);

            if (PlayerStatusMenuConfig.teleportCommandLines[textLineCounter] != null && !PlayerStatusMenuConfig.teleportCommandLines[textLineCounter].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.teleportCommandLines[textLineCounter]));

            configTextComponent.addExtra(line);
            textLineCounter++;
        }

        int counter = 0;
        ArrayList<TextComponent> textComponents = new ArrayList<>();

        for (Minidungeon minidungeon : Minidungeon.minidungeons.values()) {
            if (!minidungeon.isInstalled) continue;

            TextComponent message = new TextComponent(PlayerStatusScreen.convertLightColorsToBlack(minidungeon.dungeonPackagerConfigFields.getName() + "\n"));
            String hoverMessage = ChatColorConverter.convert(PlayerStatusMenuConfig.onTeleportHover + "\n" +
                    minidungeon.dungeonPackagerConfigFields.getPlayerInfo()
                            .replace("$bossCount", minidungeon.regionalBossCount + "")
                            .replace("$lowestTier", minidungeon.lowestTier + "")
                            .replace("$highestTier", minidungeon.highestTier + ""));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs dungeontp " + minidungeon.dungeonPackagerConfigFields.getFileName()));
            textComponents.add(message);

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
