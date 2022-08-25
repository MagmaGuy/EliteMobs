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

            if (PlayerStatusMenuConfig.getCommandsHoverLines()[i] == null) continue;

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.getCommandsTextLines()[i] + "\n");

            if (!PlayerStatusMenuConfig.getCommandsHoverLines()[i].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getCommandsHoverLines()[i]);

            if (!PlayerStatusMenuConfig.getCommandsCommandLines()[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getCommandsCommandLines()[i]));

            textComponent.addExtra(line);
        }
        return textComponent;
    }

    protected static void commandsPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, PlayerStatusMenuConfig.getCommandsChestMenuName());
        inventory.setItem(PlayerStatusMenuConfig.getCommandsAGSlot(), PlayerStatusMenuConfig.getCommandsAGItem());
        inventory.setItem(PlayerStatusMenuConfig.getCommandsShareItemSlot(), PlayerStatusMenuConfig.getCommandsShareItemItem());
        inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
        CommandsPageEvents.pageInventories.add(inventory);
        requestingPlayer.openInventory(inventory);
    }

    public static class CommandsPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);
            if (event.getSlot() == 26) {
                player.closeInventory();
                CoverPage.coverPage(player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }
    }
}
