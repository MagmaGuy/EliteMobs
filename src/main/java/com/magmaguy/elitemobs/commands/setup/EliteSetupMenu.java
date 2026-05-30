package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.dungeons.*;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.magmacore.menus.InfoButtonFactory;
import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.menus.SetupMenuBuilder;
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

    public static void createMenu(Player player) {
        List<EMPackage> rawEmPackages = new ArrayList<>(EMPackage.getEmPackages().values());
        List<EMPackage> emPackages = rawEmPackages.stream()
                .sorted(Comparator.comparing(pkg ->
                        ChatColor.stripColor(ChatColorConverter.convert(pkg.getContentPackagesConfigFields().getName()))))
                .collect(Collectors.toList());

        rawEmPackages.stream()
                .filter(MetaPackage.class::isInstance)
                .forEach(rawEmPackage -> ((MetaPackage) rawEmPackage).getPackages().forEach(emPackages::remove));

        // Trigger async refresh of version and access info
        VersionChecker.refreshContentAndAccess();

        MenuButton infoButton = InfoButtonFactory.nightbreakInfoButton(
                "magmaguy",
                "&2Installation instructions:",
                List.of(
                        "&2To setup the optional/recommended content for EliteMobs:",
                        "&61) &fLink your Nightbreak account: &a/nightbreaklogin",
                        "&62) &fDownload all content: &a/em downloadall",
                        "&63) &fOr browse & manage content: &a/em setup",
                        "&2That's it!",
                        "&6Click for more info and links!"),
                "&6&lEliteMobs installation resources:",
                "&2&lWiki page: ",
                InitializeConfig.getWikiLinkDisplay(),
                InitializeConfig.getWikiLinkHover(),
                "https://wiki.nightbreak.io",
                "&2&lContent: ",
                InitializeConfig.getContentLinkDisplay(),
                InitializeConfig.getContentLinkHover(),
                "https://nightbreak.io/plugin/elitemobs/",
                "&2&lDiscord support: ",
                InitializeConfig.getDiscordLinkDisplay(),
                InitializeConfig.getDiscordLinkHover(),
                DiscordLinks.mainLink,
                CommandMessagesConfig.getDownloadAllSetupClickMessage(),
                CommandMessagesConfig.getDownloadAllSetupClickHover(),
                "/em downloadall");

        new SetupMenuBuilder((JavaPlugin) MetadataHandler.PLUGIN, player)
                .title("Setup menu")
                .titleIconPrefix(DefaultConfig.useResourcePackModels()
                        ? "󰻱󰸋󰻵          "
                        : null)
                .infoButton(infoButton)
                .packages(emPackages)
                .appendPackage(new DownloadAllPackage(emPackages))
                .addFilter(Material.GRASS_BLOCK, "Filter By Dungeons",
                        (java.util.function.Predicate<EMPackage>) EliteSetupMenu::filterDungeon)
                .addFilter(Material.SKULL_BANNER_PATTERN, "Filter By Events",
                        (java.util.function.Predicate<EMPackage>) EliteSetupMenu::filterEvents)
                .addFilter(Material.ARMOR_STAND, "Filter By Custom Models",
                        (java.util.function.Predicate<EMPackage>) EliteSetupMenu::filterModels)
                .open();
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
