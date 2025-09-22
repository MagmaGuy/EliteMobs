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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VersionChecker {
    private static final List<EMPackage> outdatedPackages = new ArrayList<>();
    @Getter
    private static final boolean SHA1Updated = false;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 60;
    private static boolean pluginIsUpToDate = true;
    private static boolean connectionFailed = false;
    private static int connectionRetryCount = 0;

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
                    publicVersion = VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090");
                    Logger.info("Latest public release is " + publicVersion);
                    Logger.info("Your version is " + MetadataHandler.PLUGIN.getDescription().getVersion());
                } catch (IOException e) {
                    handleConnectionError("plugin version check", e);
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
                connectionFailed = false; // Reset the flag if successful
                connectionRetryCount = 0; // Reset retry count

                String[] lines = remoteVersions.split("\n");
                processContentVersionData(lines);

            } catch (IOException e) {
                handleConnectionError("content version check", e);

                // If we've tried enough times, or this is not a connection issue,
                // use cached data if available or proceed without updates
                if (connectionRetryCount >= MAX_RETRY_ATTEMPTS ||
                        !(e instanceof UnknownHostException || e instanceof ConnectException || e instanceof SocketTimeoutException)) {
                    Logger.info("Using local data for content version checks as remote server is unavailable.");
                    // Each package will be considered up-to-date since we can't verify
                }
            }
        });
    }

    /**
     * Process the content version data that was successfully retrieved
     *
     * @param lines Lines of data from the remote version file
     */
    private static void processContentVersionData(String[] lines) {
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
    }

    /**
     * Handles connection errors with proper logging and retry logic
     *
     * @param checkType Type of check being performed
     * @param e         The exception that occurred
     */
    private static void handleConnectionError(String checkType, Exception e) {
        connectionFailed = true;

        if (e instanceof UnknownHostException || e instanceof ConnectException || e instanceof SocketTimeoutException) {
            // Network-related errors that can be retried
            connectionRetryCount++;

            if (connectionRetryCount <= MAX_RETRY_ATTEMPTS) {
                Logger.warn("Network error during " + checkType + ". Will retry in " + RETRY_DELAY_SECONDS +
                        " seconds (Attempt " + connectionRetryCount + "/" + MAX_RETRY_ATTEMPTS + ")");

                // Schedule a retry after delay
                Bukkit.getScheduler().runTaskLaterAsynchronously(MetadataHandler.PLUGIN,
                        () -> checkContentVersion(), 20L * RETRY_DELAY_SECONDS);
            } else {
                Logger.warn("Failed to connect for " + checkType + " after " + MAX_RETRY_ATTEMPTS +
                        " attempts. Will continue without version checking. Error: " + e.getMessage());
            }
        } else {
            // Other errors that should be properly logged
            Logger.warn("Error during " + checkType + ": " + e.getMessage());
            if (e.getCause() != null) {
                Logger.warn("Caused by: " + e.getCause().getMessage());
            }
        }
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

                    if (connectionFailed && event.getPlayer().hasPermission("elitemobs.admin")) {
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &eWarning: Could not connect to update servers. " +
                                "Version checking is currently unavailable. Check your internet connection or try again later."));
                    }

                    if (!pluginIsUpToDate)
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&cYour version of EliteMobs is outdated." +
                                " &aYou can download the latest version from &3&n&ohttps://nightbreak.io/plugin/elitemobs/"));

                    if (!outdatedPackages.isEmpty()) {
                        Logger.sendSimpleMessage(event.getPlayer(), "&8&m-----------------------------------------------------");
                        Logger.sendMessage(event.getPlayer(), "&cThe following dungeons are outdated:");
                        for (EMPackage emPackage : outdatedPackages) {
                            String name = emPackage.getContentPackagesConfigFields().getName();
                            String link = emPackage.getContentPackagesConfigFields().getDownloadLink();

                            if (link != null && !link.isEmpty()) {
                                // only send the hover-link if we actually have a URL
                                event.getPlayer().spigot().sendMessage(
                                        SpigotMessage.hoverLinkMessage(
                                                "&c- " + name,
                                                ChatColorConverter.convert("&9Click to go to download link!"),
                                                link
                                        )
                                );
                            } else {
                                // fall back to plain text if link is missing
                                event.getPlayer().sendMessage(
                                        ChatColorConverter.convert("&c- " + name + " &7(no download link available)")
                                );
                            }
                        }

                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage("&8[EliteMobs]&f You can download the update at "),
                                SpigotMessage.hoverLinkMessage(
                                        "&9&nhttps://nightbreak.io/plugin/elitemobs/#content",
                                        "Click for Nightbreak link",
                                        "https://nightbreak.io/plugin/elitemobs/#content"
                                ),
                                SpigotMessage.simpleMessage(" !")
                        );
                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage("&2Updating is quick & easy! "),
                                SpigotMessage.hoverLinkMessage(
                                        "&9&nClick here",
                                        "Click for wiki link",
                                        "https://nightbreak.io/plugin/elitemobs/#setup"
                                ),
                                SpigotMessage.simpleMessage(" &2for info on how to install updates and "),
                                SpigotMessage.hoverLinkMessage(
                                        "&9&nhere",
                                        "Discord support link",
                                        DiscordLinks.mainLink
                                ),
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