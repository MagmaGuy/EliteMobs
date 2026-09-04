package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.magmacore.menus.NightbreakSetupIcons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventsPackage extends EMPackage {
    private final List<CustomEventsConfigFields> customEvents = new ArrayList<>();
    private boolean allFilesDownloaded = true;


    public EventsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
        for (String customEventName : contentPackagesConfigFields.getCustomEventFilenames()) {
            if (CustomEventsConfig.getCustomEvent(customEventName) != null)
                customEvents.add(CustomEventsConfig.getCustomEvent(customEventName));
            else {
//                Logger.warn("Could not find file " + customEventName); todo: might want to reenable this with an alternative for finding out if something was installed
                allFilesDownloaded = false;
            }
        }
    }

    @Override
    protected ContentState getContentState() {
        boolean allInstalled = true;
        boolean someInstalled = false;

        if (customEvents.isEmpty()) return ContentState.NOT_DOWNLOADED;

        for (CustomEventsConfigFields customEventsConfigFields : customEvents) {
            if (!customEventsConfigFields.isEnabled()) allInstalled = false;
            if (customEventsConfigFields.isEnabled()) someInstalled = true;
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
                ? DungeonsConfig.getEventsInstallingMessage().replace("$count", String.valueOf(customEvents.size()))
                : DungeonsConfig.getEventsUninstallingMessage().replace("$count", String.valueOf(customEvents.size()));
        notify(player, actionMessage);

        List<CompletableFuture<Void>> futures = customEvents.stream()
                .map(customItem -> customItem.setEnabledAndSave(enable))
                .toList();

        notify(player, DungeonsConfig.getEventsSavingMessage().replace("$count", String.valueOf(customEvents.size())));

        submitBulkMemberSaves(player, futures, DungeonsConfig.getEventsReloadingMessage(), "event");
    }

    /**
     * Bundled content: partial means members were disabled, not that files are
     * missing. See {@link ItemsPackage#getPartiallyInstalledItemStack}.
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
     * Events packages ship inside the plugin jar — there is nothing to
     * download. See {@link ItemsPackage#doDownload} for why partial-state
     * clicks land here and why completing the install is the right repair.
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
