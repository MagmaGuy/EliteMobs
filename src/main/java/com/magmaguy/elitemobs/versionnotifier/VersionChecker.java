package com.magmaguy.elitemobs.versionnotifier;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.SpigotMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VersionChecker {
    private static final List<Minidungeon> outdatedDungeons = new ArrayList<>();
    private static boolean pluginIsUpToDate = true;
    private VersionChecker() {
    }

    public static void check() {
        checkPluginVersion();
        checkDungeonVersions();
    }

    private static void checkPluginVersion() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String currentVersion = MetadataHandler.PLUGIN.getDescription().getVersion();
                boolean snapshot = false;
                if (currentVersion.contains("SNAPSHOT")) {
                    snapshot = true;
                    currentVersion = currentVersion.split("-")[0];
                }
                String publicVersion = "";

                try {
                    Bukkit.getLogger().info("[EliteMobs] Latest public release is " + VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090"));
                    Bukkit.getLogger().info("[EliteMobs] Your version is " + MetadataHandler.PLUGIN.getDescription().getVersion());
                    publicVersion = VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090");
                } catch (IOException e) {
                    Bukkit.getLogger().warning("[EliteMobs] Couldn't check latest version");
                    return;
                }

                if (Double.parseDouble(currentVersion.split("\\.")[0]) < Double.parseDouble(publicVersion.split("\\.")[0])) {
                    outOfDateHandler();
                    return;
                }

                if (Double.parseDouble(currentVersion.split("\\.")[0]) == Double.parseDouble(publicVersion.split("\\.")[0])) {

                    if (Double.parseDouble(currentVersion.split("\\.")[1]) < Double.parseDouble(publicVersion.split("\\.")[1])) {
                        outOfDateHandler();
                        return;
                    }

                    if (Double.parseDouble(currentVersion.split("\\.")[1]) == Double.parseDouble(publicVersion.split("\\.")[1])) {
                        if (Double.parseDouble(currentVersion.split("\\.")[2]) < Double.parseDouble(publicVersion.split("\\.")[2])) {
                            outOfDateHandler();
                            return;
                        }
                    }
                }

                if (!snapshot)
                    Bukkit.getLogger().info("[EliteMobs] You are running the latest version!");
                else
                    new InfoMessage("You are running a snapshot version! You can check for updates in the #releases channel on the EliteMobs Discord!");

                pluginIsUpToDate = true;
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    private static void checkDungeonVersions() {
        Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
            outdatedDungeons.clear();
            for (Minidungeon minidungeon : Minidungeon.getMinidungeons().values())
                if (minidungeon.isInstalled()) {
                    try {
                        String versionString = readStringFromURL("https://www.magmaguy.com/api/" + minidungeon.getDungeonPackagerConfigFields().getFilename().replace(".yml", ""));
                        int releaseVersion = Integer.parseInt(versionString);
                        if (minidungeon.getDungeonPackagerConfigFields().getDungeonVersion() < releaseVersion) {
                            minidungeon.setOutOfDate(true);
                            outdatedDungeons.add(minidungeon);
                            new WarningMessage("Dungeon " + minidungeon.getDungeonPackagerConfigFields().getName() + " is outdated! You should go download the updated version! Link: " + minidungeon.getDungeonPackagerConfigFields().getDownloadLink());
                        }
                    } catch (Exception exception) {
                        new WarningMessage("Failed to get version for minidungeon " + minidungeon.getDungeonPackagerConfigFields().getFilename() + "! The URL " + "https://www.magmaguy.com/api/" + minidungeon.getDungeonPackagerConfigFields().getFilename().replace(".yml", "") + " could not be reached!");
                    }
                }
        });
    }

    private static String readStringFromURL(String url) throws IOException {

        try (Scanner scanner = new Scanner(new URL(url).openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

    }

    private static void outOfDateHandler() {

        new WarningMessage("[EliteMobs] A newer version of this plugin is available for download!");
        pluginIsUpToDate = false;

    }

    public static class VersionCheckerEvents implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerJoinEvent event) {

            if (!event.getPlayer().hasPermission("elitemobs.versionnotification")) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().isOnline()) return;
                    if (!pluginIsUpToDate)
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&a[EliteMobs] &cYour version of EliteMobs is outdated." +
                                " &aYou can download the latest version from &3&n&ohttps://www.spigotmc.org/resources/%E2%9A%94elitemobs%E2%9A%94.40090/"));
                    if (!outdatedDungeons.isEmpty()) {
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&a[EliteMobs] &cThe following dungeons are outdated:"));
                        for (Minidungeon minidungeon : outdatedDungeons)
                            event.getPlayer().sendMessage(ChatColorConverter.convert(
                                    "&c- " + minidungeon.getDungeonPackagerConfigFields().getName()));
                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage("&8[EliteMobs]&f You can download the update on "),
                                SpigotMessage.hoverLinkMessage(
                                        "&9&npatreon.com/magmaguy", "Click for Patreon link", "https://www.patreon.com/magmaguy"
                                ),
                                SpigotMessage.simpleMessage(" &for "),
                                SpigotMessage.hoverLinkMessage("&9&nmagmaguy.itch.io", "Click for itch.io link", "https://magmaguy.itch.io/"),
                                SpigotMessage.simpleMessage(" !"));
                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage("&2Updating is quick & easy! "),
                                SpigotMessage.hoverLinkMessage("&9&nClick here", "Click for wiki link", "https://github.com/MagmaGuy/EliteMobs/wiki/%5BGuide%5D-Quick-Setup#updating-dungeon-content"),
                                SpigotMessage.simpleMessage(" &2for info on how to install updates and "),
                                SpigotMessage.hoverLinkMessage("&9&nhere", "Discord support link", DiscordLinks.mainLink),
                                SpigotMessage.simpleMessage(" &2for the support room.")
                        );
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L * 3);

        }
    }

}
