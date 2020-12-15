package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class StatsPage {

    protected static TextComponent statsPage(Player targetPlayer) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {
            TextComponent line = new TextComponent(PlayerStatusMenuConfig.statsTextLines[i]
                    .replace("$money", EconomyHandler.checkCurrency(targetPlayer.getUniqueId()) + "")
                    .replace("$guildtier", PlayerStatusScreen.convertLightColorsToBlack(AdventurersGuildConfig.getShortenedRankName(GuildRank.getGuildPrestigeRank(targetPlayer), GuildRank.getActiveGuildRank(targetPlayer))))
                    .replace("$kills", PlayerData.getKills(targetPlayer.getUniqueId()) + "")
                    .replace("$highestkill", PlayerData.getHighestLevelKilled(targetPlayer.getUniqueId()) + "")
                    .replace("$deaths", PlayerData.getDeaths(targetPlayer.getUniqueId()) + "")
                    .replace("$quests", PlayerData.getQuestsCompleted(targetPlayer.getUniqueId()) + "")
                    .replace("$score", PlayerData.getScore(targetPlayer.getUniqueId()) + "") + "\n");

            if (!PlayerStatusMenuConfig.statsHoverLines[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.statsHoverLines[i]);

            if (!PlayerStatusMenuConfig.statsCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.statsCommandLines[i]));

            textComponent.addExtra(line);
        }

        return textComponent;

    }
}
