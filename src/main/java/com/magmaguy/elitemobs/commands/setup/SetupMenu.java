package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.npcs.NPCInitializer;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.worlds.CustomWorldLoading;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        //reserve adventurer's guild
        adventurersGuildWorldStatus();
        //iterate through dungeons
        dungeonStatuses();
        setupMenus.put(inventory, this);
        player.openInventory(inventory);
    }

    boolean adventurersGuildIsDownloaded = false;

    private void adventurersGuildWorldStatus() {
        String state = "Adventurer's Guild world is";
        String lore;
        Material material;
        if (CustomWorldLoading.adventurersGuildWorldExists())
            adventurersGuildIsDownloaded = true;

        if (!adventurersGuildIsDownloaded) {
            material = Material.RED_STAINED_GLASS_PANE;
            lore = ChatColor.RED + "Not downloaded! Click to download!";
        } else {
            if (!AdventurersGuildConfig.guildWorldIsEnabled) {
                material = Material.ORANGE_STAINED_GLASS_PANE;
                lore = ChatColor.RED + "Not setup! Click to install!";
            } else {
                material = Material.GREEN_STAINED_GLASS_PANE;
                lore = ChatColor.GREEN + "Working correctly! Click to uninstall!";
            }
        }
        inventory.setItem(validSlots.get(0), ItemStackGenerator.generateItemStack(material, state, new ArrayList<>(Arrays.asList(lore))));
    }

    private void dungeonStatuses() {
        //continue counting from used inventory slots
        int counter = 1;
        for (Minidungeon minidungeon : Minidungeon.minidungeons.values()) {

            if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        ChatColorConverter.convert("&4You need WorldGuard to install Minidungeons correctly!")));
            else

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
        if (minidungeon.isDownloaded && !minidungeon.bossesDownloaded)
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
            if (event.getSlot() == 10) {
                //case where Adventurer's Guild Hub isn't downloaded
                if (!setupMenu.adventurersGuildIsDownloaded) {
                    player.closeInventory();
                    player.sendMessage("----------------------------------------------------");
                    player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Adventurer's Guild Hub download link: &9&nhttps://magmaguy.com/downloads/em_adventurers_guild.zip"));
                    player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aOnce downloaded, follow this setup guide: &9&nhttps://youtu.be/boRg2X4qhw4"));
                    player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Need help? " + DiscordLinks.mainLink));
                    player.sendMessage("----------------------------------------------------");
                } else {
                    //case for install
                    if (!AdventurersGuildConfig.guildWorldIsEnabled) {
                        try {
                            player.closeInventory();
                            player.sendMessage("----------------------------------------------------");
                            CustomWorldLoading.startupWorldInitialization();
                            AdventurersGuildCommand.defineTeleportLocation();
                            AdventurersGuildConfig.toggleGuildInstall();
                            //new NPCInitializer();
                            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                                WorldGuardCompatibility.protectWorldMinidugeonArea(AdventurersGuildCommand.defineTeleportLocation());
                                player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2The Adventurer's Guild Hub has been protected against griefing and mob spawning (among others)!"));
                            } else
                                player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4It is highly recommended you install WorldGuard to protect the Adventurer's Guild Hub World! Reinstall the hub through /em setup after installing WorldGuard in order to automatically protect the area!"));
                            PlayerTeleportEvent playerTeleportEvent = new PlayerTeleportEvent(player, AdventurersGuildConfig.guildWorldLocation);
                            new EventCaller(playerTeleportEvent);
                            if (!playerTeleportEvent.isCancelled())
                                player.teleport(AdventurersGuildConfig.guildWorldLocation);
                            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Successfully installed Adventurer's Guild Hub! Do &a/ag &2to go there and talk to the transporter or open the Teleports page in /em to go back!"));
                            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Need help? &9&n" + DiscordLinks.mainLink));
                            player.sendMessage("----------------------------------------------------");
                        } catch (Exception e) {
                            player.closeInventory();
                            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to install Adventurer's Guild Hub! Report this to the dev!"));
                            player.sendMessage("----------------------------------------------------");
                            e.printStackTrace();
                        }
                        //case for uninstall
                    } else {
                        try {
                            player.closeInventory();
                            Bukkit.unloadWorld(AdventurersGuildConfig.guildWorldLocation.getWorld(), true);
                            AdventurersGuildConfig.guildWorldLocation = null;
                            AdventurersGuildConfig.toggleGuildInstall();
                            player.sendMessage("----------------------------------------------------");
                            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Successfully uninstalled Adventurer's Guild Hub!"));
                            player.sendMessage("----------------------------------------------------");
                        } catch (Exception e) {
                            player.closeInventory();
                            player.sendMessage("----------------------------------------------------");
                            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to uninstall Adventurer's Guild Hub! Report this to the dev!"));
                            player.sendMessage("----------------------------------------------------");
                            e.printStackTrace();
                        }
                    }
                }
                return;
            }
            //for minidungeons
            Minidungeon minidungeon = setupMenu.minidungeonHashMap.get(event.getSlot());
            if (minidungeon != null) {
                if (!minidungeon.isDownloaded) {
                    player.sendMessage("----------------------------------------------------");
                    player.sendMessage(ChatColorConverter.convert("&4Download this at &9" + minidungeon.dungeonPackagerConfigFields.getDownloadLink() + " &4!"));
                    player.sendMessage("----------------------------------------------------");
                    player.closeInventory();
                    setupMenus.remove(event.getInventory());
                    return;
                }
                if (!minidungeon.bossesDownloaded) {
                    player.sendMessage("----------------------------------------------------");
                    player.sendMessage(ChatColorConverter.convert("&4You are missing the boss files for this minidungeon!"));
                    player.sendMessage(ChatColorConverter.convert("&4Download this at &9" + minidungeon.dungeonPackagerConfigFields.getDownloadLink() + " &4!"));
                    player.sendMessage("----------------------------------------------------");
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
