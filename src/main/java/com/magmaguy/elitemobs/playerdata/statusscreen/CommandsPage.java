package com.magmaguy.elitemobs.playerdata.statusscreen;

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

import java.util.HashSet;
import java.util.Set;

public class CommandsPage {

    private CommandsPage() {
    }

    protected static TextComponent commandsPage() {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {

            if (PlayerStatusMenuConfig.getCommandsTextLines()[i] == null) continue;

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.getCommandsTextLines()[i] + "\n");

            if (PlayerStatusMenuConfig.getCommandsHoverLines()[i] != null &&
                    !PlayerStatusMenuConfig.getCommandsHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getCommandsHoverLines()[i]);

            String command = normalizeCommand(PlayerStatusMenuConfig.getCommandsCommandLines()[i]);
            if (command != null && !command.isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

            textComponent.addExtra(line);
        }
        return textComponent;
    }

    protected static void commandsPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, PlayerStatusMenuConfig.getCommandsChestMenuName());
        inventory.setItem(PlayerStatusMenuConfig.getCommandsAGSlot(), PlayerStatusMenuConfig.getCommandsAGItem());
        inventory.setItem(PlayerStatusMenuConfig.getCommandsSpawnSlot(), PlayerStatusMenuConfig.getCommandsSpawnItem());
        inventory.setItem(PlayerStatusMenuConfig.getCommandsShareItemSlot(), PlayerStatusMenuConfig.getCommandsShareItemItem());
        inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
        if (requestingPlayer.openInventory(inventory) == null) return;
        StatusInventorySafety.protect(inventory);
        CommandsPageEvents.pageInventories.add(inventory);
    }

    static String normalizeCommand(String command) {
        if (command == null) return null;
        if (command.equalsIgnoreCase("/shareitem")) return "/em shareitem";
        return command;
    }

    public static class CommandsPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            if (event.getSlot() == 26) {
                player.closeInventory();
                CoverPage.coverPage(player);
                return;
            }
            if (event.getSlot() == PlayerStatusMenuConfig.getCommandsAGSlot()) {
                player.closeInventory();
                player.performCommand("ag");
                return;
            }
            if (event.getSlot() == PlayerStatusMenuConfig.getCommandsSpawnSlot()) {
                player.closeInventory();
                player.performCommand("em spawntp");
                return;
            }
            if (event.getSlot() == PlayerStatusMenuConfig.getCommandsShareItemSlot()) {
                player.closeInventory();
                player.performCommand("em shareitem");
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }
    }
}
