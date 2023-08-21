package com.magmaguy.elitemobs.versionnotifier;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationExporter;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.utils.*;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VersionChecker {
    private static final List<EMPackage> outdatedPackages = new ArrayList<>();
    private static boolean pluginIsUpToDate = true;
    @Getter
    private static boolean SHA1Updated = false;

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
            outdatedPackages.clear();
            for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
                if (emPackage.isInstalled()) {
                    if (!emPackage.getDungeonPackagerConfigFields().isDefaultDungeon()) continue;
                    try {
                        String versionString = readStringFromURL("https://www.magmaguy.com/api/" + emPackage.getDungeonPackagerConfigFields().getFilename().replace(".yml", ""));
                        int releaseVersion = Integer.parseInt(versionString);
                        if (emPackage.getDungeonPackagerConfigFields().getDungeonVersion() < releaseVersion) {
                            emPackage.setOutOfDate(true);
                            outdatedPackages.add(emPackage);
                            new WarningMessage("Dungeon " + emPackage.getDungeonPackagerConfigFields().getName() + " is outdated! You should go download the updated version! Link: " + emPackage.getDungeonPackagerConfigFields().getDownloadLink());
                        }
                    } catch (Exception exception) {
                        new WarningMessage("Failed to get version for EliteMobs package " + emPackage.getDungeonPackagerConfigFields().getFilename() + "! The URL " + "https://www.magmaguy.com/api/" + emPackage.getDungeonPackagerConfigFields().getFilename().replace(".yml", "") + " could not be reached!");
                    }
                }
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

        new WarningMessage("[EliteMobs] A newer version of this plugin is available for download!");
        pluginIsUpToDate = false;

    }

    public static void check() {
        checkPluginVersion();
        checkDungeonVersions();
        checkForEliteMobsResourcePackUpdate();
    }

    public static void checkForEliteMobsResourcePackUpdate() {
        if (!ResourcePackDataConfig.isEliteMobsResourcePackEnabled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, task -> {
            URL fetchWebsite = null;
            try {
                fetchWebsite = new URL("https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip");
            } catch (MalformedURLException e) {
                new WarningMessage("[EliteMobs] Failed to get resource pack from  https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip ! This might mean the server is down, in which case you will have to host the resource pack on your own! (1)");
                return;
            }
            File tempFile = new File("elitemobs_resource_pack.zip");
            try {
                FileUtils.copyURLToFile(fetchWebsite, tempFile);
            } catch (IOException e) {
                new WarningMessage("[EliteMobs] Failed to get resource pack from  https://www.magmaguy.com/downloads/elitemobs_resource_pack.zip ! This might mean the server is down, in which case you will have to host the resource pack on your own! (2)");
                return;
            }
            String SHA1 = null;
            try {
                SHA1 = ConfigurationExporter.sha1Code(tempFile);
            } catch (Exception e) {
                new WarningMessage("[EliteMobs] Failed to generate your SHA1 key! You will have to do this manually, though this might mean there is a serious problem with the resource pack.");
                return;
            }
            String existingSHA1 = ServerPropertiesModifier.getValue("resource-pack-sha1");
            if (existingSHA1 == null) {
                new WarningMessage("Failed to get the current SHA1 key!");
                return;
            }
            //todo: this will require testing the next time the resource pack gets updated
            if (!existingSHA1.equals(SHA1)) {
                if (!ServerPropertiesModifier.modify(Bukkit.getConsoleSender(), "resource-pack-sha1", SHA1)) {
                    new WarningMessage("Failed to set new SHA1 which should be " + SHA1 + " ! You will have to do this manually.");
                    return;
                } else {
                    new InfoMessage("Successfully set the new SHA1 value!");
                    new WarningMessage("Changing the SHA1 key requires a restart to apply correctly! Restart as soon as possible!");
                    SHA1Updated = true;
                }
                new InfoMessage("The EliteMobs resource pack has updated! The SHA1 code will be updated to match the new resource pack!");
            }
        });
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
                    if (!outdatedPackages.isEmpty()) {
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&a[EliteMobs] &cThe following dungeons are outdated:"));
                        for (EMPackage emPackage : outdatedPackages)
                            event.getPlayer().sendMessage(ChatColorConverter.convert(
                                    "&c- " + emPackage.getDungeonPackagerConfigFields().getName()));
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
                    if (SHA1Updated) {
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cThe EliteMobs resource pack has updated! This means that the current resource pack will not fully work until you restart your server. You only need to restart once!"));
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L * 3);

        }
    }

}
