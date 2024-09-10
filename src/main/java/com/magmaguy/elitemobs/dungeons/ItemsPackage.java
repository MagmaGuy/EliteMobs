package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        player.closeInventory();
        String action = enable ? "Installing" : "Uninstalling";
        Logger.sendMessage(player, action + " " + customItems.size() + " items...");

        List<CompletableFuture<Void>> futures = customItems.stream()
                .map(customItem -> customItem.setEnabledAndSave(enable))
                .toList();

        Logger.sendMessage(player, "Saving " + customItems.size() + " item files...");

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.thenRun(() -> {
            Logger.sendMessage(player, "Reloading EliteMobs to apply item changes!");
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
