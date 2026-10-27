package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.commands.DungeonCommands;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.dungeons.CombatContent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.util.ItemStackGenerator;
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
        for (int textLineCounter = 0; textLineCounter < PlayerStatusMenuConfig.getTeleportTextLines().length; textLineCounter++) {
            String string = PlayerStatusMenuConfig.getTeleportTextLines()[textLineCounter];
            if (string == null || string.equals("null"))
                continue;
            TextComponent line = new TextComponent(string + "\n");
            if (PlayerStatusMenuConfig.getTeleportHoverLines()[textLineCounter] != null && !PlayerStatusMenuConfig.getTeleportHoverLines()[textLineCounter].isEmpty())
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getTeleportHoverLines()[textLineCounter]);

            if (PlayerStatusMenuConfig.getTeleportCommandLines()[textLineCounter] != null && !PlayerStatusMenuConfig.getTeleportCommandLines()[textLineCounter].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.getTeleportCommandLines()[textLineCounter]));

            configTextComponent.addExtra(line);
        }

        //Fills the dungeon components, with a heading line before each group when there are several
        int counter = 0;
        ArrayList<TextComponent> textComponents = new ArrayList<>();
        List<TeleportGroups.Group<EMPackage>> groups = TeleportGroups.current();

        for (TeleportGroups.Group<EMPackage> group : groups) {
            if (groups.size() > 1) {
                textComponents.add(new TextComponent(PlayerStatusScreen.convertLightColorsToBlack(group.name() + "\n")));
                counter++;
            }
            for (EMPackage emPackage : group.dungeons()) {
                TextComponent message = new TextComponent(PlayerStatusScreen.convertLightColorsToBlack(emPackage.getContentPackagesConfigFields().getName() + "\n"));
                String playerInfo = emPackage.getContentPackagesConfigFields().getPlayerInfo();
                String hoverMessage = PlayerStatusMenuConfig.getOnTeleportHover() +
                        (playerInfo != null ? "\n" + playerInfo
                                .replace("$bossCount", emPackage.getCustomBossEntityList().size() + "")
                                .replace("$lowestTier", ((CombatContent) emPackage).getLowestLevel() + "")
                                .replace("$highestTier", ((CombatContent) emPackage).getHighestLevel() + "") : "");
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs dungeontp " + emPackage.getContentPackagesConfigFields().getFilename()));
                textComponents.add(message);
                counter++;
            }
        }

        if (counter == 0) {
            TextComponent[] textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
            return textComponent;
        } else {
            double elementsPerPage = 5D;
            TextComponent[] textComponent = new TextComponent[(int) Math.ceil((counter + 2) / elementsPerPage)];
            int internalCounter = 2;
            textComponent[0] = configTextComponent;
            for (TextComponent text : textComponents) {
                int currentPage = (int) Math.floor(internalCounter / elementsPerPage);
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
        Map<Integer, String> actions = new HashMap<>();
        addAction(inventory, actions, PlayerStatusMenuConfig.getTeleportSpawnSlot(),
                PlayerStatusMenuConfig.getTeleportSpawnItem(), "em spawntp");
        addAction(inventory, actions, PlayerStatusMenuConfig.getTeleportGuildSlot(),
                PlayerStatusMenuConfig.getTeleportGuildItem(), "ag");

        Set<Integer> reservedSlots = new HashSet<>(actions.keySet());
        reservedSlots.add(53);
        Map<Integer, EMPackage> dungeons = new HashMap<>();
        List<TeleportGroups.Group<EMPackage>> groups = TeleportGroups.current();
        List<Integer> slots = chestSlots(groups, reservedSlots);
        int slotIndex = 0;
        boolean headers = slots.size() > TeleportGroups.dungeonCount(groups);
        for (TeleportGroups.Group<EMPackage> group : groups) {
            if (headers) {
                if (slotIndex >= slots.size()) break;
                inventory.setItem(slots.get(slotIndex++), ItemStackGenerator.generateItemStack(Material.BOOK, group.name()));
            }
            for (EMPackage emPackage : group.dungeons()) {
                if (slotIndex >= slots.size()) break;
                int slot = slots.get(slotIndex++);
                String chestPlayerInfo = emPackage.getContentPackagesConfigFields().getPlayerInfo();
                String loreText = chestPlayerInfo != null ? chestPlayerInfo
                                .replace("$bossCount", emPackage.getCustomBossEntityList().size() + "")
                                .replace("$lowestTier", ((CombatContent) emPackage).getLowestLevel() + "")
                                .replace("$highestTier", ((CombatContent) emPackage).getHighestLevel() + "") : "";
                inventory.setItem(slot, ItemStackGenerator.generateItemStack(Material.PAPER, emPackage.getContentPackagesConfigFields().getName()
                        , Collections.singletonList(loreText)));
                dungeons.put(slot, emPackage);
            }
        }
        inventory.setItem(53, PlayerStatusMenuConfig.getBackItem());
        if (requestingPlayer.openInventory(inventory) == null) return;
        StatusInventorySafety.protect(inventory);
        TeleportsPageEvents.pageInventories.put(inventory, new TeleportMenuState(dungeons, actions));
    }

    /**
     * Picks the chest slots for the grouped teleport list. Each group gets a heading item and starts on a
     * fresh row when that all fits; otherwise headings flow inline, and when even that would push dungeons
     * off the page the headings are dropped so the menu lists as many dungeons as before.
     */
    private static List<Integer> chestSlots(List<TeleportGroups.Group<EMPackage>> groups, Set<Integer> reservedSlots) {
        if (groups.size() > 1) {
            List<Integer> rowAligned = placeChestSlotsWithHeadings(groups, reservedSlots, true);
            if (rowAligned != null) return rowAligned;
            List<Integer> inline = placeChestSlotsWithHeadings(groups, reservedSlots, false);
            if (inline != null) return inline;
        }
        List<Integer> plain = new ArrayList<>();
        for (int slot = 0; slot < 53; slot++)
            if (!reservedSlots.contains(slot)) plain.add(slot);
        return plain.subList(0, Math.min(plain.size(), TeleportGroups.dungeonCount(groups)));
    }

    private static List<Integer> placeChestSlotsWithHeadings(List<TeleportGroups.Group<EMPackage>> groups,
                                                             Set<Integer> reservedSlots, boolean rowAligned) {
        List<Integer> slots = new ArrayList<>();
        int slot = 0;
        for (TeleportGroups.Group<EMPackage> group : groups) {
            if (rowAligned && slot % 9 != 0) slot += 9 - slot % 9;
            for (int i = 0; i < group.dungeons().size() + 1; i++) {
                while (slot < 53 && reservedSlots.contains(slot)) slot++;
                if (slot >= 53) return null;
                slots.add(slot++);
            }
        }
        return slots;
    }

    private static void addAction(Inventory inventory, Map<Integer, String> actions, int slot,
                                  org.bukkit.inventory.ItemStack item, String command) {
        if (slot < 0 || slot >= 53 || item == null) return;
        inventory.setItem(slot, item);
        actions.put(slot, command);
    }

    public static void showTeleportInventory(Player player) {
        teleportsPage(player, player);
    }

    public static class TeleportsPageEvents implements Listener {
        private static final Map<Inventory, TeleportMenuState> pageInventories = new HashMap<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            TeleportMenuState menuState = pageInventories.get(event.getInventory());
            if (menuState == null) return;
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            if (event.getSlot() < 0) return;
            EMPackage emPackage = menuState.dungeons().get(event.getSlot());
            if (emPackage != null) {
                player.closeInventory();
                DungeonCommands.teleport(player,
                        emPackage.getContentPackagesConfigFields().getFilename(),
                        DungeonCommands.TeleportMenuSource.INVENTORY);
                return;
            }
            String command = menuState.actions().get(event.getSlot());
            if (command != null) {
                player.closeInventory();
                player.performCommand(command);
                return;
            }
            if (event.getSlot() == 53) {
                player.closeInventory();
                CoverPage.coverPage(player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }

    }

    private record TeleportMenuState(Map<Integer, EMPackage> dungeons, Map<Integer, String> actions) {
    }

}
