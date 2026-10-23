package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.magmacore.menus.NightbreakSetupIcons;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.magmacore.menus.ContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.nightbreak.NightbreakContentManager;
import com.magmaguy.magmacore.nightbreak.NightbreakManagedContent;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EMPackage extends ContentPackage implements NightbreakManagedContent {

    private static final String ELITEMOBS_CONTENT_PAGE = "https://nightbreak.io/plugin/elitemobs/";

    protected static final Map<String, EMPackage> content = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, EMPackage> emPackages = new ConcurrentHashMap<>();
    @Getter
    protected final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    @Setter
    protected volatile boolean isDownloaded;
    @Getter
    @Setter
    protected volatile boolean isInstalled;
    @Setter
    protected volatile boolean outOfDate = false;
    @Getter
    @Setter
    protected volatile NightbreakAccount.AccessInfo cachedAccessInfo = null;
    @Getter
    protected List<CustomBossEntity> customBossEntityList = new ArrayList<>();
    protected List<TreasureChest> treasureChestList = new ArrayList<>();
    protected List<NPCEntity> npcEntities = new ArrayList<>();

    public EMPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        emPackages.put(contentPackagesConfigFields.getFilename(), this);
        baseInitialization();
    }

    /**
     * Data gets stored under two formats: either the filename, i.e. the filename to a custom boss, or the name of a world
     * for world-based content
     *
     * @param data name
     * @return EM package associated to this data
     */
    public static EMPackage getContent(String data) {
        return content.get(data);
    }

    public static void shutdown() {
        BulkToggle batch = bulkToggle;
        bulkToggle = null; // Invalidates callbacks from this generation before waiting for disk settlement.
        if (batch != null) {
            if (batch.flushTask != null) batch.flushTask.cancel();
            if (batch.worker != null) reportSaveFailures(batch.worker.join());
        }
        content.clear();
        emPackages.clear();
    }

    // Server-thread admission owns intent; one worker receives only sealed file/string snapshots.
    private static BulkToggle bulkToggle;

    private static final class BulkToggle {
        private final Map<java.nio.file.Path, MemberIntent> intents = new LinkedHashMap<>();
        private final Player player;
        private final String reloadingMessage;
        private org.bukkit.scheduler.BukkitTask flushTask;
        private CompletableFuture<List<MemberResult>> worker;
        private BulkToggle(Player player, String reloadingMessage) {
            this.player = player;
            this.reloadingMessage = reloadingMessage;
        }
    }

    private record MemberIntent(com.magmaguy.magmacore.config.CustomConfigFields fields, boolean enabled) { }
    private record MemberSave(MemberIntent intent, File file, String contents) { }
    private record MemberResult(MemberSave save, RuntimeException failure) { }

    protected static boolean bulkMemberTogglesLocked() {
        return bulkToggle != null && bulkToggle.worker != null;
    }

    protected static void submitBulkMemberSaves(Player player,
            Collection<? extends com.magmaguy.magmacore.config.CustomConfigFields> members,
            boolean enabled, String reloadingMessage) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Content toggles require the server thread");
        if (bulkMemberTogglesLocked()) throw new IllegalStateException("Content toggle already saving");
        Map<java.nio.file.Path, MemberIntent> incoming = new LinkedHashMap<>();
        for (var fields : members) {
            try {
                incoming.put(fields.getFile().getCanonicalFile().toPath(), new MemberIntent(fields, enabled));
            } catch (java.io.IOException failure) {
                throw new java.io.UncheckedIOException("Cannot identify content file " + fields.getFile(), failure);
            }
        }
        if (bulkToggle == null) {
            BulkToggle batch = new BulkToggle(player, reloadingMessage);
            bulkToggle = batch;
            try {
                batch.flushTask = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> flushBulkMemberSaves(batch));
            } catch (RuntimeException failure) {
                bulkToggle = null;
                throw failure;
            }
        }
        // Last same-tick intent wins, including opposing toggles and shared meta-package members.
        bulkToggle.intents.putAll(incoming);
    }

    private static void flushBulkMemberSaves(BulkToggle batch) {
        if (bulkToggle != batch || MetadataHandler.shutdownRequested) return;
        List<MemberSave> saves = new ArrayList<>();
        try {
            for (var entry : batch.intents.entrySet()) {
                MemberIntent intent = entry.getValue();
                if (intent.fields().isEnabled() == intent.enabled()) continue;
                var snapshot = new org.bukkit.configuration.file.YamlConfiguration();
                snapshot.loadFromString(intent.fields().getWritableFileConfiguration().saveToString());
                snapshot.set("isEnabled", intent.enabled());
                saves.add(new MemberSave(intent, entry.getKey().toFile(), snapshot.saveToString()));
            }
        } catch (Exception failure) {
            bulkToggle = null;
            Logger.warn("Content toggle could not be prepared; no member was saved: " + failure);
            if (batch.player != null && batch.player.isOnline())
                Logger.sendMessage(batch.player, DungeonsConfig.getContentConfigurationSaveFailedMessage());
            return;
        }
        if (saves.isEmpty()) { bulkToggle = null; return; }
        batch.worker = CompletableFuture.supplyAsync(() -> {
            List<MemberResult> results = new ArrayList<>();
            for (MemberSave save : saves) {
                try {
                    com.magmaguy.magmacore.config.ConfigurationEngine.fileSaverSerialized(save.contents(), save.file());
                    results.add(new MemberResult(save, null));
                } catch (RuntimeException failure) {
                    results.add(new MemberResult(save, failure));
                }
            }
            return List.copyOf(results);
        });
        batch.worker.thenAccept(results -> {
            if (MetadataHandler.shutdownRequested) return;
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                if (bulkToggle != batch || MetadataHandler.shutdownRequested) return;
                bulkToggle = null;
                boolean failed = reportSaveFailures(results);
                boolean changed = false;
                for (MemberResult result : results) {
                    if (result.failure() != null) continue;
                    MemberIntent intent = result.save().intent();
                    intent.fields().setEnabled(intent.enabled());
                    intent.fields().getWritableFileConfiguration().set("isEnabled", intent.enabled());
                    changed = true;
                }
                if (batch.player != null && batch.player.isOnline())
                    Logger.sendMessage(batch.player, failed ? DungeonsConfig.getContentConfigurationSaveFailedMessage()
                            : batch.reloadingMessage);
                // Successful members are durable even if another file failed; reload once to expose that exact state.
                if (changed) ReloadCommand.reload(batch.player != null && batch.player.isOnline()
                        ? batch.player : Bukkit.getConsoleSender());
            });
        });
    }

    private static boolean reportSaveFailures(List<MemberResult> results) {
        boolean failed = false;
        for (MemberResult result : results) {
            if (result.failure() == null) continue;
            failed = true;
            Logger.warn("Content member was not changed: " + result.save().file() + ": " + result.failure());
        }
        if (failed) Logger.warn("Content toggle partially failed; successful members remain saved. Failed files are listed above.");
        return failed;
    }

    /**
     * Returns filenames of packages that are children of MetaPackages.
     * Useful for filtering out children during bulk operations (download, update).
     */
    public static Set<String> getMetaPackageChildFilenames() {
        Set<String> childFilenames = new HashSet<>();
        for (EMPackage pkg : emPackages.values()) {
            List<String> contained = pkg.getContentPackagesConfigFields().getContainedPackages();
            if (contained != null) childFilenames.addAll(contained);
        }
        return childFilenames;
    }

    public static void initialize(ContentPackagesConfigFields contentPackagesConfigFields) {
        if (contentPackagesConfigFields.getContentType() != null) {
            switch (contentPackagesConfigFields.getContentType()) {
                case INSTANCED_DUNGEON:
                    new WorldInstancedDungeonPackage(contentPackagesConfigFields);
                    break;
                case DYNAMIC_DUNGEON:
                    new DynamicDungeonPackage(contentPackagesConfigFields);
                    break;
                case OPEN_DUNGEON:
                    new WorldDungeonPackage(contentPackagesConfigFields);
                    break;
                case HUB:
                    new WorldPackage(contentPackagesConfigFields);
                    break;
                case SCHEMATIC_DUNGEON:
                    Logger.warn("Tried to load schematic dungeon " + contentPackagesConfigFields.getFilename() + "! This will not work because schematic dungeons have been removed as of EliteMobs 9.0 and replaced with world dungeons. If you want the schematic dungeon experience, I recommend you use BetterStructures with the elite shrines packages, which work better than schematics ever could. Fix this by deleting it from the dungeonpackager file.");
                    break;
                case META_PACKAGE:
                    new MetaPackage(contentPackagesConfigFields);
                    break;
                case ITEMS_PACKAGE:
                    new ItemsPackage(contentPackagesConfigFields);
                    break;
                case EVENTS_PACKAGE:
                    new EventsPackage(contentPackagesConfigFields);
                    break;
                case MODELS_PACKAGE:
                    new ModelsPackage(contentPackagesConfigFields);
                    break;
            }
        }
    }

    /**
     * Install/uninstall feedback that tolerates a missing player: setup actions can come from the
     * console or automation, where the message belongs in the server log instead of a chat window.
     */
    protected static void notify(Player player, String message) {
        if (player != null) player.sendMessage(message);
        else com.magmaguy.magmacore.util.Logger.info(message);
    }

    public void setupMenuToggle(Player player) {
        // Dispatch on freshly derived state, mirroring ContentPackage#onClick, so the
        // command path and the setup menu agree. The old flag checks read isInstalled /
        // isDownloaded directly, which only ratchet true during menu rendering — a
        // package that was never rendered (or was rebuilt by the reload every toggle
        // triggers) answered from stale state.
        switch (getContentState()) {
            case INSTALLED -> doUninstall(player);
            case NOT_INSTALLED -> doInstall(player);
            case NEEDS_ACCESS, OUT_OF_DATE_NO_ACCESS -> doShowAccessInfo(player);
            default -> doDownload(player);
        }
    }

    protected ItemStack getInstalledItemStack() {
        return generateItemStackWithIcon(
                installedLore(),
                Material.GREEN_STAINED_GLASS_PANE,
                NightbreakSetupIcons.MODEL_CHECKMARK);
    }

    protected ItemStack getNotInstalledItemStack() {
        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentNotInstalledLine1(), DungeonsConfig.getContentNotInstalledLine2()),
                Material.YELLOW_STAINED_GLASS_PANE,
                NightbreakSetupIcons.MODEL_GRAY_X);
    }

    protected ItemStack getPartiallyInstalledItemStack() {
        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentPartialLine1(),
                        DungeonsConfig.getContentPartialLine2(),
                        DungeonsConfig.getContentPartialLine3(),
                        DungeonsConfig.getContentPartialLine4()),
                Material.ORANGE_STAINED_GLASS_PANE,
                NightbreakSetupIcons.MODEL_GRAY_X);
    }

    protected ItemStack getNotDownloadedItemStack() {
        // Determine icon based on account token and access
        String modelId;
        String slug = contentPackagesConfigFields.getNightbreakSlug();

        if (slug == null || slug.isEmpty()) {
            // No Nightbreak integration - show unlocked (can download manually)
            modelId = NightbreakSetupIcons.MODEL_UNLOCKED;
        } else if (!NightbreakAccount.hasToken()) {
            // Has Nightbreak slug but no token - show locked unlinked
            modelId = NightbreakSetupIcons.MODEL_LOCKED_UNLINKED;
        } else if (cachedAccessInfo != null && cachedAccessInfo.hasAccess) {
            // Has token and access - show unlocked (can download)
            modelId = NightbreakSetupIcons.MODEL_UNLOCKED;
        } else {
            // Has token but no access - show locked unpaid
            modelId = NightbreakSetupIcons.MODEL_LOCKED_UNPAID;
        }

        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentNotDownloadedLine1(), DungeonsConfig.getContentNotDownloadedLine2()),
                Material.YELLOW_STAINED_GLASS_PANE,
                modelId);
    }

    protected ItemStack getNeedsAccessItemStack() {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(DungeonsConfig.getContentNeedAccessMessage());
        tooltip.add(DungeonsConfig.getContentAccessClickMessage());
        if (cachedAccessInfo != null) {
            if (cachedAccessInfo.patreonLink != null && !cachedAccessInfo.patreonLink.isEmpty()) {
                tooltip.add(DungeonsConfig.getContentAvailablePatreon());
            }
            if (cachedAccessInfo.itchLink != null && !cachedAccessInfo.itchLink.isEmpty()) {
                tooltip.add(DungeonsConfig.getContentAvailableItch());
            }
        }
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        ItemStack itemStack = ItemStackGenerator.generateItemStack(
                Material.PURPLE_STAINED_GLASS_PANE,
                contentPackagesConfigFields.getName(),
                tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (ItemSettingsConfig.isHideEnchants())
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        NightbreakSetupIcons.applyItemModel(itemStack, NightbreakSetupIcons.MODEL_LOCKED_UNPAID);
        return itemStack;
    }

    @Override
    protected ItemStack getOutOfDateUpdatableItemStack() {
        // Determine icon based on account token status
        String modelId;
        String slug = contentPackagesConfigFields.getNightbreakSlug();

        if (slug == null || slug.isEmpty()) {
            // No Nightbreak integration - show update (can update manually)
            modelId = NightbreakSetupIcons.MODEL_UPDATE;
        } else if (!NightbreakAccount.hasToken()) {
            // Has Nightbreak slug but no token - show update unlinked
            modelId = NightbreakSetupIcons.MODEL_UPDATE_UNLINKED;
        } else {
            // Has token - show update (can auto-update)
            modelId = NightbreakSetupIcons.MODEL_UPDATE;
        }

        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentUpdateAvailable(), DungeonsConfig.getContentUpdateClickMessage()),
                Material.YELLOW_STAINED_GLASS_PANE,
                modelId);
    }

    @Override
    protected ItemStack getOutOfDateNoAccessItemStack() {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(DungeonsConfig.getContentUpdateAvailable());
        tooltip.add(DungeonsConfig.getContentUpdateNeedAccess());
        tooltip.add(DungeonsConfig.getContentUpdateAccessClick());
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        ItemStack itemStack = ItemStackGenerator.generateItemStack(
                Material.ORANGE_STAINED_GLASS_PANE,
                contentPackagesConfigFields.getName(),
                tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (ItemSettingsConfig.isHideEnchants())
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        NightbreakSetupIcons.applyItemModel(itemStack, NightbreakSetupIcons.MODEL_UPDATE_UNPAID);
        return itemStack;
    }

    private ItemStack generateItemStack(List<String> specificTooltip, Material material) {
        List<String> tooltip = new ArrayList<>(specificTooltip);
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        ItemStack itemStack = ItemStackGenerator.generateItemStack(material, contentPackagesConfigFields.getName(), tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (ItemSettingsConfig.isHideEnchants())
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    protected ItemStack generateItemStackWithIcon(List<String> specificTooltip, Material material, String modelId) {
        List<String> tooltip = new ArrayList<>(specificTooltip);
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        // Use the actual material - resource pack shows custom icon, fallback shows colored glass pane
        ItemStack itemStack = ItemStackGenerator.generateItemStack(
                material,
                contentPackagesConfigFields.getName(),
                tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (ItemSettingsConfig.isHideEnchants())
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        NightbreakSetupIcons.applyItemModel(itemStack, modelId);
        return itemStack;
    }

    private List<String> installedLore() {
        return List.of(
                normalizedInstalledLoreLine(DungeonsConfig.getContentInstalledLine1(),
                        "Content is installed!",
                        "Content is installed and up to date.",
                        "&a"),
                normalizedInstalledLoreLine(DungeonsConfig.getContentInstalledLine2(),
                        "Click to uninstall!",
                        "Click to uninstall.",
                        "&7"));
    }

    private static String normalizedInstalledLoreLine(String configuredLine,
                                                      String oldDefault,
                                                      String newDefault,
                                                      String fallbackColor) {
        String line = configuredLine == null || configuredLine.isBlank() ? newDefault : configuredLine;
        if (line.trim().equalsIgnoreCase(oldDefault)) line = newDefault;
        return hasFormatting(line) ? line : fallbackColor + line;
    }

    private static boolean hasFormatting(String line) {
        return line.contains("&") || line.contains("§") || line.contains("<");
    }

    public abstract void doInstall(Player player);

    public abstract void doUninstall(Player player);

    public void doDownload(Player player) {
        String slug = contentPackagesConfigFields.getNightbreakSlug();

        // If no Nightbreak slug, use legacy download link behavior
        if (slug == null || slug.isEmpty()) {
            String downloadLink = getContentPackagesConfigFields().getDownloadLink();
            if (downloadLink == null || downloadLink.isEmpty()) {
                Logger.sendSimpleMessage(player, "&cNo download link available for this content pack.");
                return;
            }
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(DungeonsConfig.getContentDownloadLegacyMessage().replace("$link", "")),
                    SpigotMessage.hoverLinkMessage(
                            "&9&n" + downloadLink,
                            "&7Click to download",
                            downloadLink));
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            return;
        }

        // If no account token is registered, prompt user
        if (!NightbreakAccount.hasToken()) {
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentNightbreakPromptLine1());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakPromptLine2()),
                    SpigotMessage.hoverLinkMessage(
                            InitializeConfig.getAccountLinkDisplay(),
                            InitializeConfig.getAccountLinkHover(),
                            "https://nightbreak.io/account/"));
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentNightbreakPromptLine3());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakPromptLine4()),
                    SpigotMessage.hoverLinkMessage(
                            InitializeConfig.getContentLinkDisplay(),
                            InitializeConfig.getContentLinkHover(),
                            getContentPageUrl()));
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            return;
        }

        // Check access first
        player.sendMessage(DungeonsConfig.getContentCheckingAccessMessage().replace("$name", contentPackagesConfigFields.getName()));

        NightbreakContentManager.checkAccessAsync(MetadataHandler.PLUGIN, slug, accessInfo -> {
            cachedAccessInfo = accessInfo;

            if (!player.isOnline()) return;

            if (accessInfo == null) {
                if (NightbreakAccount.hasAuthFailure()) {
                    sendInvalidTokenMessage(player);
                    return;
                }
                player.sendMessage(DungeonsConfig.getContentAccessFailedMessage());
                player.sendMessage(DungeonsConfig.getContentAccessFailedLink());
                return;
            }

            if (!accessInfo.hasAccess) {
                doShowAccessInfo(player);
                return;
            }

            // Has access - proceed with download
            File importsFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "imports");
            if (!importsFolder.exists()) {
                importsFolder.mkdirs();
            }

            NightbreakContentManager.downloadAsync(MetadataHandler.PLUGIN, slug, importsFolder, player, success -> {
                if (success && player.isOnline()) {
                    player.sendMessage(DungeonsConfig.getContentDownloadedReloadMessage());
                    // Schedule reload after a short delay
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                        com.magmaguy.elitemobs.commands.ReloadCommand.reload(player);
                    }, 20L);
                }
            });
        });
    }

    public void doShowAccessInfo(Player player) {
        Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
        Logger.sendSimpleMessage(player, DungeonsConfig.getContentNoAccessMessage().replace("$name", contentPackagesConfigFields.getName()));
        Logger.sendSimpleMessage(player, "");
        Logger.sendSimpleMessage(player, DungeonsConfig.getContentGetAccessMessage());
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakLink()),
                SpigotMessage.hoverLinkMessage(
                        InitializeConfig.getContentLinkDisplay(),
                        InitializeConfig.getContentLinkHover(),
                        getContentPageUrl()));
        if (cachedAccessInfo != null) {
            if (cachedAccessInfo.patreonLink != null && !cachedAccessInfo.patreonLink.isEmpty()) {
                player.spigot().sendMessage(
                        SpigotMessage.simpleMessage(DungeonsConfig.getContentPatreonLink()),
                        SpigotMessage.hoverLinkMessage(
                                "&9&n" + cachedAccessInfo.patreonLink,
                                "&7Click to open Patreon",
                                cachedAccessInfo.patreonLink));
            }
            if (cachedAccessInfo.itchLink != null && !cachedAccessInfo.itchLink.isEmpty()) {
                player.spigot().sendMessage(
                        SpigotMessage.simpleMessage(DungeonsConfig.getContentItchLink()),
                        SpigotMessage.hoverLinkMessage(
                                "&9&n" + cachedAccessInfo.itchLink,
                                "&7Click to open itch.io",
                                cachedAccessInfo.itchLink));
            }
        }
        Logger.sendSimpleMessage(player, "");
        Logger.sendSimpleMessage(player, DungeonsConfig.getContentLinkAccountMessage());
        Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
    }

    private void sendInvalidTokenMessage(Player player) {
        player.sendMessage(DungeonsConfig.getContentInvalidTokenMessage());
        player.spigot().sendMessage(
                SpigotMessage.simpleMessage(DungeonsConfig.getContentInvalidTokenInstructions()),
                SpigotMessage.hoverLinkMessage(
                        InitializeConfig.getAccountLinkDisplay(),
                        InitializeConfig.getAccountLinkHover(),
                        "https://nightbreak.io/account/"));
    }

    protected ContentState getContentState() {
        // Check for out-of-date installed content first
        if (isInstalled && outOfDate) {
            String slug = contentPackagesConfigFields.getNightbreakSlug();
            if (slug != null && !slug.isEmpty() && NightbreakAccount.hasToken()) {
                // If we have cached access info showing access, can update
                if (cachedAccessInfo != null && cachedAccessInfo.hasAccess) {
                    return ContentState.OUT_OF_DATE_UPDATABLE;
                }
                // If we have cached access info showing no access, can't update
                if (cachedAccessInfo != null && !cachedAccessInfo.hasAccess) {
                    return ContentState.OUT_OF_DATE_NO_ACCESS;
                }
                // No cached info yet - default to updatable (will check on click)
                return ContentState.OUT_OF_DATE_UPDATABLE;
            }
            // No slug or no token - show as updatable (manual download)
            return ContentState.OUT_OF_DATE_UPDATABLE;
        }

        if (isInstalled) return ContentState.INSTALLED;
        if (isDownloaded) return ContentState.NOT_INSTALLED;

        // Check if this content requires Nightbreak access
        String slug = contentPackagesConfigFields.getNightbreakSlug();
        if (slug != null && !slug.isEmpty() && NightbreakAccount.hasToken()) {
            // If we have cached access info showing no access, show NEEDS_ACCESS
            if (cachedAccessInfo != null && !cachedAccessInfo.hasAccess) {
                return ContentState.NEEDS_ACCESS;
            }
        }

        return ContentState.NOT_DOWNLOADED;
    }

    public boolean isOutOfDate() {
        if (!isInstalled) return false;
        return outOfDate;
    }

    @Override
    public String getNightbreakSlug() {
        return contentPackagesConfigFields.getNightbreakSlug();
    }

    @Override
    public String getDisplayName() {
        return contentPackagesConfigFields.getName();
    }

    @Override
    public String getDownloadLink() {
        return contentPackagesConfigFields.getDownloadLink();
    }

    public String getContentPageUrl() {
        String downloadLink = contentPackagesConfigFields.getDownloadLink();
        if (downloadLink != null && !downloadLink.isBlank()) return downloadLink;
        String slug = contentPackagesConfigFields.getNightbreakSlug();
        if (slug != null && !slug.isBlank()) return ELITEMOBS_CONTENT_PAGE + "#" + slug;
        return ELITEMOBS_CONTENT_PAGE;
    }

    @Override
    public int getLocalVersion() {
        return contentPackagesConfigFields.getDungeonVersion();
    }

    /**
     * Forces re-derivation of isDownloaded/isInstalled state from children (for MetaPackages)
     * or from the current content state. Useful when commands need accurate state outside the menu.
     */
    public void refreshState() {
        getContentState();
    }

    /**
     * Very first initialization - checks if content is downloaded / installed, loads worlds
     */
    public abstract void baseInitialization();

    /**
     * Initializes content - this means it associates bosses, treasure chests and NPCs to the dungeon (if not instanced)
     */
    public abstract void initializeContent();

}
