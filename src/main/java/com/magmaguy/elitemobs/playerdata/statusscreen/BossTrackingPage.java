package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;

public class BossTrackingPage {
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
        for (Iterator<CustomBossEntity> customBossEntityIterator = CustomBossEntity.trackableCustomBosses.iterator(); customBossEntityIterator.hasNext(); ) {
            CustomBossEntity customBossEntity = customBossEntityIterator.next();
            if (customBossEntity == null ||
                    customBossEntity.advancedGetEntity() == null ||
                    customBossEntity.advancedGetEntity().isDead()) {
                customBossEntityIterator.remove();
                continue;
            }
            TextComponent message = new TextComponent(customBossEntity.bossBarMessage(player, customBossEntity.customBossConfigFields.getLocationMessage()) + "\n");
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlayerStatusMenuConfig.onBossTrackHover).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + player.getName() + " " + customBossEntity.uuid));
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
