package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.VersionChecker;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlayerStatusScreen implements Listener {

    public PlayerStatusScreen(Player requestingPlayer, Player targetPlayer) {
        generateBook(requestingPlayer, targetPlayer);
    }

    public PlayerStatusScreen(Player player) {
        if (!PlayerData.getUseBookMenus(player.getUniqueId()) || BedrockChecker.isBedrock(player) || DefaultConfig.isOnlyUseBedrockMenus()) {
            generateChestMenu(player, player);
        } else if (VersionChecker.serverVersionOlderThan(21,6)){
            generateBook(player, player);
        } else
            PlayerStatusScreenDialog.showPlayerStatusDialog(player);
        if (!PlayerData.getDismissEMStatusScreenMessage(player.getUniqueId()) && !DefaultConfig.isOnlyUseBedrockMenus()) {
            player.sendMessage(DefaultConfig.getDismissEMMessage());
        }
    }

    protected static void setHoverText(TextComponent textComponent, String text) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
    }

    public static String convertLightColorsToBlack(String string) {
        string = ChatColorConverter.convert(string);
        string = string.replace("§f", "§0").replace("§e", "§0")
                .replace("§a", "§0").replace("§b", "§0")
                .replace("§d", "§0").replace("§6", "§0")
                .replace("§9", "§0");
        if (!string.startsWith("§"))
            string = "§0" + string;
        return string;
    }

    private void generateChestMenu(Player requestingPlayer, Player targetPlayer) {
        CoverPage.coverPage(requestingPlayer);
    }

    private ItemStack generateBook(Player requestingPlayer, Player targetPlayer) {
        java.util.List<TextComponent> pages = new java.util.ArrayList<>();
        pages.add(new TextComponent()); // Cover is filled after physical page indices are known.
        int statsPage = PlayerStatusMenuConfig.isDoStatsPage() ? appendPages(pages, StatsPage.statsPage(targetPlayer)) : -1;
        int gearPage = PlayerStatusMenuConfig.isDoGearPage() ? appendPages(pages, GearPage.gearPage(targetPlayer)) : -1;
        int teleportsPage = PlayerStatusMenuConfig.isDoTeleportsPage() ? appendPages(pages, TeleportsPage.teleportsPage()) : -1;
        int commandsPage = PlayerStatusMenuConfig.isDoCommandsPage() ? appendPages(pages, CommandsPage.commandsPage()) : -1;
        int questsPage = PlayerStatusMenuConfig.isDoQuestTrackingPage() ? appendPages(pages, QuestsPage.questsPage(targetPlayer)) : -1;
        int bossTrackingPage = PlayerStatusMenuConfig.isDoBossTrackingPage() ? appendPages(pages, BossTrackingPage.bossTrackingPage(targetPlayer)) : -1;
        int skillsPage = SkillsConfig.isSkillSystemEnabled() ? appendPages(pages, SkillsPage.skillsPage(targetPlayer)) : -1;
        if (pages.size() > 100) {
            requestingPlayer.sendMessage("[EliteMobs] This status book exceeds Minecraft's 100-page limit. Use the inventory status menu.");
            if (requestingPlayer == targetPlayer) generateChestMenu(requestingPlayer, targetPlayer);
            return null;
        }
        pages.set(0, CoverPage.coverPage(requestingPlayer, statsPage, gearPage, teleportsPage,
                commandsPage, questsPage, bossTrackingPage, skillsPage));
        return BookMaker.generateBook(requestingPlayer, pages.toArray(TextComponent[]::new));
    }

    private static int appendPages(java.util.List<TextComponent> pages, TextComponent... section) {
        int firstPage = pages.size() + 1;
        for (TextComponent page : section) if (page != null) pages.add(page);
        return pages.size() < firstPage ? -1 : firstPage;
    }
}
