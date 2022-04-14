package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.commands.DungeonCommands;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;


public class TeleportsPage {

    protected static TextComponent[] teleportsPage() {
        TextComponent configTextComponent = new TextComponent();
        //Fills the non-dungeon lines
        int textLineCounter = 0;
        for (String string : PlayerStatusMenuConfig.getTeleportTextLines()) {
            if (string == null || string.equals("null"))
                continue;
            TextComponent line = new TextComponent(string + "\n");
            if (PlayerStatusMenuConfig.getTeleportHoverLines()[textLineCounter] != null && !PlayerStatusMenuConfig.getTeleportHoverLines()[textLineCounter].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getTeleportHoverLines()[textLineCounter]);

            if (PlayerStatusMenuConfig.getTeleportCommandLines()[textLineCounter] != null && !PlayerStatusMenuConfig.getTeleportCommandLines()[textLineCounter].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getTeleportCommandLines()[textLineCounter]));

            configTextComponent.addExtra(line);
            textLineCounter++;
        }

        //Fills the minidungeon components
        int counter = 0;
        ArrayList<TextComponent> textComponents = new ArrayList<>();

        for (Minidungeon minidungeon : Minidungeon.getMinidungeons().values()) {
            if (!minidungeon.isInstalled()) continue;

            TextComponent message = new TextComponent(PlayerStatusScreen.convertLightColorsToBlack(minidungeon.getDungeonPackagerConfigFields().getName() + "\n"));
            String hoverMessage = ChatColorConverter.convert(PlayerStatusMenuConfig.getOnTeleportHover() + "\n" +
                    minidungeon.getDungeonPackagerConfigFields().getPlayerInfo()
                            .replace("$bossCount", minidungeon.getRegionalBossCount() + "")
                            .replace("$lowestTier", minidungeon.getLowestTier() + "")
                            .replace("$highestTier", minidungeon.getHighestTier() + ""));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs dungeontp " + minidungeon.getDungeonPackagerConfigFields().getFilename()));
            textComponents.add(message);

            counter++;
        }

        if (counter == 0) {
            TextComponent[] textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
            return textComponent;
        } else {
            TextComponent[] textComponent = new TextComponent[(int) Math.floor(counter / 9D) + 1];
            int internalCounter = 0;
            textComponent[0] = configTextComponent;
            for (TextComponent text : textComponents) {
                int currentPage = (int) Math.floor(internalCounter / 9D);
                if (textComponent[currentPage] == null)
                    textComponent[currentPage] = new TextComponent();
                textComponent[currentPage].addExtra(text);
                internalCounter++;
            }
            return textComponent;
        }
    }

    protected static void teleportsPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 54, PlayerStatusMenuConfig.getTeleportChestMenuName());
        int counter = 0;
        TeleportsPageEvents.orderedDungeons.clear();
        for (Minidungeon minidungeon : Minidungeon.getMinidungeons().values()) {
            if (!minidungeon.isInstalled()) continue;
            TeleportsPageEvents.orderedDungeons.add(minidungeon);
            inventory.setItem(counter, ItemStackGenerator.generateItemStack(Material.PAPER, minidungeon.getDungeonPackagerConfigFields().getName()
                    , Collections.singletonList(minidungeon.getDungeonPackagerConfigFields().getPlayerInfo()
                            .replace("$bossCount", minidungeon.getRegionalBossCount() + "")
                            .replace("$lowestTier", minidungeon.getLowestTier() + "")
                            .replace("$highestTier", minidungeon.getHighestTier() + ""))));
            counter++;
        }
        inventory.setItem(53, PlayerStatusMenuConfig.getBackItem());
        requestingPlayer.openInventory(inventory);
        TeleportsPageEvents.pageInventories.put(requestingPlayer, inventory);
    }

    public static class TeleportsPageEvents implements Listener {
        private static final Map<Player, Inventory> pageInventories = new HashMap<>();
        private static List<Minidungeon> orderedDungeons = new ArrayList<>();

        @EventHandler (ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.containsKey(player)) return;
            event.setCancelled(true);
            if (event.getSlot() < 0) return;
            if (orderedDungeons.size() - 1 >= event.getSlot()) {
                player.closeInventory();
                DungeonCommands.teleport(player, orderedDungeons.get(event.getSlot()).getDungeonPackagerConfigFields().getFilename());
                return;
            }
            if (event.getSlot() == 53) {
                player.closeInventory();
                CoverPage.coverPage(player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getPlayer());
        }
    }

}
