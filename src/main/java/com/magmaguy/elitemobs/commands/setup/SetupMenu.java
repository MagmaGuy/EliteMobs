package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class SetupMenu {

    public static HashMap<Inventory, SetupMenu> setupMenus = new HashMap<>();

    Inventory inventory;
    Player player;
    ArrayList<Integer> validSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23,
            24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
    HashMap<Integer, Minidungeon> minidungeonHashMap = new HashMap<>();

    public SetupMenu(Player player) {
        this.inventory = Bukkit.createInventory(player, 54, "Setup menu");
        this.player = player;
        //reserves the first slot
        permissionsStatus();
        //reserve adventurer's guild
        adventurersGuildWorldStatus();
        //iterate through dungeons
        dungeonStatuses();
        setupMenus.put(inventory, this);
        player.openInventory(inventory);
    }

    private void permissionsStatus() {

        String name = "Currently using";
        String state;
        if (DefaultConfig.usePermissions)
            state = "Permissions!";
        else
            state = "Permissionless mode!";

        Material material;
        if (!DefaultConfig.setupDone) {
            material = Material.RED_STAINED_GLASS_PANE;
            state = ChatColor.RED + state;
        } else {
            material = Material.GREEN_STAINED_GLASS_PANE;
            state = ChatColor.GREEN + state;
        }

        inventory.setItem(validSlots.get(0), ItemStackGenerator.generateItemStack(material, name, Collections.singletonList(state)));
    }

    private void adventurersGuildWorldStatus() {
        String state = "Adventurer's Guild world is";
        String lore;
        Material material;
        if (AdventurersGuildConfig.guildWorldLocation != null) {
            material = Material.RED_STAINED_GLASS_PANE;
            lore = ChatColor.RED + "Not setup!";
        } else {
            material = Material.GREEN_STAINED_GLASS_PANE;
            lore = ChatColor.GREEN + "Working correctly!";
        }
        inventory.setItem(validSlots.get(1), ItemStackGenerator.generateItemStack(material, state, new ArrayList<>(Arrays.asList(lore))));
    }

    private void dungeonStatuses() {
        //continue counting from used inventory slots
        int counter = 2;
        for (Minidungeon minidungeon : Minidungeon.minidungeons.values()) {

            switch (minidungeon.dungeonPackagerConfigFields.getDungeonLocationType()) {
                case WORLD:
                    addWorldDungeon(minidungeon, counter);
                    break;
                case SCHEMATIC:
                    addSchematicDungeon(minidungeon, counter);
                    break;
                default:
                    new WarningMessage("Dungeon " + minidungeon.dungeonPackagerConfigFields.getFileName() + " does not have a valid location type and therefore can't be set up automatically!");
                    break;
            }
            minidungeonHashMap.put(validSlots.get(counter), minidungeon);
            counter++;

        }
    }

    private void addWorldDungeon(Minidungeon minidungeon, int counter) {

        String itemName = minidungeon.dungeonPackagerConfigFields.getName();
        List<String> lore = new ArrayList<>();

        addSize(lore, minidungeon);
        //boss count can't be calculated ahead of time here, unfortunately
        addInstallationString(lore, minidungeon);

        lore = ChatColorConverter.convert(lore);
        inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(getMaterial(minidungeon), itemName, lore));
    }

    private void addSchematicDungeon(Minidungeon minidungeon, int counter) {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE, ChatColorConverter.convert("&4You need WorldEdit to use this!")));
            return;
        }

        String itemName = minidungeon.dungeonPackagerConfigFields.getName();

        List<String> lore = new ArrayList<>();
        if (minidungeon.dungeonPackagerConfigFields.getCustomInfo() != null)
            lore.addAll(minidungeon.dungeonPackagerConfigFields.getCustomInfo());
        addSize(lore, minidungeon);
        addBossCount(lore, minidungeon);
        addInstallationString(lore, minidungeon);

        lore = ChatColorConverter.convert(lore);

        inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(getMaterial(minidungeon), itemName, lore));
    }

    private Material getMaterial(Minidungeon minidungeon) {
        if (minidungeon.isInstalled)
            return Material.GREEN_STAINED_GLASS_PANE;
        if (minidungeon.isDownloaded && minidungeon.bossesDownloaded)
            return Material.YELLOW_STAINED_GLASS_PANE;
        if (!minidungeon.bossesDownloaded)
            return Material.ORANGE_STAINED_GLASS_PANE;
        return Material.RED_STAINED_GLASS_PANE;
    }

    private void addSize(List<String> lore, Minidungeon minidungeon) {
        lore.add("&fSize: " + minidungeon.dungeonPackagerConfigFields.getDungeonSizeCategory().toString());
    }

    private void addBossCount(List<String> lore, Minidungeon minidungeon) {
        lore.add("&fRegional boss count: " + minidungeon.relativeDungeonLocations.bossCount);
    }

    private void addInstallationString(List<String> lore, Minidungeon minidungeon) {
        String status = "&fStatus: ";
        if (minidungeon.isInstalled) {
            lore.add(status + "&2already installed!");
            lore.add("&cClick to uninstall!");
            return;
        }
        if (!minidungeon.isDownloaded) {
            lore.add(status + "&4not downloaded!");
            lore.add("&4Download this at");
            lore.add("&9" + minidungeon.dungeonPackagerConfigFields.getDownloadLink() + " &f!");
            return;
        }
        if (!minidungeon.bossesDownloaded) {
            lore.add("&4Minidungeon boss files are not downloaded!");
            lore.add("&4Download this at");
            lore.add("&9" + minidungeon.dungeonPackagerConfigFields.getDownloadLink() + " &f!");
            return;
        }
        lore.add(status + "&aready to install!");
        lore.add("&2Click to install!");
    }


    public static class SetupMenuListeners implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteraction(InventoryClickEvent event) {
            SetupMenu setupMenu = setupMenus.get(event.getInventory());
            if (setupMenu == null) return;
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            //for permissions mode
            //for adventurer's guild world
            //for minidungeons
            Minidungeon minidungeon = setupMenu.minidungeonHashMap.get(event.getSlot());
            if (minidungeon != null) {
                if (!minidungeon.isDownloaded) {
                    player.sendMessage(ChatColorConverter.convert("&4Download this at &9" + minidungeon.dungeonPackagerConfigFields.getDownloadLink() + " &4!"));
                    player.closeInventory();
                    setupMenus.remove(event.getInventory());
                    return;
                }
                if (!minidungeon.bossesDownloaded) {
                    player.sendMessage(ChatColorConverter.convert("&4You are missing the boss files for this minidungeon!"));
                    player.sendMessage(ChatColorConverter.convert("&4Download this at &9" + minidungeon.dungeonPackagerConfigFields.getDownloadLink() + " &4!"));
                    player.closeInventory();
                    setupMenus.remove(event.getInventory());
                    return;
                }
                minidungeon.buttonToggleBehavior(player);
                setupMenus.remove(event.getInventory());
                player.closeInventory();
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) {
            setupMenus.remove(event.getInventory());
        }
    }

}
