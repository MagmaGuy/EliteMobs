package com.magmaguy.elitemobs.versionnotifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.magmaguy.magmacore.nightbreak.NightbreakChatStyle;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.nightbreak.NightbreakContentManager;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VersionChecker {
    private static final List<EMPackage> outdatedPackages = Collections.synchronizedList(new ArrayList<>());
    @Getter
    private static final boolean SHA1Updated = false;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 60;
    private static final long REFRESH_COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes
    private static boolean pluginIsUpToDate = true;
    private static boolean connectionFailed = false;
    private static int connectionRetryCount = 0;
    private static final long CHECK_INTERVAL_TICKS = 20L * 60 * 60 * 24; // 24 hours in ticks
    private static NightbreakAccount.TokenChangeListenerRegistration
            tokenChangeListener;
    private static BukkitTask scheduledCheckTask;
    private static volatile long lifecycleGeneration;
    private record DlcCatalog(Map<String, Integer> versions, Set<String> slugs, Set<String> unpublishedSlugs) {}

    private static volatile DlcCatalog dlcCatalog = new DlcCatalog(Map.of(), Set.of(), Set.of());

    private VersionChecker() {
    }
    private static volatile long lastRefreshTimestamp = 0;

    private static boolean isCurrent(long generation) {
        return generation == lifecycleGeneration && !MetadataHandler.shutdownRequested;
    }

    private static void publish(long generation, Runnable publication) {
        if (!isCurrent(generation)) return;
        if (Bukkit.isPrimaryThread()) {
            publication.run();
            return;
        }
        try {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                if (isCurrent(generation)) publication.run();
            });
        } catch (org.bukkit.plugin.IllegalPluginAccessException disabled) {
            if (isCurrent(generation)) throw disabled;
        }
    }

    /**
     * Compares a Minecraft version with the current version on the server. Returns true if the version on the server is older.
     * Handles both legacy format (1.21.11) and new year.drop format (26.1).
     *
     * @param majorVersion Target major version to compare (e.g. 21 for 1.21.x, or 26 for 26.x)
     * @param minorVersion Target minor version to compare (e.g. 11 for 1.21.11, or 1 for 26.1)
     * @return Whether the version is under the value to be compared
     */
    public static boolean serverVersionOlderThan(int majorVersion, int minorVersion) {
        return com.magmaguy.magmacore.util.VersionChecker.serverVersionOlderThan(majorVersion, minorVersion);
    }
    private static void checkPluginVersion() {
        long generation = lifecycleGeneration;
        String installedVersion = MetadataHandler.PLUGIN.getDescription().getVersion();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isCurrent(generation)) return;
                String currentVersion = installedVersion;
                boolean snapshot = false;
                if (currentVersion.contains("SNAPSHOT")) {
                    snapshot = true;
                    currentVersion = currentVersion.split("-")[0];
                }
                String publicVersion;

                NightbreakAccount.VersionInfo versionInfo = NightbreakAccount.getPublicPluginVersion("elitemobs");
                if (versionInfo != null && versionInfo.version != null && !versionInfo.version.isBlank()) {
                    publicVersion = versionInfo.version;
                } else {
                    try {
                        publicVersion = VersionChecker.readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=40090");
                    } catch (IOException e) {
                        publish(generation, () -> handleConnectionError("plugin version check", e));
                        return;
                    }
                }

                boolean outdated = NightbreakPluginUpdater.compareVersions(publicVersion, currentVersion) > 0;
                boolean snapshotVersion = snapshot;
                String latestVersion = publicVersion;
                publish(generation, () -> {
                    Logger.info("Latest public release is " + latestVersion);
                    Logger.info("Your version is " + installedVersion);
                    if (outdated) {
                        outOfDateHandler();
                        return;
                    }
                    Logger.info(snapshotVersion
                            ? "You are running a snapshot version! You can check for updates in the #releases channel on the EliteMobs Discord!"
                            : "You are running the latest version!");
                    pluginIsUpToDate = true;
                });
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static void shutdown() {
        lifecycleGeneration++;
        if (tokenChangeListener != null) {
            tokenChangeListener.close();
            tokenChangeListener = null;
        }
        if (scheduledCheckTask != null) {
            scheduledCheckTask.cancel();
            scheduledCheckTask = null;
        }
        outdatedPackages.clear();
        connectionRetryCount = 0;
        connectionFailed = false;
        pluginIsUpToDate = true;
        lastRefreshTimestamp = 0;
        dlcCatalog = new DlcCatalog(Map.of(), Set.of(), Set.of());
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

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != 200)
                throw new IOException("Nightbreak API returned status code: " + responseCode);
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } finally {
            connection.disconnect();
        }
    }

    private static DlcCatalog parseNightbreakDlcResponse(String json) throws IOException {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            Map<String, Integer> versions = new HashMap<>();
            Set<String> slugs = new HashSet<>();
            Set<String> unpublished = new HashSet<>();
            boolean foundCategory = false;
            for (String category : List.of("accessible", "patreonRequired", "purchaseAvailable")) {
                JsonElement entries = root.get(category);
                if (entries == null) continue;
                foundCategory = true;
                for (JsonElement element : entries.getAsJsonArray()) {
                    JsonObject entry = element.getAsJsonObject();
                    String slug = catalogString(entry, "slug");
                    if (slug == null || slug.isBlank())
                        throw new IllegalStateException("DLC entry has no slug");
                    slugs.add(slug);
                    String version = catalogString(entry, "currentVersion");
                    if (version == null || version.isEmpty()) {
                        versions.remove(slug);
                        unpublished.add(slug);
                    } else {
                        versions.put(slug, Integer.parseInt(version.startsWith("v") ? version.substring(1) : version));
                        unpublished.remove(slug);
                    }
                }
            }
            if (!foundCategory) throw new IllegalStateException("DLC catalog has no recognized categories");
            return new DlcCatalog(Map.copyOf(versions), Set.copyOf(slugs), Set.copyOf(unpublished));
        } catch (JsonParseException | IllegalStateException | NumberFormatException exception) {
            throw new IOException("Invalid Nightbreak DLC catalog", exception);
        }
    }

    private static String catalogString(JsonObject entry, String key) {
        JsonElement value = entry.get(key);
        if (value == null || value.isJsonNull()) return null;
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
            throw new IllegalStateException("DLC " + key + " must be a string");
        return value.getAsString();
    }

    private static void checkContentVersion() {
        checkContentVersion(null);
    }

    private static void checkContentVersion(Runnable onComplete) {
        long generation = lifecycleGeneration;
        publish(generation, () -> {
            List<EMPackage> packageSnapshot = new ArrayList<>(EMPackage.getEmPackages().values());
            Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
                try {
                    if (!isCurrent(generation)) return;
                    DlcCatalog parsedCatalog = parseNightbreakDlcResponse(fetchFromNightbreak("https://nightbreak.io/api/dlc"));
                    publish(generation, () -> {
                        dlcCatalog = parsedCatalog;
                        connectionFailed = false;
                        connectionRetryCount = 0;
                        Logger.info("Parsed " + parsedCatalog.versions().size() + " content versions from Nightbreak API");
                        processContentVersionData(parsedCatalog.versions(), true);
                    });
                    if (!isCurrent(generation)) return;
                    prefetchAccessInfoInternal(packageSnapshot, parsedCatalog, generation);
                } catch (IOException failure) {
                    publish(generation, () -> {
                        lastRefreshTimestamp = 0;
                        handleConnectionError("content version check", failure);
                        if (connectionRetryCount >= MAX_RETRY_ATTEMPTS ||
                                !(failure instanceof UnknownHostException || failure instanceof ConnectException || failure instanceof SocketTimeoutException))
                            Logger.info("Using local data for content version checks as remote server is unavailable.");
                    });
                } finally {
                    if (onComplete != null) publish(generation, onComplete);
                }
            });
        });
    }
    /**
     * Process the content version data from Nightbreak API
     *
     * @param remoteVersions Map of slug to version from Nightbreak
     */
    private static void processContentVersionData(Map<String, Integer> remoteVersions, boolean notifyAdmins) {
        // Track newly found outdated packages
        List<EMPackage> newlyOutdated = new ArrayList<>();
        Set<EMPackage> checkedPackages = new HashSet<>();
        Set<EMPackage> currentlyOutdated = new HashSet<>();

        List<EMPackage> packageSnapshot = new ArrayList<>(EMPackage.getEmPackages().values());
        Set<String> metaChildren = EMPackage.getMetaPackageChildFilenames();
        for (EMPackage emPackage : packageSnapshot) {
            // Skip non-default dungeons unless they have a nightbreak slug for version checking
            String slug = emPackage.getContentPackagesConfigFields().getNightbreakSlug();
            if (!emPackage.getContentPackagesConfigFields().isDefaultDungeon() && (slug == null || slug.isEmpty())) continue;

            if (metaChildren.contains(emPackage.getContentPackagesConfigFields().getFilename())) continue;
            // slug already fetched above for defaultDungeon check
            if (slug == null || slug.isEmpty()) {
                // No slug configured, skip version checking for this content
                continue;
            }

            checkedPackages.add(emPackage);
            if (!emPackage.isInstalled()) {
                emPackage.setOutOfDate(false);
                outdatedPackages.remove(emPackage);
                continue;
            }

            Integer remoteVersion = remoteVersions.get(slug);
            if (remoteVersion == null) {
                if (dlcCatalog.unpublishedSlugs().contains(slug)) {
                    emPackage.setOutOfDate(false);
                    outdatedPackages.remove(emPackage);
                    continue;
                }
                Logger.warn("No version info found on Nightbreak for content: " + emPackage.getContentPackagesConfigFields().getName() + " (slug: " + slug + ")");
                emPackage.setOutOfDate(false);
                outdatedPackages.remove(emPackage);
                continue;
            }

            int localVersion = emPackage.getContentPackagesConfigFields().getDungeonVersion();
            if (remoteVersion > localVersion) {
                emPackage.setOutOfDate(true);
                currentlyOutdated.add(emPackage);
                synchronized (outdatedPackages) {
                    if (!outdatedPackages.contains(emPackage)) {
                        outdatedPackages.add(emPackage);
                        newlyOutdated.add(emPackage);
                    }
                }
                Logger.warn("Content " + emPackage.getContentPackagesConfigFields().getName() +
                        " is outdated! You should go download the updated version! Your version: " +
                        localVersion + " / remote version: " + remoteVersion +
                        " / Link: " + emPackage.getContentPageUrl());
            } else {
                emPackage.setOutOfDate(false);
                outdatedPackages.remove(emPackage);
            }
        }

        synchronized (outdatedPackages) {
            outdatedPackages.removeIf(emPackage -> checkedPackages.contains(emPackage) && !currentlyOutdated.contains(emPackage));
        }

        // Notify online admins about newly found outdated packages
        if (notifyAdmins && !newlyOutdated.isEmpty()) {
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
                long generation = lifecycleGeneration;
                Bukkit.getScheduler().runTaskLaterAsynchronously(MetadataHandler.PLUGIN,
                        () -> {
                            if (isCurrent(generation)) checkContentVersion();
                        }, 20L * RETRY_DELAY_SECONDS);
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
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        try (Scanner scanner = new Scanner(connection.getInputStream(),
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
        publish(lifecycleGeneration, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("elitemobs.versionnotification")) continue;

                Logger.sendSimpleMessage(player, NightbreakChatStyle.separator());
                NightbreakPluginUpdater.PluginUpdateCheck pluginUpdateCheck = currentPluginUpdateCheck();
                if (hasPluginUpdateNotice(pluginUpdateCheck)) {
                    sendPluginUpdateNotice(player, pluginUpdateCheck);
                    Logger.sendSimpleMessage(player, "&8&m-----------------------------------------------------");
                }
                Logger.sendMessage(player, CommandMessagesConfig.getContentUpdatesAvailable().replace("$count", String.valueOf(newlyOutdated.size())));
                for (EMPackage emPackage : newlyOutdated) {
                    String name = emPackage.getContentPackagesConfigFields().getName();
                    sendContentPageEntry(player, emPackage, CommandMessagesConfig.getContentUpdateEntry().replace("$name", name));
                }
                player.spigot().sendMessage(
                        SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionUseSetupMessage()),
                        SpigotMessage.commandHoverMessage(
                                InitializeConfig.getEmSetupDisplay(),
                                InitializeConfig.getEmSetupHover(),
                                "/em setup"),
                        SpigotMessage.simpleMessage(" &8| "),
                        SpigotMessage.hoverLinkMessage(
                                InitializeConfig.getContentLinkDisplay(),
                                InitializeConfig.getContentLinkHover(),
                                "https://nightbreak.io/plugin/elitemobs/#content"
                        )
                );
                if (NightbreakAccount.hasToken()) {
                    player.spigot().sendMessage(
                            SpigotMessage.commandHoverMessage(
                                    CommandMessagesConfig.getVersionAutoUpdateMessage(),
                                    CommandMessagesConfig.getVersionAutoUpdateHover(),
                                    "/em updatecontent"
                            )
                    );
                }
                Logger.sendSimpleMessage(player, NightbreakChatStyle.separator());
            }
        });
    }

    public static void check() {
        long generation = lifecycleGeneration;
        // Run immediately on startup
        checkPluginVersion();
        checkContentVersion();

        // Refresh content + access info the moment an account token shows up,
        // whether via /nightbreaklogin in this classloader or via another
        // MagmaCore-shading plugin writing the shared config file. Without
        // this, /em setup keeps showing "no account token linked" until the
        // server restarts because the first /em setup open pre-login stamps
        // the throttle cooldown for 5 minutes.
        if (tokenChangeListener != null) {
            tokenChangeListener.close();
        }
        tokenChangeListener = NightbreakAccount.registerTokenChangeListener(
                () -> publish(generation, () -> {
                    invalidateRefreshCooldown();
                    if (NightbreakAccount.hasToken()) {
                        Logger.info("Account token changed; refreshing content access info.");
                        refreshContentAndAccess();
                    }
                }));

        // Schedule repeating task every 24 hours
        if (scheduledCheckTask != null) {
            scheduledCheckTask.cancel();
        }
        scheduledCheckTask = Bukkit.getScheduler().runTaskTimer(
                MetadataHandler.PLUGIN, () -> {
            if (!isCurrent(generation)) return;
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
    public static boolean refreshContentAndAccess() {
        return refreshContentAndAccess(null);
    }

    public static boolean refreshContentAndAccess(Runnable onComplete) {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTimestamp < REFRESH_COOLDOWN_MS) return false;
        lastRefreshTimestamp = now;
        checkContentVersion(onComplete);
        return true;
    }

    public static void applyCachedContentAndAccess() {
        Map<String, Integer> cachedVersions = new HashMap<>();
        NightbreakContentManager.getVersionCache().forEach((slug, versionInfo) -> {
            if (slug != null && versionInfo != null && versionInfo.versionInt > 0) {
                cachedVersions.put(slug, versionInfo.versionInt);
            }
        });
        if (!cachedVersions.isEmpty()) {
            processContentVersionData(cachedVersions, false);
        }

        var accessCache = NightbreakContentManager.getAccessCache();
        for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
            String slug = emPackage.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null) continue;
            NightbreakAccount.AccessInfo accessInfo = accessCache.get(slug);
            if (accessInfo != null) emPackage.setCachedAccessInfo(accessInfo);
        }
    }
    private static NightbreakPluginUpdater.PluginUpdateCheck currentPluginUpdateCheck() {
        NightbreakPluginUpdater.CachedPluginUpdateCheck snapshot = NightbreakPluginUpdater.getCachedUpdateCheck(
                (JavaPlugin) MetadataHandler.PLUGIN,
                EliteMobs.NIGHTBREAK_PLUGIN_SPEC);
        return snapshot.check();
    }

    private static boolean hasPluginUpdateNotice(NightbreakPluginUpdater.PluginUpdateCheck check) {
        if (check != null) return check.updateAvailable();
        return !pluginIsUpToDate;
    }

    private static void sendContentPageEntry(Player player, EMPackage emPackage, String displayText) {
        String link = emPackage.getContentPageUrl();
        if (link == null || link.isBlank()) {
            player.sendMessage(ChatColorConverter.convert(displayText));
            return;
        }
        player.spigot().sendMessage(
                SpigotMessage.hoverLinkMessage(
                        displayText,
                        ChatColorConverter.convert(CommandMessagesConfig.getVersionClickDownloadHover()),
                        link));
    }

    private static void sendPluginUpdateNotice(Player player, NightbreakPluginUpdater.PluginUpdateCheck check) {
        Logger.sendSimpleMessage(player, "<g:#8B0000:#FF4500:#DAA520>EliteMobs plugin update available</g>");
        if (check != null && check.remoteVersion() != null && !check.remoteVersion().isBlank()) {
            Logger.sendSimpleMessage(player, "&7Installed plugin: &f" + check.localVersion()
                    + " &8| &7Available plugin: &a" + check.remoteVersion());
        }
        Logger.sendSimpleMessage(player, "&eThis is separate from content updates. Downloading the plugin update requires a server restart.");
        player.spigot().sendMessage(
                SpigotMessage.commandHoverMessage(
                        "&a[Download the EliteMobs plugin update]",
                        "&eRuns /em downloadpluginupdate. Restart required after download.",
                        "/em downloadpluginupdate"));
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage("&7Manual download page: "),
                SpigotMessage.hoverLinkMessage(
                        "&9&nhttps://nightbreak.io/plugin/elitemobs/download/",
                        "&7Click to open the public EliteMobs download page.",
                        "https://nightbreak.io/plugin/elitemobs/download/"));
    }

    /**
     * Forces a refresh on the next call, bypassing the cooldown. Used by the
     * Account token-change listener; without this, opening /em setup
     * pre-login stamps the cooldown even though the refresh short-circuited,
     * and the legitimate post-login refresh gets throttled out for 5 minutes.
     */
    public static void invalidateRefreshCooldown() {
        lastRefreshTimestamp = 0L;
    }

    /**
     * Prefetches access info for all content packages with Nightbreak slugs.
     * Called internally after version checks complete.
     */
    private static void prefetchAccessInfoInternal(List<EMPackage> packageSnapshot, DlcCatalog catalog, long generation) {
        if (!isCurrent(generation) || !NightbreakAccount.hasToken()) return;

        // Deduplicate by slug — many packages share the same slug, no need to hit the API repeatedly
        Map<String, NightbreakAccount.AccessInfo> slugCache = new HashMap<>();
        Map<EMPackage, NightbreakAccount.AccessInfo> resolvedAccess = new HashMap<>();
        List<String> failedSlugs = new ArrayList<>();
        boolean authFailed = NightbreakAccount.hasAuthFailure();
        if (authFailed) {
            publish(generation, VersionChecker::logNightbreakTokenRejected);
            return;
        }

        for (EMPackage pkg : packageSnapshot) {
            // Bail fast if the plugin is being disabled mid-loop — each
            // checkAccess can take up to 10s on its HTTP timeout, and Bukkit
            // nags ("not properly shutting down its async tasks") if this
            // task is still alive when onDisable returns.
            if (!isCurrent(generation)) return;

            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null || slug.isEmpty()) continue;
            if (!catalog.slugs().contains(slug) || catalog.unpublishedSlugs().contains(slug)) continue;

            NightbreakAccount.AccessInfo info;
            if (slugCache.containsKey(slug)) {
                info = slugCache.get(slug);
            } else {
                info = NightbreakAccount.getInstance().checkAccess(slug);
                if (!isCurrent(generation)) return;
                if (NightbreakAccount.hasAuthFailure()) {
                    authFailed = true;
                    break;
                }
                slugCache.put(slug, info);
                if (info == null && !failedSlugs.contains(slug)) {
                    failedSlugs.add(slug);
                }
            }

            if (info != null) {
                resolvedAccess.put(pkg, info);
            }
        }
        boolean rejectedToken = authFailed;
        publish(generation, () -> {
            resolvedAccess.forEach(EMPackage::setCachedAccessInfo);
            if (!resolvedAccess.isEmpty())
                Logger.info("Prefetched Nightbreak access info for " + resolvedAccess.size() + " content packages");
            if (rejectedToken) logNightbreakTokenRejected();
            else if (!failedSlugs.isEmpty())
                Logger.warn("Failed to prefetch access info for " + failedSlugs.size() + " slugs: " + String.join(", ", failedSlugs));
        });
    }

    private static void logNightbreakTokenRejected() {
        String status = NightbreakAccount.getLastAuthFailureStatus() > 0
                ? " (HTTP " + NightbreakAccount.getLastAuthFailureStatus() + ")"
                : "";
        Logger.warn("The saved account token needs to be updated" + status
                + ". Get a new token at https://nightbreak.io/account/ and run "
                + "/nightbreaklogin <token>, then run /em setup again.");
    }

    public static class VersionCheckerEvents implements Listener {
        @EventHandler
        public void onPlayerLogin(PlayerJoinEvent event) {

            if (!event.getPlayer().hasPermission("elitemobs.versionnotification")) return;
            long generation = lifecycleGeneration;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isCurrent(generation) || !event.getPlayer().isOnline()) return;

                    if (connectionFailed && event.getPlayer().hasPermission("elitemobs.admin")) {
                        event.getPlayer().sendMessage(CommandMessagesConfig.getVersionCheckConnectionWarning());
                    }

                    List<EMPackage> outdatedSnapshot;
                    synchronized (outdatedPackages) {
                        outdatedPackages.removeIf(emPackage -> !emPackage.isOutOfDate());
                        outdatedSnapshot = new ArrayList<>(outdatedPackages);
                    }

                    NightbreakPluginUpdater.PluginUpdateCheck pluginUpdateCheck = currentPluginUpdateCheck();
                    boolean pluginUpdateAvailable = hasPluginUpdateNotice(pluginUpdateCheck);
                    if (pluginUpdateAvailable && outdatedSnapshot.isEmpty()) {
                        Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                        sendPluginUpdateNotice(event.getPlayer(), pluginUpdateCheck);
                        Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                    }

                    if (!outdatedSnapshot.isEmpty()) {
                        Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                        if (pluginUpdateAvailable) {
                            sendPluginUpdateNotice(event.getPlayer(), pluginUpdateCheck);
                            Logger.sendSimpleMessage(event.getPlayer(), "&8&m-----------------------------------------------------");
                        }
                        Logger.sendMessage(event.getPlayer(), CommandMessagesConfig.getDungeonsOutdatedMessage());
                        for (EMPackage emPackage : outdatedSnapshot) {
                            String name = emPackage.getContentPackagesConfigFields().getName();
                            sendContentPageEntry(event.getPlayer(), emPackage, CommandMessagesConfig.getVersionOutdatedEntryPrefix() + name);
                        }

                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionDownloadAtMessage()),
                                SpigotMessage.hoverLinkMessage(
                                        InitializeConfig.getContentLinkDisplay(),
                                        InitializeConfig.getContentLinkHover(),
                                        "https://nightbreak.io/plugin/elitemobs/#content"
                                ),
                                SpigotMessage.simpleMessage(" !")
                        );
                        event.getPlayer().spigot().sendMessage(
                                SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionUpdateEasyMessage()),
                                SpigotMessage.hoverLinkMessage(
                                        CommandMessagesConfig.getVersionClickHereLabel(),
                                        CommandMessagesConfig.getVersionWikiHover(),
                                        "https://nightbreak.io/plugin/elitemobs/#setup"
                                ),
                                SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionInstallInfoMessage()),
                                SpigotMessage.hoverLinkMessage(
                                        CommandMessagesConfig.getVersionHereLabel(),
                                        CommandMessagesConfig.getVersionDiscordHover(),
                                        DiscordLinks.mainLink
                                ),
                                SpigotMessage.simpleMessage(CommandMessagesConfig.getVersionSupportRoomMessage())
                        );
                        // Suggest update command if an account token is registered
                        if (NightbreakAccount.hasToken()) {
                            event.getPlayer().spigot().sendMessage(
                                    SpigotMessage.commandHoverMessage(
                                            CommandMessagesConfig.getVersionAutoUpdateMessage(),
                                            CommandMessagesConfig.getVersionAutoUpdateHover(),
                                            "/em updatecontent"
                                    )
                            );
                        }
                        Logger.sendSimpleMessage(event.getPlayer(), NightbreakChatStyle.separator());
                    }
                    if (SHA1Updated) {
                        event.getPlayer().sendMessage(CommandMessagesConfig.getResourcePackUpdatedMessage());
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L * 3);
        }
    }
}
