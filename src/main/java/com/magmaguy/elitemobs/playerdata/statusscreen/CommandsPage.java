package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class CommandsPage {

    private CommandsPage() {
    }

    protected static TextComponent commandsPage() {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {

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
        requestingPlayer.openInventory(inventory);
        CommandsPageEvents.pageInventories.put(requestingPlayer, inventory);
    }

    public static class CommandsPageEvents implements Listener {
        private static final Map<Player, Inventory> pageInventories = new HashMap<>();

        @EventHandler (ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.containsKey(player)) return;
            event.setCancelled(true);
            if (event.getSlot() == 26) {
                player.closeInventory();
                pageInventories.remove(player);
                CoverPage.coverPage(player);
            }
        }
    }
}
