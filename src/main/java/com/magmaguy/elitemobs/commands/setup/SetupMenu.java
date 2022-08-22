package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigurationExporter;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.*;
import com.magmaguy.elitemobs.worlds.CustomWorldLoading;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SetupMenu {

    public static Map<Inventory, SetupMenu> setupMenus = new HashMap<>();

    Inventory inventory;
    Player player;
    ArrayList<Integer> validSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23,
            24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
    HashMap<Integer, EMPackage> minidungeonHashMap = new HashMap<>();
    boolean adventurersGuildIsDownloaded = false;

    public SetupMenu(Player player) {
        this.inventory = Bukkit.createInventory(player, 54, "Setup menu");
        this.player = player;
        //reserve resource pack status
        customResourcePackStatus();
        //reserve adventurer's guild
        adventurersGuildWorldStatus();
        //iterate through dungeons
        dungeonStatuses();
        setupMenus.put(inventory, this);
        player.openInventory(inventory);
    }

    private static <FileOutputStream> void resourcePackButtonInteraction(Player player, SetupMenu setupMenu) {
        if (!player.hasPermission("elitemobs.*")) {
            player.sendMessage("[EliteMobs] You do not have the required permission (elitemobs.*) to do that!");
            return;
        }

        if (ResourcePackDataConfig.isEliteMobsResourcePackEnabled()) {
            //Case for if it is downloaded
            if (ServerPropertiesModifier.modify(player, "resource-pack", "") &&
                    ServerPropertiesModifier.modify(player, "resource-pack-sha1", "") &&
                    ServerPropertiesModifier.modify(player, "require-resource-pack", "")) {
                player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Reverted the following server.properties" +
                        " configuration settings to default: &aresource-pack, resource-pack-sha1 and require-resource-pack." +
                        " &fYou have successfully removed the EliteMobs resource pack!"));
                player.sendMessage(ChatColorConverter.convert("If you are doing this to remove the " +
                        "require-resource-pack setting, you can now do &a/em setup &fand install the resource pack again!"));
            } else {
                player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cFailed to set the following settings" +
                        " to default values: &eresource-pack, resource-pack-sha1 and require-resource-pack. &cYou will need to" +
                        " set these manually back to nothing in server.properties."));
            }
            ResourcePackDataConfig.toggleEliteMobsResourcePackStatus(false);
        } else {
            URL fetchWebsite = null;
            try {
                fetchWebsite = new URL("https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip");
            } catch (MalformedURLException e) {
                player.sendMessage("[EliteMobs] Failed to get resource pack from  https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip ! This might mean the server is down, in which case you will have to host the resource pack on your own! (1)");
                return;
            }
            File file = new File("elitemobs_resource_pack.zip");
            try {
                FileUtils.copyURLToFile(fetchWebsite, file);
            } catch (IOException e) {
                player.sendMessage("[EliteMobs] Failed to get resource pack from  https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip ! This might mean the server is down, in which case you will have to host the resource pack on your own! (2)");
                return;
            }
            String SHA1 = null;
            try {
                SHA1 = ConfigurationExporter.sha1Code(file);
            } catch (Exception e) {
                player.sendMessage("[EliteMobs] Failed to generate your SHA1 key! You will have to do this manually, though this might mean there is a serious problem with the resource pack.");
                return;
            }

            if (!ServerPropertiesModifier.modify(player, "resource-pack-sha1", SHA1)) {
                player.sendMessage("[EliteMobs] Failed to modify your server.properties SHA1 key which should be " + SHA1 + "  ! You will have to set this and the link to the resource pack manually.");
                return;
            }

            if (!ServerPropertiesModifier.modify(player, "resource-pack", "https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip")) {
                player.sendMessage("[EliteMobs] Failed to set the link to the resource pack correctly! You will have to set this manually.");
                return;
            }
            ResourcePackDataConfig.toggleEliteMobsResourcePackStatus(true);
            player.spigot().sendMessage(SpigotMessage.simpleMessage("&8[EliteMobs] &2The EliteMobs resource pack has been installed! &cThis requires a server restart to work correctly!"));
            player.spigot().sendMessage(SpigotMessage.commandHoverMessage("&eBefore you go! &fDo you want to force players to use the resource pack? This is necessary if you plan to use &cModelEngine for the custom boss models. &aClick here if you want to force resource packs. &eIgnore this message if you don't!", "Click to force resource packs!", "/elitemobs forceresourcepack"));
        }
    }

    public static void forceResourcePack(Player player) {
        if (!ServerPropertiesModifier.modify(player, "require-resource-pack", "true")) {
            player.sendMessage("[EliteMobs] Failed to set the resource pack requirement! You will have to do this manually on server.properties !");
            return;
        }
        player.sendMessage("[EliteMobs] Using the resource pack is now mandatory! This requires a server restart to work correctly.");
    }

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
                if (emPackage instanceof SchematicPackage)
                    emPackage.install(player, true); //todo this needs to differentiate between paste and no paste
                else emPackage.install(player);
            else if (emPackage instanceof SchematicPackage) emPackage.uninstall(player);
            else if (!emPackage.uninstall(player)) {
                player.sendMessage("[EliteMobs] Failed to unload package because players were present in the worlds you were trying to unload! Remove the players from the dungeon before uninstalling it!");
            }
            setupMenus.remove(event.getInventory());
            player.closeInventory();
        }
    }

    private void dungeonStatuses() {
        //continue counting from used inventory slots
        int counter = 2;
        for (EMPackage emPackage : EMPackage.getEmPackages().values()) {

            if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        ChatColorConverter.convert("&4You need WorldGuard to install Minidungeons correctly!")));
            else

                switch (emPackage.getDungeonPackagerConfigFields().getDungeonLocationType()) {
                    case WORLD:
                        addWorldDungeon(emPackage, counter);
                        break;
                    case SCHEMATIC:
                        addSchematicDungeon(emPackage, counter);
                        break;
                    default:
                        new WarningMessage("Dungeon " + emPackage.getDungeonPackagerConfigFields().getFilename() + " does not have a valid location type and therefore can't be set up automatically!");
                        break;
                }
            minidungeonHashMap.put(validSlots.get(counter), emPackage);
            counter++;

        }
    }

    private void addWorldDungeon(EMPackage emPackage, int counter) {

        String itemName = emPackage.getDungeonPackagerConfigFields().getName();
        List<String> lore = new ArrayList<>();

        addSize(lore, emPackage);
        //boss count can't be calculated ahead of time here, unfortunately
        addInstallationString(lore, emPackage);

        lore = ChatColorConverter.convert(lore);
        inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(getMaterial(emPackage), itemName, lore));
    }

    private void addSchematicDungeon(EMPackage emPackage, int counter) {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE, ChatColorConverter.convert("&4You need WorldEdit to use this!")));
            return;
        }

        String itemName = emPackage.getDungeonPackagerConfigFields().getName();

        List<String> lore = new ArrayList<>();
        if (emPackage.getDungeonPackagerConfigFields().getCustomInfo() != null)
            lore.addAll(emPackage.getDungeonPackagerConfigFields().getCustomInfo());
        addSize(lore, emPackage);
        addBossCount(lore, emPackage);
        addInstallationString(lore, emPackage);

        lore = ChatColorConverter.convert(lore);

        inventory.setItem(validSlots.get(counter), ItemStackGenerator.generateItemStack(getMaterial(emPackage), itemName, lore));
    }

    private Material getMaterial(EMPackage emPackage) {
        if (emPackage.isInstalled())
            return Material.GREEN_STAINED_GLASS_PANE;
        if (emPackage.isDownloaded())
            return Material.YELLOW_STAINED_GLASS_PANE;
        return Material.RED_STAINED_GLASS_PANE;
    }

    private void addSize(List<String> lore, EMPackage emPackage) {
        lore.add("&fSize: " + emPackage.getDungeonPackagerConfigFields().getDungeonSizeCategory().toString());
    }

    private void addBossCount(List<String> lore, EMPackage emPackage) {
        try {
            lore.add("&fRegional boss count: " + emPackage.getCustomBossEntityList().size());
        } catch (Exception ex) {
            //todo: fix this
            //new WarningMessage("Failed to determine regional boss count! Are the relative dungeon locations correct?");
        }
    }

    private static void adventurersGuildButtonInteraction(Player player, SetupMenu setupMenu) {
        //case where Adventurer's Guild Hub isn't downloaded
        if (!setupMenu.adventurersGuildIsDownloaded) {
            player.closeInventory();
            player.sendMessage("----------------------------------------------------");
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Adventurer's Guild Hub download link: https://magmaguy.itch.io/"));
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aOnce downloaded, follow this setup guide: &9&nhttps://youtu.be/boRg2X4qhw4"));
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Need help? " + DiscordLinks.mainLink));
            player.sendMessage("----------------------------------------------------");
        } else {
            //case for install
            if (!AdventurersGuildConfig.isGuildWorldIsEnabled()) {
                try {
                    player.closeInventory();
                    player.sendMessage("----------------------------------------------------");
                    CustomWorldLoading.startupWorldInitialization();
                    AdventurersGuildCommand.defineTeleportLocation();
                    AdventurersGuildConfig.toggleGuildInstall();
                    if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                        WorldGuardCompatibility.protectWorldMinidugeonArea(AdventurersGuildCommand.defineTeleportLocation());
                        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2The Adventurer's Guild Hub has been protected against griefing and mob spawning (among others)!"));
                    } else
                        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4It is highly recommended you install WorldGuard to protect the Adventurer's Guild Hub World! Reinstall the hub through /em setup after installing WorldGuard in order to automatically protect the area!"));
                    PlayerTeleportEvent playerTeleportEvent = new PlayerTeleportEvent(player, AdventurersGuildConfig.getGuildWorldLocation());
                    new EventCaller(playerTeleportEvent);
                    if (!playerTeleportEvent.isCancelled())
                        player.teleport(AdventurersGuildConfig.getGuildWorldLocation());
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
                    Bukkit.unloadWorld(AdventurersGuildConfig.getGuildWorldLocation().getWorld(), true);
                    AdventurersGuildConfig.setGuildWorldLocation(null);
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
    }

    private void addInstallationString(List<String> lore, EMPackage emPackage) {
        String status = "&fStatus: ";
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
        String state = "Custom resource pack is";
        String lore;
        Material material;
        boolean resourceState = ResourcePackDataConfig.isEliteMobsResourcePackEnabled();

        if (!resourceState) {
            material = Material.RED_STAINED_GLASS_PANE;
            lore = ChatColor.RED + "Not enabled! Click to enable!";
        } else {
            material = Material.GREEN_STAINED_GLASS_PANE;
            lore = ChatColor.GREEN + "Working correctly! Click to disable!";
        }

        inventory.setItem(validSlots.get(0), ItemStackGenerator.generateItemStack(material, state, new ArrayList<>(Arrays.asList(lore))));
    }

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
            if (!AdventurersGuildConfig.isGuildWorldIsEnabled()) {
                material = Material.ORANGE_STAINED_GLASS_PANE;
                lore = ChatColor.RED + "Not setup! Click to install!";
            } else {
                material = Material.GREEN_STAINED_GLASS_PANE;
                lore = ChatColor.GREEN + "Working correctly! Click to uninstall!";
            }
        }
        inventory.setItem(validSlots.get(1), ItemStackGenerator.generateItemStack(material, state, new ArrayList<>(Arrays.asList(lore))));
    }

    public static class SetupMenuListeners implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteraction(InventoryClickEvent event) {
            SetupMenu setupMenu = setupMenus.get(event.getInventory());
            if (setupMenu == null) return;
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            //for resource pack
            if (event.getSlot() == 10) {
                resourcePackButtonInteraction(player, setupMenu);
                player.closeInventory();
                return;
            }
            //for permissions mode
            //for adventurer's guild world
            if (event.getSlot() == 11) {
                adventurersGuildButtonInteraction(player, setupMenu);
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
