package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.dungeons.*;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.magmacore.menus.ContentPackage;
import com.magmaguy.magmacore.menus.MenuButton;
import com.magmaguy.magmacore.menus.SetupMenu;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.SpigotMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
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

        MenuButton infoButton = new MenuButton(ItemStackGenerator.generateSkullItemStack("magmaguy",
                "&2Installation instructions:",
                List.of(
                        "&2To setup the optional/recommended content for EliteMobs:",
                        "&61) &fDownload content from &9https://nightbreak.io/plugin/elitemobs",
                        "&62) &fPut the content in the &2imports &ffolder of EliteMobs",
                        "&63) &fDo &2/em reload",
                        "&2That's it!",
                        "&6Click for more info and links!"))) {
            @Override
            public void onClick(Player p) {
                p.closeInventory();
                Logger.sendSimpleMessage(p, "&8&l&m&o---------------------------------------------");
                Logger.sendSimpleMessage(p, "&6&lEliteMobs installation resources:");
                Logger.sendSimpleMessage(p, "&2&lWiki page: &9&nhttps://magmaguy.com/wiki.html");
                Logger.sendSimpleMessage(p, "&2&lVideo setup guide: &9&nhttps://youtu.be/boRg2X4qhw4");
                Logger.sendSimpleMessage(p, "&2&lContent download links: &9&nhttps://nightbreak.io/plugin/elitemobs/");
                Logger.sendSimpleMessage(p, "&2&lDiscord support: &9&nhttps://discord.gg/9f5QSka");
                if (NightbreakAccount.hasToken()) {
                    p.spigot().sendMessage(
                            SpigotMessage.commandHoverMessage(
                                    CommandMessagesConfig.getDownloadAllSetupClickMessage(),
                                    CommandMessagesConfig.getDownloadAllSetupClickHover(),
                                    "/em downloadall"
                            )
                    );
                }
                Logger.sendSimpleMessage(p, "&8&l&m&o---------------------------------------------");
            }
        };

        // Add DownloadAllPackage as a content item at the end
        List<ContentPackage> allPackages = new ArrayList<>(emPackages);
        allPackages.add(new DownloadAllPackage(emPackages));

        String menuTitle = "Setup menu";
        if (DefaultConfig.useResourcePackModels())
            menuTitle = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE0B\uDB83\uDEF5          " + menuTitle;

        new SetupMenu(player, infoButton, allPackages,
                List.of(createFilter(emPackages, Material.GRASS_BLOCK, "Filter By Dungeons", EliteSetupMenu::filterDungeon),
                        createFilter(emPackages, Material.SKULL_BANNER_PATTERN, "Filter By Events", EliteSetupMenu::filterEvents),
                        createFilter(emPackages, Material.ARMOR_STAND, "Filter By Custom Models", EliteSetupMenu::filterModels)),
                menuTitle);
    }

    private static SetupMenu.SetupMenuFilter createFilter(List<EMPackage> orderedPackages, Material material, String name, Predicate<EMPackage> predicate) {
        List<EMPackage> filteredPackages = orderedPackages.stream()
                .filter(predicate)
                .toList();

        return new SetupMenu.SetupMenuFilter(
                ItemStackGenerator.generateItemStack(material, name),
                filteredPackages);
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
