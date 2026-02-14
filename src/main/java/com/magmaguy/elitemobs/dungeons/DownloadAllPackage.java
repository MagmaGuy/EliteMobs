package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.menus.SetupMenuIcons;
import com.magmaguy.magmacore.menus.ContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A special ContentPackage that acts as a "Download All" button in the setup menu.
 * When clicked, it closes the menu and runs /em downloadall.
 */
public class DownloadAllPackage extends ContentPackage {

    private final ItemStack displayIcon;

    public DownloadAllPackage(List<EMPackage> allPackages) {
        super();
        this.displayIcon = buildIcon(allPackages);
    }

    private static ItemStack buildIcon(List<EMPackage> allPackages) {
        String iconModel;
        Material baseMaterial;
        String displayName;
        List<String> lore;

        if (!NightbreakAccount.hasToken()) {
            iconModel = SetupMenuIcons.MODEL_RED_CROSS;
            baseMaterial = Material.RED_STAINED_GLASS_PANE;
            displayName = "&cDownload All";
            lore = List.of("&7No Nightbreak token linked.", "&7Use &e/nightbreaklogin &7first.");
        } else {
            boolean hasAnyNotDownloaded = allPackages.stream().anyMatch(pkg -> {
                String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
                if (slug == null || slug.isEmpty()) return false;
                return !pkg.isDownloaded()
                        && pkg.getCachedAccessInfo() != null
                        && pkg.getCachedAccessInfo().hasAccess;
            });

            boolean hasAnyOutdated = allPackages.stream().anyMatch(EMPackage::isOutOfDate);

            if (hasAnyNotDownloaded) {
                iconModel = SetupMenuIcons.MODEL_CROWN_YELLOW;
                baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
                displayName = "&eDownload All";
                lore = List.of("&7Click to download all available content.");
            } else if (hasAnyOutdated) {
                iconModel = SetupMenuIcons.MODEL_CROWN_YELLOW;
                baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
                displayName = "&eUpdate All";
                lore = List.of("&7Click to update all outdated content.");
            } else {
                iconModel = SetupMenuIcons.MODEL_CHECKMARK;
                baseMaterial = Material.LIME_STAINED_GLASS_PANE;
                displayName = "&aAll Content Installed";
                lore = List.of("&7All available content is downloaded", "&7and up to date!");
            }
        }

        ItemStack icon = ItemStackGenerator.generateItemStack(baseMaterial, displayName, lore);
        SetupMenuIcons.applyItemModel(icon, iconModel);
        return icon;
    }

    @Override
    public ItemStack getItemstack() {
        return displayIcon;
    }

    @Override
    public void onClick(Player player) {
        player.closeInventory();
        Bukkit.dispatchCommand(player, "elitemobs downloadall");
    }

    // Abstract method stubs — not used since onClick is overridden directly
    @Override protected ItemStack getInstalledItemStack() { return displayIcon; }
    @Override protected ItemStack getPartiallyInstalledItemStack() { return displayIcon; }
    @Override protected ItemStack getNotInstalledItemStack() { return displayIcon; }
    @Override protected ItemStack getNotDownloadedItemStack() { return displayIcon; }
    @Override protected ItemStack getNeedsAccessItemStack() { return displayIcon; }
    @Override protected ItemStack getOutOfDateUpdatableItemStack() { return displayIcon; }
    @Override protected ItemStack getOutOfDateNoAccessItemStack() { return displayIcon; }
    @Override protected void doInstall(Player player) {}
    @Override protected void doUninstall(Player player) {}
    @Override protected void doDownload(Player player) {}
    @Override protected void doShowAccessInfo(Player player) {}
    @Override protected ContentState getContentState() { return ContentState.INSTALLED; }
}
