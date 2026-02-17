package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.menus.SetupMenuIcons;
import com.magmaguy.magmacore.menus.ContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A special ContentPackage that acts as a "Download All" button in the setup menu.
 * Behavior adapts to the server's content state:
 * - No token: shows Nightbreak setup instructions
 * - Content not downloaded: downloads all available content
 * - Content out of date: updates all outdated content
 * - All up to date: shows success message
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
            lore = List.of(
                    "&7No Nightbreak token linked.",
                    "&7Click for setup instructions.");
        } else {
            long notDownloadedCount = countNotDownloaded(allPackages);
            long outdatedCount = countOutdated(allPackages);

            if (notDownloadedCount > 0 && outdatedCount > 0) {
                iconModel = SetupMenuIcons.MODEL_CROWN_YELLOW;
                baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
                displayName = "&eDownload & Update All";
                lore = List.of(
                        "&7Click to download &e" + notDownloadedCount + " &7new package" + (notDownloadedCount != 1 ? "s" : ""),
                        "&7and update &e" + outdatedCount + " &7outdated package" + (outdatedCount != 1 ? "s" : "") + ".");
            } else if (notDownloadedCount > 0) {
                iconModel = SetupMenuIcons.MODEL_CROWN_YELLOW;
                baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
                displayName = "&eDownload All";
                lore = List.of(
                        "&7Click to download &e" + notDownloadedCount + " &7available package" + (notDownloadedCount != 1 ? "s" : "") + ".");
            } else if (outdatedCount > 0) {
                iconModel = SetupMenuIcons.MODEL_CROWN_YELLOW;
                baseMaterial = Material.YELLOW_STAINED_GLASS_PANE;
                displayName = "&eUpdate All";
                lore = List.of(
                        "&7Click to update &e" + outdatedCount + " &7outdated package" + (outdatedCount != 1 ? "s" : "") + ".");
            } else {
                iconModel = SetupMenuIcons.MODEL_CHECKMARK;
                baseMaterial = Material.LIME_STAINED_GLASS_PANE;
                displayName = "&aAll Content Up To Date";
                lore = List.of("&7All available content is downloaded", "&7and up to date!");
            }
        }

        ItemStack icon = ItemStackGenerator.generateItemStack(baseMaterial, displayName, lore);
        SetupMenuIcons.applyItemModel(icon, iconModel);
        return icon;
    }

    /**
     * Counts unique slugs of packages that are not downloaded but the user has access to.
     */
    private static long countNotDownloaded(List<EMPackage> allPackages) {
        Set<String> seenSlugs = new HashSet<>();
        long count = 0;
        for (EMPackage pkg : allPackages) {
            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null || slug.isEmpty()) continue;
            if (seenSlugs.contains(slug)) continue;
            if (!pkg.isDownloaded()
                    && pkg.getCachedAccessInfo() != null
                    && pkg.getCachedAccessInfo().hasAccess) {
                count++;
                seenSlugs.add(slug);
            }
        }
        return count;
    }

    /**
     * Counts unique slugs of packages that are out of date and accessible.
     */
    private static long countOutdated(List<EMPackage> allPackages) {
        Set<String> seenSlugs = new HashSet<>();
        long count = 0;
        for (EMPackage pkg : allPackages) {
            if (!pkg.isOutOfDate()) continue;
            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null || slug.isEmpty()) continue;
            // Skip packages we know we don't have access to
            if (pkg.getCachedAccessInfo() != null && !pkg.getCachedAccessInfo().hasAccess) continue;
            if (seenSlugs.add(slug)) count++;
        }
        return count;
    }

    @Override
    public ItemStack getItemstack() {
        return displayIcon;
    }

    @Override
    public void onClick(Player player) {
        player.closeInventory();

        // No token — show detailed setup instructions
        if (!NightbreakAccount.hasToken()) {
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentNightbreakPromptLine1());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakPromptLine2()),
                    SpigotMessage.hoverLinkMessage(
                            InitializeConfig.getAccountLinkDisplay(),
                            InitializeConfig.getAccountLinkHover(),
                            "https://nightbreak.io/account/"));
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentNightbreakPromptLine3());
            player.spigot().sendMessage(
                    SpigotMessage.simpleMessage(DungeonsConfig.getContentNightbreakPromptLine4()),
                    SpigotMessage.hoverLinkMessage(
                            InitializeConfig.getContentLinkDisplay(),
                            InitializeConfig.getContentLinkHover(),
                            "https://nightbreak.io/plugin/elitemobs/"));
            Logger.sendSimpleMessage(player, DungeonsConfig.getContentDownloadSeparator());
            return;
        }

        // Check live state to determine the right action
        boolean hasNotDownloaded = false;
        boolean hasOutdated = false;
        for (EMPackage pkg : EMPackage.getEmPackages().values()) {
            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            if (slug == null || slug.isEmpty()) continue;
            if (!pkg.isDownloaded()
                    && pkg.getCachedAccessInfo() != null
                    && pkg.getCachedAccessInfo().hasAccess) {
                hasNotDownloaded = true;
            }
            if (pkg.isOutOfDate()) {
                hasOutdated = true;
            }
            if (hasNotDownloaded && hasOutdated) break;
        }

        if (!hasNotDownloaded && !hasOutdated) {
            // Everything is installed and up to date
            player.sendMessage(CommandMessagesConfig.getDownloadAllNothingMessage());
            return;
        }

        // Download missing and/or update outdated content
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
