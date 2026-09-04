package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.magmacore.menus.NightbreakSetupIcons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemsPackage extends EMPackage {
    private final List<CustomItemsConfigFields> customItems = new ArrayList<>();
    private boolean allFilesDownloaded = true;

    public ItemsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
        for (String customItemFilename : contentPackagesConfigFields.getCustomItemFilenames()) {
            if (CustomItemsConfig.getCustomItems().containsKey(customItemFilename)) {
                customItems.add(CustomItemsConfig.getCustomItems().get(customItemFilename));
            } else allFilesDownloaded = false;
        }
    }


    @Override
    protected ContentState getContentState() {
        boolean allInstalled = true;
        boolean someInstalled = false;

        if (customItems.isEmpty()) return ContentState.NOT_DOWNLOADED;

        for (CustomItemsConfigFields customItem : customItems) {
            if (!customItem.isEnabled()) allInstalled = false;
            if (customItem.isEnabled()) someInstalled = true;
        }

        if (allInstalled) {
            isInstalled = true;
            return ContentState.INSTALLED;
        }
        if (someInstalled) {
            isInstalled = true;
            return ContentState.PARTIALLY_INSTALLED;
        }
        if (allFilesDownloaded) {
            isDownloaded = true;
            return ContentState.NOT_INSTALLED;
        }
        return ContentState.NOT_DOWNLOADED;
    }

    private void handleInstallation(Player player, boolean enable) {
        if (bulkMemberTogglesLocked()) {
            notify(player, DungeonsConfig.getContentToggleInProgressMessage());
            return;
        }
        String actionMessage = enable
                ? DungeonsConfig.getItemsInstallingMessage().replace("$count", String.valueOf(customItems.size()))
                : DungeonsConfig.getItemsUninstallingMessage().replace("$count", String.valueOf(customItems.size()));
        notify(player, actionMessage);

        List<CompletableFuture<Void>> futures = customItems.stream()
                .map(customItem -> customItem.setEnabledAndSave(enable))
                .toList();

        notify(player, DungeonsConfig.getItemsSavingMessage().replace("$count", String.valueOf(customItems.size())));

        submitBulkMemberSaves(player, futures, DungeonsConfig.getItemsReloadingMessage(), "item");
    }

    /**
     * Bundled content: partial means members were disabled, not that files are
     * missing — the tooltip must promise enabling, matching what {@link #doDownload}
     * (the action a partial-state click runs) actually does.
     */
    @Override
    protected ItemStack getPartiallyInstalledItemStack() {
        return generateItemStackWithIcon(
                List.of(DungeonsConfig.getBundledContentPartialLine1(),
                        DungeonsConfig.getBundledContentPartialLine2(),
                        DungeonsConfig.getBundledContentPartialLine3()),
                Material.ORANGE_STAINED_GLASS_PANE,
                NightbreakSetupIcons.MODEL_GRAY_X);
    }

    @Override
    public void doInstall(Player player) {
        handleInstallation(player, true);
    }

    @Override
    public void doUninstall(Player player) {
        handleInstallation(player, false);
    }

    /**
     * Items packages ship inside the plugin jar — there is nothing to download.
     * The setup menu routes PARTIALLY_INSTALLED clicks (and the defensive
     * NOT_DOWNLOADED fallback) to doDownload; for premade items the only
     * sensible repair is to finish enabling every member. Without this, a
     * partially installed Default EliteMobs Custom Items package pointed
     * operators at a Discord download link for content they already have,
     * with no path in the menu to actually re-enable it.
     */
    @Override
    public void doDownload(Player player) {
        handleInstallation(player, true);
    }

    @Override
    public void baseInitialization() {

    }

    @Override
    public void initializeContent() {

    }
}
