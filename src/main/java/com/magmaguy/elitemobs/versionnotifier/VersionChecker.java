package com.magmaguy.elitemobs.versionnotifier;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import lombok.Getter;
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
    private static final List<EMPackage> outdatedPackages = new ArrayList<>();
    @Getter
    private static final boolean SHA1Updated = false;
    private static boolean pluginIsUpToDate = true;

    private VersionChecker() {
    }

    /**
     * Compares a Minecraft version with the current version on the server. Returns true if the version on the server is older.
     *
     * @param majorVersion Target major version to compare (i.e. 1.>>>17<<<.0)
     * @param minorVersion Target minor version to compare (i.e. 1.17.>>>0<<<)
     * @return Whether the version is under the value to be compared
     */
    public static boolean serverVersionOlderThan(int majorVersion, int minorVersion) {

        String[] splitVersion = Bukkit.getBukkitVersion().split("[.]");

        int actualMajorVersion = Integer.parseInt(splitVersion[1].split("-")[0]);

        int actualMinorVersion = 0;
        if (splitVersion.length > 2)
            actualMinorVersion = Integer.parseInt(splitVersion[2].split("-")[0]);

        if (actualMajorVersion < majorVersion)
            return true;

        if (splitVersion.length > 2)
            return actualMajorVersion == majorVersion && actualMinorVersion < minorVersion;

        return false;

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
                    Logger.info("Latest public release is " + VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090"));
                    Logger.info("Your version is " + MetadataHandler.PLUGIN.getDescription().getVersion());
                    publicVersion = VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090");
                } catch (IOException e) {
                    Logger.warn("Couldn't check latest version");
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
                    Logger.info("You are running the latest version!");
                else
                    Logger.info("You are running a snapshot version! You can check for updates in the #releases channel on the EliteMobs Discord!");

                pluginIsUpToDate = true;
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    private static void checkContentVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
            try {
                String remoteVersions = readStringFromURL("https://www.magmaguy.com/api/elitemobs_content");
                String[] lines = remoteVersions.split("\n");
                for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
                    if (!emPackage.isInstalled()) continue;
                    if (!emPackage.getContentPackagesConfigFields().isDefaultDungeon()) continue;
                    boolean containedInMetaPackage = false;
                    for (EMPackage metaPackage : EMPackage.getEmPackages().values())
                        if (metaPackage.getContentPackagesConfigFields().getContainedPackages() != null &&
                                !metaPackage.getContentPackagesConfigFields().getContainedPackages().isEmpty() &&
                                metaPackage.getContentPackagesConfigFields().getContainedPackages().contains(emPackage.getContentPackagesConfigFields().getFilename())) {
                            containedInMetaPackage = true;
                            break;
                        }

                    if (containedInMetaPackage) continue;
                    boolean checked = false;
                    for (String line : lines) {
                        if (line.startsWith(emPackage.getContentPackagesConfigFields().getFilename().replace(".yml", ""))) {
                            String[] split = line.split(":");
                            int remoteVersion = 0;
                            try {
                                remoteVersion = Integer.parseInt(split[1].trim());
                            } catch (Exception e) {
                                Logger.warn("Remote version substring: " + split[1].trim());
                                e.printStackTrace();
                            }
                            if (remoteVersion > emPackage.getContentPackagesConfigFields().getDungeonVersion()) {
                                emPackage.setOutOfDate(true);
                                outdatedPackages.add(emPackage);
                                Logger.warn("Content " + emPackage.getContentPackagesConfigFields().getName() +
                                        " is outdated! You should go download the updated version! Your version: " +
                                        emPackage.getContentPackagesConfigFields().getDungeonVersion() + " / remote version: " +
                                        remoteVersion + " / Link: " + emPackage.getContentPackagesConfigFields().getDownloadLink());
                            }
                            checked = true;
                            break;
                        }
                    }
                    if (!checked)
                        Logger.warn("Failed to check content " + emPackage.getContentPackagesConfigFields().getFilename() + " ! The remote server doesn't have a version listed for it, report it to the developer!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String readStringFromURL(String url) throws IOException {

        try (Scanner scanner = new Scanner(new URL(url).openStream(),
                StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

    }

    private static void outOfDateHandler() {

        Logger.warn("[EliteMobs] A newer version of this plugin is available for download!");
        pluginIsUpToDate = false;

    }

    public static void check() {
        checkPluginVersion();
        checkContentVersion();
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
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&cYour version of EliteMobs is outdated." +
                                " &aYou can download the latest version from &3&n&ohttps://www.spigotmc.org/resources/%E2%9A%94elitemobs%E2%9A%94.40090/"));
                    if (!outdatedPackages.isEmpty()) {
                        Logger.sendSimpleMessage(event.getPlayer(), "&8&m-----------------------------------------------------");
                        Logger.sendMessage(event.getPlayer(), "&cThe following dungeons are outdated:");
                        for (EMPackage emPackage : outdatedPackages) {
                            event.getPlayer().spigot().sendMessage(
                                    SpigotMessage.hoverLinkMessage("&c- " + emPackage.getContentPackagesConfigFields().getName(),
                                            ChatColorConverter.convert("&9Click to go to download link!"),
                                            emPackage.getContentPackagesConfigFields().getDownloadLink()));
                        }
                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage("&8[EliteMobs]&f You can download the update at "),
                                SpigotMessage.hoverLinkMessage(
                                        "&9&nhttps://nightbreak.io/plugin/elitemobs/#content", "Click for Nightbreak link", "https://nightbreak.io/plugin/elitemobs/#content"
                                ),
                                SpigotMessage.simpleMessage(" !"));
                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage("&2Updating is quick & easy! "),
                                SpigotMessage.hoverLinkMessage("&9&nClick here", "Click for wiki link", "https://nightbreak.io/plugin/elitemobs/#setup"),
                                SpigotMessage.simpleMessage(" &2for info on how to install updates and "),
                                SpigotMessage.hoverLinkMessage("&9&nhere", "Discord support link", DiscordLinks.mainLink),
                                SpigotMessage.simpleMessage(" &2for the support room.")
                        );
                        Logger.sendSimpleMessage(event.getPlayer(), "&8&m-----------------------------------------------------");
                    }
                    if (SHA1Updated) {
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cThe EliteMobs resource pack has updated! This means that the current resource pack will not fully work until you restart your server. You only need to restart once!"));
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L * 3);

        }
    }

}
