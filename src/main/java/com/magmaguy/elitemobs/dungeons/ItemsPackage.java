package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.magmacore.util.Logger;
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
        String actionMessage = enable
                ? DungeonsConfig.getItemsInstallingMessage().replace("$count", String.valueOf(customItems.size()))
                : DungeonsConfig.getItemsUninstallingMessage().replace("$count", String.valueOf(customItems.size()));
        Logger.sendMessage(player, actionMessage);

        List<CompletableFuture<Void>> futures = customItems.stream()
                .map(customItem -> customItem.setEnabledAndSave(enable))
                .toList();

        Logger.sendMessage(player, DungeonsConfig.getItemsSavingMessage().replace("$count", String.valueOf(customItems.size())));

        reloadAfterConfigurationSaves(
                player,
                futures,
                DungeonsConfig.getItemsReloadingMessage(),
                "item");
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
