package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        player.closeInventory();
        String action = enable ? "Installing" : "Uninstalling";
        Logger.sendMessage(player, action + " " + customEvents.size() + " events...");

        List<CompletableFuture<Void>> futures = customEvents.stream()
                .map(customItem -> customItem.setEnabledAndSave(enable))
                .toList();

        Logger.sendMessage(player, "Saving " + customEvents.size() + " event files...");

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.thenRun(() -> {
            Logger.sendMessage(player, "Reloading EliteMobs to apply events changes!");
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> ReloadCommand.reload(player));
        }).join(); // This ensures the current thread waits until all futures are complete
    }

    @Override
    public void doInstall(Player player) {
        handleInstallation(player, true);
    }

    @Override
    public void doUninstall(Player player) {
        handleInstallation(player, false);
    }

    @Override
    public void baseInitialization() {

    }

    @Override
    public void initializeContent() {

    }
}
