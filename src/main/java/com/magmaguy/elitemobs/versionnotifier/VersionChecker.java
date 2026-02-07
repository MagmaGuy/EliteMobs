package com.magmaguy.elitemobs.versionnotifier;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
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
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VersionChecker {
    private static final List<EMPackage> outdatedPackages = new ArrayList<>();
    @Getter
    private static final boolean SHA1Updated = false;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 60;
    private static final long REFRESH_COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes
    private static boolean pluginIsUpToDate = true;
    private static boolean connectionFailed = false;
    private static int connectionRetryCount = 0;
    private static final long CHECK_INTERVAL_TICKS = 20L * 60 * 60 * 24; // 24 hours in ticks

    private VersionChecker() {
    }
    private static volatile long lastRefreshTimestamp = 0;

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

    public static void shutdown() {
        outdatedPackages.clear();
        connectionRetryCount = 0;
        connectionFailed = false;
        pluginIsUpToDate = true;
        lastRefreshTimestamp = 0;
    }

    /**
     * Fetches JSON data from the Nightbreak API
     *
     * @param urlString The URL to fetch from
     * @return The JSON response as a string
     * @throws IOException If the request fails
     */
    private static String fetchFromNightbreak(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Nightbreak API returned status code: " + responseCode);
        }

        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * Parses the Nightbreak /api/dlc response and extracts slug -> version mappings
     *
     * @param json The JSON response from the API
     * @return Map of slug to version number
     */
    private static Map<String, Integer> parseNightbreakDlcResponse(String json) {
        Map<String, Integer> versions = new HashMap<>();

        // Parse DLC entries from all categories (accessible, patreonRequired, purchaseAvailable)
        String[] categories = {"accessible", "patreonRequired", "purchaseAvailable"};

        for (String category : categories) {
            String categoryKey = "\"" + category + "\":";
            int categoryStart = json.indexOf(categoryKey);
            if (categoryStart == -1) continue;

            // Find the array for this category
            int arrayStart = json.indexOf("[", categoryStart);
            if (arrayStart == -1) continue;

            int arrayEnd = findMatchingBracket(json, arrayStart);
            if (arrayEnd == -1) continue;

            String arrayContent = json.substring(arrayStart, arrayEnd + 1);
            parseEntriesFromArray(arrayContent, versions);
        }

        return versions;
    }

    /**
     * Finds the matching closing bracket for an opening bracket
     */
    private static int findMatchingBracket(String json, int openPos) {
        int depth = 0;
        boolean inString = false;
        for (int i = openPos; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    /**
     * Parses individual DLC entries from a JSON array and extracts slug/version
     */
    private static void parseEntriesFromArray(String arrayJson, Map<String, Integer> versions) {
        int pos = 0;
        while (pos < arrayJson.length()) {
            int objectStart = arrayJson.indexOf("{", pos);
            if (objectStart == -1) break;

            int objectEnd = findMatchingBrace(arrayJson, objectStart);
            if (objectEnd == -1) break;

            String objectJson = arrayJson.substring(objectStart, objectEnd + 1);

            String slug = extractJsonString(objectJson, "slug");
            String versionStr = extractJsonString(objectJson, "currentVersion");

            if (slug != null && versionStr != null && !versionStr.isEmpty()) {
                try {
                    // Version format is "v11" or similar, strip the 'v' prefix
                    String numericVersion = versionStr.startsWith("v") ? versionStr.substring(1) : versionStr;
                    int version = Integer.parseInt(numericVersion);
                    versions.put(slug, version);
                } catch (NumberFormatException e) {
                    Logger.warn("Failed to parse version '" + versionStr + "' for slug '" + slug + "'");
                }
            } else if (slug != null && (versionStr == null || versionStr.isEmpty())) {
                // Slug exists but no version - this is expected for some free content
                // Only log at debug level to avoid spam
            }

            pos = objectEnd + 1;
        }
    }

    /**
     * Finds the matching closing brace for an opening brace
     */
    private static int findMatchingBrace(String json, int openPos) {
        int depth = 0;
        boolean inString = false;
        for (int i = openPos; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    /**
     * Extracts a string value from a JSON object
     */
    private static String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int valueStart = keyIndex + searchKey.length();
        // Skip whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) return null;

        // Check for null value
        if (json.substring(valueStart).startsWith("null")) {
            return null;
        }

        // Check for string value
        if (json.charAt(valueStart) == '"') {
            int stringEnd = json.indexOf("\"", valueStart + 1);
            if (stringEnd == -1) return null;
            return json.substring(valueStart + 1, stringEnd);
        }

        return null;
    }

    private static void checkContentVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
            try {
                String jsonResponse = fetchFromNightbreak("https://nightbreak.io/api/dlc");
                connectionFailed = false;
                connectionRetryCount = 0;

                Map<String, Integer> remoteVersions = parseNightbreakDlcResponse(jsonResponse);
                Logger.info("Parsed " + remoteVersions.size() + " content versions from Nightbreak API");
                processContentVersionData(remoteVersions);

                // Prefetch access info after version check completes
                prefetchAccessInfoInternal();

            } catch (IOException e) {
                handleConnectionError("content version check", e);

                if (connectionRetryCount >= MAX_RETRY_ATTEMPTS ||
                        !(e instanceof UnknownHostException || e instanceof ConnectException || e instanceof SocketTimeoutException)) {
                    Logger.info("Using local data for content version checks as remote server is unavailable.");
                }
            }
        });
    }

    /**
     * Process the content version data from Nightbreak API
     *
     * @param remoteVersions Map of slug to version from Nightbreak
     */
    private static void processContentVersionData(Map<String, Integer> remoteVersions) {
        // Track newly found outdated packages
        List<EMPackage> newlyOutdated = new ArrayList<>();

        // Snapshot to avoid ConcurrentModificationException from async iteration
        List<EMPackage> packageSnapshot = new ArrayList<>(EMPackage.getEmPackages().values());

        for (EMPackage emPackage : packageSnapshot) {
            if (!emPackage.isInstalled()) continue;
            if (!emPackage.getContentPackagesConfigFields().isDefaultDungeon()) continue;

            // Skip packages contained in meta packages
            boolean containedInMetaPackage = false;
            for (EMPackage metaPackage : packageSnapshot) {
                if (metaPackage.getContentPackagesConfigFields().getContainedPackages() != null &&
                        !metaPackage.getContentPackagesConfigFields().getContainedPackages().isEmpty() &&
                        metaPackage.getContentPackagesConfigFields().getContainedPackages().contains(emPackage.getContentPackagesConfigFields().getFilename())) {
                    containedInMetaPackage = true;
                    break;
                }
            }
            if (containedInMetaPackage) continue;

            // Get the Nightbreak slug from the content package
            String slug = emPackage.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null || slug.isEmpty()) {
                // No slug configured, skip version checking for this content
                continue;
            }

            Integer remoteVersion = remoteVersions.get(slug);
            if (remoteVersion == null) {
                Logger.warn("No version info found on Nightbreak for content: " + emPackage.getContentPackagesConfigFields().getName() + " (slug: " + slug + ")");
                continue;
            }

            int localVersion = emPackage.getContentPackagesConfigFields().getDungeonVersion();
            if (remoteVersion > localVersion) {
                emPackage.setOutOfDate(true);
                if (!outdatedPackages.contains(emPackage)) {
                    outdatedPackages.add(emPackage);
                    newlyOutdated.add(emPackage);
                }
                Logger.warn("Content " + emPackage.getContentPackagesConfigFields().getName() +
                        " is outdated! You should go download the updated version! Your version: " +
                        localVersion + " / remote version: " + remoteVersion +
                        " / Link: " + emPackage.getContentPackagesConfigFields().getDownloadLink());
            }
        }

        // Notify online admins about newly found outdated packages
        if (!newlyOutdated.isEmpty()) {
            notifyOnlineAdmins(newlyOutdated);
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

    /**
     * Notifies all online players with admin permission about outdated content.
     * This is called after version check completes so admins don't need to relog.
     */
    private static void notifyOnlineAdmins(List<EMPackage> newlyOutdated) {
        // Must run on main thread to access Bukkit
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("elitemobs.versionnotification")) continue;

                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                Logger.sendMessage(player, "&e" + newlyOutdated.size() + " content update(s) available:");
                for (EMPackage emPackage : newlyOutdated) {
                    String name = emPackage.getContentPackagesConfigFields().getName();
                    player.sendMessage(ChatColorConverter.convert("&e- " + name));
                }
                player.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&7Use &e/em setup &7to view and update, or "),
                        SpigotMessage.hoverLinkMessage(
                                "&9&nhttps://nightbreak.io/plugin/elitemobs/#content",
                                "Click for Nightbreak link",
                                "https://nightbreak.io/plugin/elitemobs/#content"
                        )
                );
                if (NightbreakAccount.hasToken()) {
                    player.spigot().sendMessage(
                            SpigotMessage.commandHoverMessage(
                                    "&a[Click here to update all content automatically]",
                                    "&eRuns /em updatecontent",
                                    "/em updatecontent"
                            )
                    );
                }
                Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
            }
        });
    }

    public static void check() {
        // Run immediately on startup
        checkPluginVersion();
        checkContentVersion();

        // Schedule repeating task every 24 hours
        Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
            Logger.info("Running scheduled 24-hour version and access check...");
            checkPluginVersion();
            checkContentVersion();
        }, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    /**
     * Public method to trigger a version and access refresh.
     * Called when setup menu is opened to ensure fresh data.
     * Throttled to prevent excessive API calls when opened repeatedly.
     */
    public static void refreshContentAndAccess() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTimestamp < REFRESH_COOLDOWN_MS) return;
        lastRefreshTimestamp = now;
        checkContentVersion();
    }

    /**
     * Prefetches access info for all content packages with Nightbreak slugs.
     * Called internally after version checks complete.
     */
    private static void prefetchAccessInfoInternal() {
        if (!NightbreakAccount.hasToken()) return;

        // Snapshot to avoid ConcurrentModificationException from async iteration
        List<EMPackage> packageSnapshot = new ArrayList<>(EMPackage.getEmPackages().values());

        // Deduplicate by slug — many packages share the same slug, no need to hit the API repeatedly
        Map<String, NightbreakAccount.AccessInfo> slugCache = new HashMap<>();
        List<String> failedSlugs = new ArrayList<>();
        int prefetched = 0;

        for (EMPackage pkg : packageSnapshot) {
            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null || slug.isEmpty()) continue;

            NightbreakAccount.AccessInfo info;
            if (slugCache.containsKey(slug)) {
                info = slugCache.get(slug);
            } else {
                info = NightbreakAccount.getInstance().checkAccess(slug);
                slugCache.put(slug, info);
                if (info == null && !failedSlugs.contains(slug)) {
                    failedSlugs.add(slug);
                }
            }

            if (info != null) {
                pkg.setCachedAccessInfo(info);
                prefetched++;
            }
        }
        if (prefetched > 0) {
            Logger.info("Prefetched Nightbreak access info for " + prefetched + " content packages");
        }
        if (!failedSlugs.isEmpty()) {
            Logger.warn("Failed to prefetch access info for " + failedSlugs.size() + " slugs: " + String.join(", ", failedSlugs));
        }
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

                        // Suggest update command if Nightbreak token is registered
                        if (NightbreakAccount.hasToken()) {
                            event.getPlayer().spigot().sendMessage(
                                    SpigotMessage.commandHoverMessage(
                                            "&a[Click here to update all content automatically]",
                                            "&eRuns /em updatecontent",
                                            "/em updatecontent"
                                    )
                            );
                        }
                    }
                    if (SHA1Updated) {
                        event.getPlayer().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cThe EliteMobs resource pack has updated! This means that the current resource pack will not fully work until you restart your server. You only need to restart once!"));
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L * 3);
        }
    }
}