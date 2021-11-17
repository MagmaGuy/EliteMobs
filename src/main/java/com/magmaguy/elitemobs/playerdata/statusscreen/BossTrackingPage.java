package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class BossTrackingPage {
    private BossTrackingPage() {
    }

    protected static TextComponent[] bossTrackingPage(Player player) {

        TextComponent configTextComponent = new TextComponent();

        for (int i = 0; i < 3; i++) {

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.bossTrackerTextLines[i] + "\n");

            if (!PlayerStatusMenuConfig.bossTrackerHoverLines[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.bossTrackerHoverLines[i]);

            if (!PlayerStatusMenuConfig.bossTrackerCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.bossTrackerCommandLines[i]));

            configTextComponent.addExtra(line);
        }

        ArrayList<TextComponent> textComponents = new ArrayList<>();
        int counter = 0;

        for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
            try {
                TextComponent message = new TextComponent(customBossEntity.getCustomBossBossBar().bossBarMessage(player, customBossEntity.getCustomBossesConfigFields().getLocationMessage()) + "\n");
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlayerStatusMenuConfig.onBossTrackHover).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + customBossEntity.getEliteUUID()));
                textComponents.add(message);

                counter++;
            } catch (Exception ex) {
                new WarningMessage("Failed to correctly get elements for boss tracking page!");
                ex.printStackTrace();
            }
        }

        if (counter == 0) {
            TextComponent[] textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
            return textComponent;
        } else {
            TextComponent[] textComponent = new TextComponent[(int) Math.floor(counter / 6D) + 1];
            int internalCounter = 0;
            textComponent[0] = configTextComponent;
            for (TextComponent text : textComponents) {
                int currentPage = (int) Math.floor(internalCounter / 6D);
                if (textComponent[currentPage] == null)
                    textComponent[currentPage] = new TextComponent();
                textComponent[currentPage].addExtra(text);
                internalCounter++;
            }
            return textComponent;
        }
    }
}
