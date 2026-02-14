package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.menus.SetupMenuIcons;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.magmacore.menus.ContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.nightbreak.NightbreakContentManager;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EMPackage extends ContentPackage {

    protected static final HashMap<String, EMPackage> content = new HashMap<>();
    @Getter
    private static final Map<String, EMPackage> emPackages = new HashMap<>();
    @Getter
    protected final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    @Setter
    protected boolean isDownloaded;
    @Getter
    @Setter
    protected boolean isInstalled;
    @Setter
    protected boolean outOfDate = false;
    @Getter
    @Setter
    protected NightbreakAccount.AccessInfo cachedAccessInfo = null;
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
        content.clear();
        emPackages.clear();
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

    public void setupMenuToggle(Player player) {
        if (isInstalled) {
            doUninstall(player);
            return;
        }
        if (isDownloaded) {
            doInstall(player);
            return;
        }
        doDownload(player);
    }

    protected ItemStack getInstalledItemStack() {
        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentInstalledLine1(), DungeonsConfig.getContentInstalledLine2()),
                Material.GREEN_STAINED_GLASS_PANE,
                SetupMenuIcons.MODEL_CHECKMARK);
    }

    protected ItemStack getNotInstalledItemStack() {
        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentNotInstalledLine1(), DungeonsConfig.getContentNotInstalledLine2()),
                Material.YELLOW_STAINED_GLASS_PANE,
                SetupMenuIcons.MODEL_GRAY_X);
    }

    protected ItemStack getPartiallyInstalledItemStack() {
        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getContentPartialLine1(),
                        DungeonsConfig.getContentPartialLine2(),
                        DungeonsConfig.getContentPartialLine3(),
                        DungeonsConfig.getContentPartialLine4()),
                Material.ORANGE_STAINED_GLASS_PANE,
                SetupMenuIcons.MODEL_GRAY_X);
    }

    protected ItemStack getNotDownloadedItemStack() {
        // Determine icon based on Nightbreak token and access
        String modelId;
        String slug = contentPackagesConfigFields.getNightbreakSlug();

        if (slug == null || slug.isEmpty()) {
            // No Nightbreak integration - show unlocked (can download manually)
            modelId = SetupMenuIcons.MODEL_UNLOCKED;
        } else if (!NightbreakAccount.hasToken()) {
            // Has Nightbreak slug but no token - show locked unlinked
            modelId = SetupMenuIcons.MODEL_LOCKED_UNLINKED;
        } else if (cachedAccessInfo != null && cachedAccessInfo.hasAccess) {
            // Has token and access - show unlocked (can download)
            modelId = SetupMenuIcons.MODEL_UNLOCKED;
        } else {
            // Has token but no access - show locked unpaid
            modelId = SetupMenuIcons.MODEL_LOCKED_UNPAID;
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
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        SetupMenuIcons.applyItemModel(itemStack, SetupMenuIcons.MODEL_LOCKED_UNPAID);
        return itemStack;
    }

    @Override
    protected ItemStack getOutOfDateUpdatableItemStack() {
        // Determine icon based on Nightbreak token status
        String modelId;
        String slug = contentPackagesConfigFields.getNightbreakSlug();

        if (slug == null || slug.isEmpty()) {
            // No Nightbreak integration - show update (can update manually)
            modelId = SetupMenuIcons.MODEL_UPDATE;
        } else if (!NightbreakAccount.hasToken()) {
            // Has Nightbreak slug but no token - show update unlinked
            modelId = SetupMenuIcons.MODEL_UPDATE_UNLINKED;
        } else {
            // Has token - show update (can auto-update)
            modelId = SetupMenuIcons.MODEL_UPDATE;
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
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        SetupMenuIcons.applyItemModel(itemStack, SetupMenuIcons.MODEL_UPDATE_UNPAID);
        return itemStack;
    }

    private ItemStack generateItemStack(List<String> specificTooltip, Material material) {
        List<String> tooltip = new ArrayList<>(specificTooltip);
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        ItemStack itemStack = ItemStackGenerator.generateItemStack(material, contentPackagesConfigFields.getName(), tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack generateItemStackWithIcon(List<String> specificTooltip, Material material, String modelId) {
        List<String> tooltip = new ArrayList<>(specificTooltip);
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        // Use the actual material - resource pack shows custom icon, fallback shows colored glass pane
        ItemStack itemStack = ItemStackGenerator.generateItemStack(
                material,
                contentPackagesConfigFields.getName(),
                tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        SetupMenuIcons.applyItemModel(itemStack, modelId);
        return itemStack;
    }

    public abstract void doInstall(Player player);

    public abstract void doUninstall(Player player);

    public void doDownload(Player player) {
        String slug = contentPackagesConfigFields.getNightbreakSlug();

        // If no Nightbreak slug, use legacy download link behavior
        if (slug == null || slug.isEmpty()) {
            player.sendMessage(DungeonsConfig.getContentDownloadSeparator());
            player.sendMessage(DungeonsConfig.getContentDownloadLegacyMessage().replace("$link", getContentPackagesConfigFields().getDownloadLink()));
            player.sendMessage(DungeonsConfig.getContentDownloadSeparator());
            return;
        }

        // If no Nightbreak token registered, prompt user
        if (!NightbreakAccount.hasToken()) {
            player.sendMessage(DungeonsConfig.getContentDownloadSeparator());
            player.sendMessage(DungeonsConfig.getContentNightbreakPromptLine1());
            player.sendMessage(DungeonsConfig.getContentNightbreakPromptLine2());
            player.sendMessage(DungeonsConfig.getContentNightbreakPromptLine3());
            player.sendMessage("");
            player.sendMessage(DungeonsConfig.getContentNightbreakPromptLine4());
            player.sendMessage(DungeonsConfig.getContentDownloadSeparator());
            return;
        }

        // Check access first
        player.sendMessage(DungeonsConfig.getContentCheckingAccessMessage().replace("$name", contentPackagesConfigFields.getName()));

        NightbreakContentManager.checkAccessAsync(slug, accessInfo -> {
            cachedAccessInfo = accessInfo;

            if (!player.isOnline()) return;

            if (accessInfo == null) {
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

            NightbreakContentManager.downloadAsync(slug, importsFolder, player, success -> {
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
        player.sendMessage(DungeonsConfig.getContentDownloadSeparator());
        player.sendMessage(DungeonsConfig.getContentNoAccessMessage().replace("$name", contentPackagesConfigFields.getName()));
        player.sendMessage("");
        player.sendMessage(DungeonsConfig.getContentGetAccessMessage());
        player.sendMessage(DungeonsConfig.getContentNightbreakLink());
        if (cachedAccessInfo != null) {
            if (cachedAccessInfo.patreonLink != null && !cachedAccessInfo.patreonLink.isEmpty()) {
                player.sendMessage(DungeonsConfig.getContentPatreonLink().replace("$link", cachedAccessInfo.patreonLink));
            }
            if (cachedAccessInfo.itchLink != null && !cachedAccessInfo.itchLink.isEmpty()) {
                player.sendMessage(DungeonsConfig.getContentItchLink().replace("$link", cachedAccessInfo.itchLink));
            }
        }
        player.sendMessage("");
        player.sendMessage(DungeonsConfig.getContentLinkAccountMessage());
        player.sendMessage(DungeonsConfig.getContentDownloadSeparator());
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

    /**
     * Very first initialization - checks if content is downloaded / installed, loads worlds
     */
    public abstract void baseInitialization();

    /**
     * Initializes content - this means it associates bosses, treasure chests and NPCs to the dungeon (if not instanced)
     */
    public abstract void initializeContent();

}
