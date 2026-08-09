package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyInventoryMenu;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

public class CoverPage {
    protected static TextComponent coverPage(Player requestingPlayer, int statsPage, int gearPage, int teleportsPage,
                                             int commandsPage, int questsPage, int bossTrackingPage, int skillsPage) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < PlayerStatusMenuConfig.getIndexTextLines().length; i++) {
            String configuredText = PlayerStatusMenuConfig.getIndexTextLines()[i];
            String configuredCommand = PlayerStatusMenuConfig.getIndexCommandLines()[i];
            if (configuredText == null || configuredText.isBlank()) continue;
            if (configuredCommand == null) configuredCommand = "";
            if (unavailablePageTarget(configuredText + configuredCommand, statsPage, gearPage, teleportsPage,
                    commandsPage, questsPage, bossTrackingPage, skillsPage)) continue;
            if (configuredCommand.equalsIgnoreCase("/em party menu")
                    && (!PartyConfig.isEnabled() || !requestingPlayer.hasPermission("elitemobs.party"))) continue;
            TextComponent line = new TextComponent(
                    configuredText
                            .replace("$statsPage", statsPage + "")
                            .replace("$gearPage", gearPage + "")
                            .replace("$teleportsPage", teleportsPage + "")
                            .replace("$commandsPage", commandsPage + "")
                            .replace("$questsPage", questsPage + "")
                            .replace("$bossTrackingPage", bossTrackingPage + "")
                            .replace("$skillsPage", skillsPage + "")
                            + "\n");

            if (PlayerStatusMenuConfig.getIndexHoverLines()[i] == null) continue;

            if (!PlayerStatusMenuConfig.getIndexHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getIndexHoverLines()[i]);

            if (configuredCommand.contains("$statsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$statsPage", statsPage + "")));
            else if (configuredCommand.contains("$gearPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$gearPage", gearPage + "")));
            else if (configuredCommand.contains("$teleportsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$teleportsPage", teleportsPage + "")));
            else if (configuredCommand.contains("$commandsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$commandsPage", commandsPage + "")));
            else if (configuredCommand.contains("$questsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$questsPage", questsPage + "")));
            else if (configuredCommand.contains("$bossTrackingPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$bossTrackingPage", bossTrackingPage + "")));
            else if (configuredCommand.contains("$skillsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, configuredCommand.replace("$skillsPage", skillsPage + "")));

            else if (!configuredCommand.isBlank())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, configuredCommand));

            textComponent.addExtra(line);
        }

        return textComponent;

    }

    private static boolean unavailablePageTarget(String command, int statsPage, int gearPage, int teleportsPage,
                                                 int commandsPage, int questsPage, int bossTrackingPage, int skillsPage) {
        return command.contains("$statsPage") && statsPage <= 0 ||
                command.contains("$gearPage") && gearPage <= 0 ||
                command.contains("$teleportsPage") && teleportsPage <= 0 ||
                command.contains("$commandsPage") && commandsPage <= 0 ||
                command.contains("$questsPage") && questsPage <= 0 ||
                command.contains("$bossTrackingPage") && bossTrackingPage <= 0 ||
                command.contains("$skillsPage") && skillsPage <= 0;
    }

    public static void coverPage(Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, PlayerStatusMenuConfig.getIndexChestMenuName());
        inventory.setItem(PlayerStatusMenuConfig.getIndexHeaderSlot(),
                PlayerStatusOverview.decorateHeader(PlayerStatusMenuConfig.getIndexHeaderItem(), requestingPlayer));

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
        if (PartyConfig.isEnabled() && requestingPlayer.hasPermission("elitemobs.party"))
            inventory.setItem(PlayerStatusMenuConfig.getIndexPartySlot(), PlayerStatusMenuConfig.getIndexPartyItem());

        if (SkillsConfig.isSkillSystemEnabled())
            inventory.setItem(PlayerStatusMenuConfig.getIndexSkillsSlot(), PlayerStatusMenuConfig.getIndexSkillsItem());

        if (requestingPlayer.openInventory(inventory) == null) return;
        StatusInventorySafety.protect(inventory);
        CoverPageEvents.pageInventories.add(inventory);
    }

    public static class CoverPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;

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

            if (event.getSlot() == PlayerStatusMenuConfig.getIndexPartySlot()
                    && PartyConfig.isEnabled()
                    && player.hasPermission("elitemobs.party")) {
                player.closeInventory();
                PartyInventoryMenu.open(player);
                return;
            }

            // Skills page
            if (event.getSlot() == PlayerStatusMenuConfig.getIndexSkillsSlot() && SkillsConfig.isSkillSystemEnabled()) {
                player.closeInventory();
                SkillsPage.skillsPage(player, player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }
    }
}
