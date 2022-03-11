package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.geyser.GeyserDetector;
import com.magmaguy.elitemobs.utils.BookMaker;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatusScreen implements Listener {

    public PlayerStatusScreen(Player requestingPlayer, Player targetPlayer) {
        ItemStack writtenBook = generateBook(requestingPlayer, targetPlayer);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        List<String> pages = bookMeta.getPages();
        String debugPage = "";
        debugPage += "Is in memory: " + PlayerData.isInMemory(targetPlayer);
        ArrayList<String> newPages = new ArrayList<>(pages);
        newPages.add(debugPage);
        BookMaker.generateBook(requestingPlayer, newPages);
    }

    public PlayerStatusScreen(Player player) {
        if (!PlayerData.getUseBookMenus(player.getUniqueId()) || GeyserDetector.bedrockPlayer(player)) {
            generateChestMenu(player, player);
        } else {
            generateBook(player, player);
        }
        if (!PlayerData.getDismissEMStatusScreenMessage(player.getUniqueId())){
            player.sendMessage(TranslationConfig.getDismissEMMessage());
        }
    }

    protected static void setHoverText(TextComponent textComponent, String text) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
    }

    public static String convertLightColorsToBlack(String string) {
        string = ChatColorConverter.convert(string);
        string = string.replace("§f", "§0").replace("§e", "§0");
        if (!string.startsWith("§"))
            string = "§0" + string;
        return string;
    }

    private void generateChestMenu(Player requestingPlayer, Player targetPlayer){
        CoverPage.coverPage(requestingPlayer);
    }

    private ItemStack generateBook(Player requestingPlayer, Player targetPlayer) {
        TextComponent[] pages = new TextComponent[50];
        int pageCounter = 1;
        pageCounter++;
        int statsPage = -1;
        int gearPage = -1;
        int teleportsPage = -1;
        int commandsPage = -1;
        int questsPage = -1;
        int bossTrackingPage = -1;
        if (PlayerStatusMenuConfig.isDoStatsPage()) {
            statsPage = pageCounter;
            pages[pageCounter] = StatsPage.statsPage(targetPlayer);
            pageCounter++;
        }
        if (PlayerStatusMenuConfig.isDoGearPage()) {
            gearPage = pageCounter;
            pages[pageCounter] = GearPage.gearPage(targetPlayer);
            pageCounter++;
        }
        if (PlayerStatusMenuConfig.isDoTeleportsPage()) {
            teleportsPage = pageCounter;
            for (TextComponent textComponent : TeleportsPage.teleportsPage()) {
                pages[pageCounter] = textComponent;
                pageCounter++;
            }
        }
        if (PlayerStatusMenuConfig.isDoCommandsPage()) {
            commandsPage = pageCounter;
            pages[pageCounter] = CommandsPage.commandsPage();
            pageCounter++;
        }
        if (PlayerStatusMenuConfig.isDoQuestTrackingPage()) {
            questsPage = pageCounter;
            for (TextComponent textComponent : QuestsPage.questsPage(targetPlayer)) {
                pages[pageCounter] = textComponent;
                pageCounter++;
            }
        }

        if (PlayerStatusMenuConfig.isDoBossTrackingPage()) {
            bossTrackingPage = pageCounter;
            for (TextComponent textComponent : BossTrackingPage.bossTrackingPage(targetPlayer)) {
                pages[pageCounter] = textComponent;
                pageCounter++;
            }
        }

        pages[0] = CoverPage.coverPage(statsPage, gearPage, teleportsPage, commandsPage, questsPage, bossTrackingPage);

        int counter = 0;
        for (TextComponent textComponent : pages) {
            if (textComponent != null)
                counter++;
        }

        TextComponent[] finalPages = new TextComponent[counter];
        int secondCounter = 0;
        for (TextComponent textComponent : pages) {
            if (textComponent != null) {
                finalPages[secondCounter] = textComponent;
                secondCounter++;
            }
        }

        return BookMaker.generateBook(requestingPlayer, finalPages);
    }


}
