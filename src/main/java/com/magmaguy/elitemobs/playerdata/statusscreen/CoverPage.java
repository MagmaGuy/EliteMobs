package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

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

    protected static void coverPage(Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, PlayerStatusMenuConfig.getIndexChestMenuName());
        inventory.setItem(PlayerStatusMenuConfig.getIndexHeaderSlot(), PlayerStatusMenuConfig.getIndexHeaderItem());

        if (PlayerStatusMenuConfig.isDoStatsPage())
            inventory.setItem(PlayerStatusMenuConfig.getIndexStatsSlot(), PlayerStatusMenuConfig.getIndexStatsItem());
        if (PlayerStatusMenuConfig.isDoGearPage())
            inventory.setItem(PlayerStatusMenuConfig.getIndexGearSlot(), PlayerStatusMenuConfig.getIndexGearItem());
        if (PlayerStatusMenuConfig.isDoTeleportsPage())
            inventory.setItem(PlayerStatusMenuConfig.getIndexTeleportsSlot(), PlayerStatusMenuConfig.getIndexTeleportsItem());
        if (PlayerStatusMenuConfig.isDoCommandsPage())
            inventory.setItem(PlayerStatusMenuConfig.getIndexCommandsSlot(), PlayerStatusMenuConfig.getIndexCommandsItem());
        if (PlayerStatusMenuConfig.isDoQuestTrackingPage())
            inventory.setItem(PlayerStatusMenuConfig.getIndexQuestTrackingSlot(), PlayerStatusMenuConfig.getIndexQuestTrackingItem());
        if (PlayerStatusMenuConfig.isDoBossTrackingPage())
            inventory.setItem(PlayerStatusMenuConfig.getIndexBossTrackingSlot(), PlayerStatusMenuConfig.getIndexBossTrackingItem());
        CoverPageEvents.pageInventories.put(requestingPlayer, inventory);
        requestingPlayer.openInventory(inventory);
    }

    public static class CoverPageEvents implements Listener {
        private static final Map<Player, Inventory> pageInventories = new HashMap<>();

        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.containsKey(player)) return;
            event.setCancelled(true);

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexHeaderSlot()) {
                player.closeInventory();
                AdventurersGuildCommand.adventurersGuildCommand(player);
                return;
            }

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexGearSlot() && PlayerStatusMenuConfig.isDoGearPage()) {
                player.closeInventory();
                GearPage.gearPage(player, player);
                return;
            }

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexStatsSlot() && PlayerStatusMenuConfig.isDoStatsPage()) {
                player.closeInventory();
                StatsPage.statsPage(player, player);
                return;
            }

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexCommandsSlot() && PlayerStatusMenuConfig.isDoCommandsPage()) {
                player.closeInventory();
                CommandsPage.commandsPage(player, player);
                return;
            }

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexTeleportsSlot() && PlayerStatusMenuConfig.isDoTeleportsPage()) {
                player.closeInventory();
                TeleportsPage.teleportsPage(player, player);
                return;
            }

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexQuestTrackingSlot() && PlayerStatusMenuConfig.isDoQuestTrackingPage()) {
                player.closeInventory();
                QuestsPage.questsPage(player, player);
                return;
            }

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexBossTrackingSlot() && PlayerStatusMenuConfig.isDoBossTrackingPage()) {
                player.closeInventory();
                BossTrackingPage.bossTrackingPage(player, player);
                return;
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getPlayer());
        }
    }
}
