package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class QuestsPage {
    protected static TextComponent[] questsPage(Player targetPlayer) {
        return QuestMenu.generateQuestEntry(targetPlayer, null);
    }
}
