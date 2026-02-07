package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MetaPackage extends EMPackage {

    public MetaPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
    }

    public List<EMPackage> getPackages() {
        List<EMPackage> packages = new ArrayList<>();
        for (String packageFilename : contentPackagesConfigFields.getContainedPackages()) {
            EMPackage emPackage = EMPackage.getEmPackages().get(packageFilename);
            if (emPackage != null) packages.add(emPackage);
        }
        return packages;
    }

    @Override
    protected ContentState getContentState() {
        boolean allInstalled = true;
        boolean someInstalled = false;
        boolean allDownloaded = true;
        for (EMPackage aPackage : getPackages()) {
            if (!aPackage.isInstalled()) allInstalled = false;
            if (aPackage.isInstalled()) someInstalled = true;
            if (!aPackage.isDownloaded()) allDownloaded = false;
        }
        if (allInstalled) {
            isInstalled = true;
            return ContentState.INSTALLED;
        }
        if (someInstalled) {
            isInstalled = true;
            return ContentState.PARTIALLY_INSTALLED;
        }
        if (allDownloaded) {
            isDownloaded = true;
            return ContentState.NOT_INSTALLED;
        }
        return ContentState.NOT_DOWNLOADED;
    }

    @Override
    public void doInstall(Player player) {
        getPackages().forEach(emPackage -> emPackage.doInstall(player));
        super.isInstalled = true;
    }

    @Override
    public void doUninstall(Player player) {
        getPackages().forEach(emPackage -> emPackage.doUninstall(player));
        super.isInstalled = false;
    }

    @Override
    public void baseInitialization() {
        //Implemented by the packages themselves
    }

    @Override
    public void initializeContent() {
        //Implemented by the packages themselves
    }
}
