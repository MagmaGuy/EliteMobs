package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.dungeons.*;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.menus.SetupMenuBuilder;
import com.magmaguy.magmacore.nightbreak.DownloadAllContentPackage;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.magmacore.nightbreak.NightbreakSetupControls;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EliteSetupMenu {
    private static final String MENU_TITLE = "Setup menu";
    private static final long PLUGIN_UPDATE_MENU_REFRESH_MS = 60_000L;

    public static void createMenu(Player player) {
        createMenu(player, true);
    }

    private static void createMenu(Player player, boolean requestRefresh) {
        VersionChecker.applyCachedContentAndAccess();

        List<EMPackage> rawEmPackages = new ArrayList<>(EMPackage.getEmPackages().values());
        List<EMPackage> emPackages = rawEmPackages.stream()
                .sorted(Comparator.comparing(pkg ->
                        ChatColor.stripColor(ChatColorConverter.convert(pkg.getContentPackagesConfigFields().getName()))))
                .collect(Collectors.toList());

        rawEmPackages.stream()
                .filter(MetaPackage.class::isInstance)
                .forEach(rawEmPackage -> ((MetaPackage) rawEmPackage).getPackages().forEach(emPackages::remove));

        MenuButton infoButton = NightbreakSetupControls.setupInfoButton(
                EliteMobs.NIGHTBREAK_PLUGIN_SPEC,
                "https://wiki.nightbreak.io/EliteMobs/setup/",
                DiscordLinks.mainLink);

        SetupMenuBuilder builder = new SetupMenuBuilder((JavaPlugin) MetadataHandler.PLUGIN, player)
                .title("Setup menu")
                .titleIconPrefix(DefaultConfig.useResourcePackModels()
                        ? "󰻱󰸋󰻵          "
                        : null)
                .infoButton(infoButton)
                .packages(emPackages)
                .appendPackage(new DownloadAllContentPackage<>(() -> new ArrayList<>(EMPackage.getEmPackages().values()),
                        "EliteMobs",
                        "https://nightbreak.io/plugin/elitemobs/",
                        "elitemobs downloadall"))
                .addFilter(Material.GRASS_BLOCK, "Filter By Dungeons",
                        (java.util.function.Predicate<EMPackage>) EliteSetupMenu::filterDungeon)
                .addFilter(Material.SKULL_BANNER_PATTERN, "Filter By Events",
                        (java.util.function.Predicate<EMPackage>) EliteSetupMenu::filterEvents)
                .addFilter(Material.ARMOR_STAND, "Filter By Custom Models",
                        (java.util.function.Predicate<EMPackage>) EliteSetupMenu::filterModels);
        NightbreakSetupControls.prependStandardControls(builder, (JavaPlugin) MetadataHandler.PLUGIN, EliteMobs.NIGHTBREAK_PLUGIN_SPEC)
                .open();

        if (requestRefresh) {
            requestContentStatusRefresh(player);
            requestPluginUpdateRefresh(player);
        }
    }

    private static void requestContentStatusRefresh(Player player) {
        VersionChecker.refreshContentAndAccess(() -> reopenIfStillViewingSetup(player));
    }

    private static void requestPluginUpdateRefresh(Player player) {
        JavaPlugin plugin = (JavaPlugin) MetadataHandler.PLUGIN;
        NightbreakPluginUpdater.CachedPluginUpdateCheck snapshot =
                NightbreakPluginUpdater.getCachedUpdateCheck(plugin, EliteMobs.NIGHTBREAK_PLUGIN_SPEC);
        long checkedAt = snapshot.checkedAtMillis();
        boolean shouldRefresh = !snapshot.checking()
                && (checkedAt == 0L || System.currentTimeMillis() - checkedAt > PLUGIN_UPDATE_MENU_REFRESH_MS);
        if (!shouldRefresh) return;
        NightbreakPluginUpdater.refreshPluginUpdateCheckAsync(plugin,
                EliteMobs.NIGHTBREAK_PLUGIN_SPEC,
                ignored -> reopenIfStillViewingSetup(player));
    }

    private static void reopenIfStillViewingSetup(Player player) {
        if (player == null || !player.isOnline()) return;
        String title = player.getOpenInventory().getTitle();
        if (title == null || !title.contains(MENU_TITLE)) return;
        createMenu(player, false);
    }

    private static boolean filterDungeon(EMPackage pkg) {
        return pkg instanceof WorldDungeonPackage ||
                pkg instanceof WorldInstancedDungeonPackage ||
                pkg instanceof DynamicDungeonPackage ||
                (pkg instanceof MetaPackage metaPackage && metaPackage.getPackages().stream().anyMatch(p ->
                        p instanceof WorldDungeonPackage || p instanceof WorldInstancedDungeonPackage || p instanceof DynamicDungeonPackage));
    }

    private static boolean filterEvents(EMPackage pkg) {
        return pkg instanceof EventsPackage;
    }

    private static boolean filterModels(EMPackage pkg) {
        return pkg instanceof ModelsPackage;
    }
}
