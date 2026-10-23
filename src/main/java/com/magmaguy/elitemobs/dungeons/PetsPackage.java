package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.magmacore.menus.NightbreakSetupIcons;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A pack of pet species: the custom boss files listed in {@code petFilenames}, each with a {@code pet:} section,
 * and the models they use. Installing enables those files, uninstalling disables them; the setup menu reload
 * picks the change up like an events package.
 */
public class PetsPackage extends EMPackage {
    private final List<CustomBossesConfigFields> species = new ArrayList<>();
    private boolean allFilesDownloaded = true;

    public PetsPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        super(contentPackagesConfigFields);
        for (String filename : contentPackagesConfigFields.getPetFilenames()) {
            CustomBossesConfigFields boss = CustomBossesConfig.getCustomBoss(filename);
            if (boss != null) species.add(boss);
            else allFilesDownloaded = false;
        }
    }

    @Override
    protected ContentState getContentState() {
        boolean allInstalled = true;
        boolean someInstalled = false;
        if (species.isEmpty()) return ContentState.NOT_DOWNLOADED;
        for (CustomBossesConfigFields boss : species) {
            if (!boss.isEnabled()) allInstalled = false;
            if (boss.isEnabled()) someInstalled = true;
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
                ? DungeonsConfig.getEventsInstallingMessage().replace("$count", String.valueOf(species.size()))
                : DungeonsConfig.getEventsUninstallingMessage().replace("$count", String.valueOf(species.size()));
        notify(player, actionMessage);
        notify(player, DungeonsConfig.getEventsSavingMessage().replace("$count", String.valueOf(species.size())));
        submitBulkMemberSaves(player, species, enable, DungeonsConfig.getEventsReloadingMessage());
    }

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

    @Override
    public void doDownload(Player player) {
        if (allFilesDownloaded) handleInstallation(player, true);
        else super.doDownload(player);
    }

    @Override
    public void baseInitialization() {
    }

    @Override
    public void initializeContent() {
    }
}
