package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

public class ModelsPackage extends EMPackage {

    public ModelsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
    }

    @Override
    public void doInstall(Player player) {
        Logger.sendMessage(player, "Models are installed automatically, you can't do this manually!");
    }

    @Override
    public void doUninstall(Player player) {
        Logger.sendMessage(player, "Models can't currently be uninstalled via this menu! You'll have to manually delete the files from your server.");
    }

    @Override
    public void baseInitialization() {

    }

    @Override
    protected ContentState getContentState() {
        boolean allInstalled = true;
        boolean someInstalled = false;

        for (String customModel : contentPackagesConfigFields.getModelNames()) {
            boolean exists = CustomModel.modelExists(customModel);
            if (exists) someInstalled = true;
            if (!exists) allInstalled = false;
        }

        if (!someInstalled) {
            isInstalled = false;
            isDownloaded = false;
            return ContentState.NOT_DOWNLOADED;
        }
        if (allInstalled) {
            isInstalled = true;
            isDownloaded = true;
            return ContentState.INSTALLED;
        }

        isDownloaded = true;
        return ContentState.PARTIALLY_INSTALLED;
    }

    @Override
    public void initializeContent() {

    }
}
