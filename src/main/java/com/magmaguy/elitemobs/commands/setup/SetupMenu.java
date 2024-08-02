package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.config.menus.premade.GetLootMenuConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SetupMenu {

    private static final int nextIcon = 35;
    private static final int infoIcon = 4;
    public static Map<Inventory, SetupMenu> setupMenus = new HashMap<>();
    private static List<EMPackage> emPackages = new ArrayList<>();
    private final int previousIcon = 27;
    Inventory inventory;
    Player player;
    ArrayList<Integer> validSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23,
            24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52));
    HashMap<Integer, EMPackage> minidungeonHashMap = new HashMap<>();
    @Getter
    private int currentPage = 1;

    public SetupMenu(Player player) {
        this.inventory = Bukkit.createInventory(player, 54, "Setup menu");
        this.player = player;
        redrawMenu(1, inventory);
    }

    private static void resourcePackButtonInteraction(Player player, SetupMenu setupMenu) {
        player.closeInventory();
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Processing your request, please wait...\n"));
        if (!player.hasPermission("elitemobs.*")) {
            player.sendMessage("[EliteMobs] You do not have the required permission (elitemobs.*) to do that!");
            return;
        }

        Logger.sendMessage(player, "This setting is now no longer in use! Use my ResourcePackManager plugin to easily and automatically merge & host the EliteMobs resource pack, and much more!");
        return;
//
//        if (ResourcePackDataConfig.isEliteMobsResourcePackEnabled()) {
//            //Case for if it is downloaded
//            ResourcePackDataConfig.toggleEliteMobsResourcePackStatus(false);
//        } else {
//            ResourcePackDataConfig.toggleEliteMobsResourcePackStatus(true);
//            player.spigot().sendMessage(SpigotMessage.simpleMessage("&8[EliteMobs] &2The EliteMobs resource pack has been installed!"));
//            player.spigot().sendMessage(SpigotMessage.commandHoverMessage("&eBefore you go! &fDo you want to force players to use the resource pack? This is necessary if you plan to use &cModelEngine for the custom boss models. &aClick here if you want to force resource packs. &eIgnore this message if you don't!", "Click to force resource packs!", "/elitemobs forceresourcepack"));
//        }
    }

//    public static void forceResourcePack(Player player) {
//        ResourcePackDataConfig.toggleForceResourcePack(true);
//        player.sendMessage("[EliteMobs] Using the resource pack is now mandatory!");
//    }

    private static void dungeonButtonInteraction(Player player, SetupMenu setupMenu, InventoryClickEvent event) {
        //for minidungeons
        EMPackage emPackage = setupMenu.minidungeonHashMap.get(event.getSlot());
        if (emPackage != null) {
            if (!emPackage.isDownloaded()) {
                player.sendMessage("----------------------------------------------------");
                player.sendMessage(ChatColorConverter.convert("&4Download this at &9" + emPackage.getDungeonPackagerConfigFields().getDownloadLink() + " &4!"));
                player.sendMessage("----------------------------------------------------");
                player.closeInventory();
                setupMenus.remove(event.getInventory());
                return;
            }
            if (!emPackage.isInstalled())
                emPackage.install(player);
            else if (!emPackage.uninstall(player)) {
                player.sendMessage("[EliteMobs] Failed to unload package because players were present in the worlds you were trying to unload! Remove the players from the dungeon before uninstalling it!");
            }
            setupMenus.remove(event.getInventory());
            player.closeInventory();
        }
    }

    private void redrawMenu(int page, Inventory inventory) {
        currentPage = page;
        setupMenus.remove(inventory);
        this.inventory = inventory;
        inventory.clear();
        //reserve resource pack status
        customResourcePackStatus();
        //iterate through dungeons
        dungeonStatuses();
        //Set icons
        addNavigationElements();
        player.openInventory(inventory);
        setupMenus.put(inventory, this);
    }

    private void addNavigationElements() {
        ItemStack infoButton = GetLootMenuConfig.infoItem;
        List<String> lore = ChatColorConverter.convert(List.of("&2To setup optional/recommended content for EliteMobs:",
                "&61) &fDownload content from &9magmaguy.itch.io &for &9patreon.com/magmaguy",
                "&62) &fPut content in the &2imports &ffolder of EliteMobs",
                "&63) &fDo &2/em reload",
                "&64) &fDo &2/em setup",
                "&65) &fClick on &eyellow &ficons to install!",
                "Click to get more info!"));
        ItemMeta itemMeta = infoButton.getItemMeta();
        itemMeta.setLore(lore);
        infoButton.setItemMeta(itemMeta);
        inventory.setItem(infoIcon, infoButton);

        ItemStack previousButton = GetLootMenuConfig.previousLootItem;
        ItemMeta previousButtonMeta = previousButton.getItemMeta();
        previousButtonMeta.setDisplayName("Previous page");
        previousButton.setItemMeta(previousButtonMeta);
        if (currentPage > 1)
            inventory.setItem(previousIcon, previousButton);

        ItemStack nextButton = GetLootMenuConfig.nextLootItem;
        ItemMeta nextButtonMeta = nextButton.getItemMeta();
        nextButtonMeta.setDisplayName("Next page");
        nextButton.setItemMeta(nextButtonMeta);
        int totalPages = (int) Math.ceil(emPackages.size() / 28d);
        if (totalPages > 1 && currentPage < totalPages)
            inventory.setItem(nextIcon, nextButton);
    }

    private void dungeonStatuses() {
        //Counter has to account for pages
        int dungeonCounter = 0;
        int inventoryLocationCounter = 0;
        if (currentPage != 1)
            dungeonCounter = validSlots.size() * (currentPage - 1) - 2;
        else
            inventoryLocationCounter = 1;
        List<EMPackage> rawEmPackages = EMPackage.getEmPackages().values().stream().toList();
        List<String> alphabeticalSort = new ArrayList<>();
        rawEmPackages.forEach(iteratedPackage -> alphabeticalSort.add(ChatColor.stripColor(ChatColorConverter.convert(iteratedPackage.getDungeonPackagerConfigFields().getName()))));
        Collections.sort(alphabeticalSort);
        emPackages = new ArrayList<>();
        alphabeticalSort.forEach(entry -> rawEmPackages.forEach(iteratedPackage -> {
            if (ChatColor.stripColor(ChatColorConverter.convert(iteratedPackage.getDungeonPackagerConfigFields().getName())).equals(entry))
                emPackages.add(iteratedPackage);
        }));
        minidungeonHashMap.clear();
        for (int i = dungeonCounter; i < emPackages.size(); i++) {
            if (currentPage == 1 && minidungeonHashMap.size() > validSlots.size() - 2) break;
            if (inventoryLocationCounter >= validSlots.size()) break;
            EMPackage emPackage = emPackages.get(i);

            addDungeonPackagerContent(emPackage, inventoryLocationCounter);

            minidungeonHashMap.put(validSlots.get(inventoryLocationCounter), emPackage);
            dungeonCounter++;
            inventoryLocationCounter++;
        }
    }

    private void addDungeonPackagerContent(EMPackage emPackage, int counter) {
        String itemName = emPackage.getDungeonPackagerConfigFields().getName();
        List<String> lore = new ArrayList<>();

        addSize(lore, emPackage);
        //boss count can't be calculated ahead of time here, unfortunately
        addInstallationString(lore, emPackage);

        lore = ChatColorConverter.convert(lore);
        inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(getMaterial(emPackage), itemName, lore));
    }

    private Material getMaterial(EMPackage emPackage) {
        if (emPackage.isOutOfDate())
            return Material.ORANGE_STAINED_GLASS_PANE;
        if (emPackage.isInstalled())
            return Material.GREEN_STAINED_GLASS_PANE;
        if (emPackage.isDownloaded())
            return Material.YELLOW_STAINED_GLASS_PANE;
        return Material.RED_STAINED_GLASS_PANE;
    }

    private void addSize(List<String> lore, EMPackage emPackage) {
        lore.add("&fSize: " + emPackage.getDungeonPackagerConfigFields().getDungeonSizeCategory().toString());
    }

    private void addInstallationString(List<String> lore, EMPackage emPackage) {
        String status = "&fStatus: ";
        if (emPackage.isOutOfDate()) {
            lore.add(status + "&4outdated!");
            lore.add("&4Download the update at");
            lore.add("&9" + emPackage.getDungeonPackagerConfigFields().getDownloadLink() + " &f!");
            lore.add("&cClick to uninstall!");
            return;
        }
        if (emPackage.isInstalled()) {
            lore.add(status + "&2already installed!");
            lore.add("&cClick to uninstall!");
            return;
        }
        if (!emPackage.isDownloaded()) {
            lore.add(status + "&4not downloaded!");
            lore.add("&4Download this at");
            lore.add("&9" + emPackage.getDungeonPackagerConfigFields().getDownloadLink() + " &f!");
            return;
        }
        lore.add(status + "&aready to install!");
        lore.add("&2Click to install!");
    }

    private void customResourcePackStatus() {
        if (currentPage != 1) return;
        String state = "Custom resource pack is";
        String lore;
        Material material;
        boolean resourceState = Bukkit.getPluginManager().isPluginEnabled("ResourcePackManager");

        if (!resourceState) {
            material = Material.YELLOW_STAINED_GLASS_PANE;
            lore = ChatColor.RED + "Not enabled! Click to enable!";
        } else {
            material = Material.GREEN_STAINED_GLASS_PANE;
            lore = ChatColor.GREEN + "Working correctly! Click to disable!";
        }

        inventory.setItem(validSlots.get(0), ItemStackGenerator.generateItemStack(material, state, new ArrayList<>(List.of(lore))));
    }

    public static class SetupMenuListeners implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteraction(InventoryClickEvent event) {
            SetupMenu setupMenu = setupMenus.get(event.getInventory());
            if (setupMenu == null) return;
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            //for resource pack
            if (event.getSlot() == 10 && setupMenu.getCurrentPage() == 1) {
                resourcePackButtonInteraction(player, setupMenu);
                return;
            }
            //for permissions mode
            if (event.getSlot() == infoIcon) {
                player.sendMessage(
                        "EliteMobs installation process:",
                        "Wiki page: https://github.com/MagmaGuy/EliteMobs/wiki/%5BGuide%5D-Quick-Setup",
                        "Video version: https://youtu.be/boRg2X4qhw4",
                        "Download links: free & premium https://magmaguy.itch.io/ | premium-only https://www.patreon.com/magmaguy",
                        "Discord support: https://discord.gg/9f5QSka");
                player.closeInventory();
                return;
            }
            if (event.getSlot() == setupMenu.previousIcon && event.getCurrentItem() != null) {
                setupMenu.redrawMenu(setupMenu.getCurrentPage() - 1, event.getInventory());
                return;
            }
            if (event.getSlot() == nextIcon && event.getCurrentItem() != null) {
                setupMenu.redrawMenu(setupMenu.getCurrentPage() + 1, event.getInventory());
                return;
            }

            dungeonButtonInteraction(player, setupMenu, event);
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            setupMenus.remove(event.getInventory());
        }

    }

}
