package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.dungeons.*;
import com.magmaguy.elitemobs.utils.DiscordLinks;
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
                        "&61) &fLink your Nightbreak account: &a/nightbreaklogin",
                        "&62) &fDownload all content: &a/em downloadall",
                        "&63) &fOr browse & manage content: &a/em setup",
                        "&2That's it!",
                        "&6Click for more info and links!"))) {
            @Override
            public void onClick(Player p) {
                p.closeInventory();
                Logger.sendSimpleMessage(p, "<g:#8B0000:#CC4400:#DAA520>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</g>");
                Logger.sendSimpleMessage(p, "&6&lEliteMobs installation resources:");
                p.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&2&lWiki page: "),
                        SpigotMessage.hoverLinkMessage(
                                InitializeConfig.getWikiLinkDisplay(),
                                InitializeConfig.getWikiLinkHover(),
                                "https://wiki.nightbreak.io"));
                p.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&2&lContent: "),
                        SpigotMessage.hoverLinkMessage(
                                InitializeConfig.getContentLinkDisplay(),
                                InitializeConfig.getContentLinkHover(),
                                "https://nightbreak.io/plugin/elitemobs/"));
                p.spigot().sendMessage(
                        SpigotMessage.simpleMessage("&2&lDiscord support: "),
                        SpigotMessage.hoverLinkMessage(
                                InitializeConfig.getDiscordLinkDisplay(),
                                InitializeConfig.getDiscordLinkHover(),
                                DiscordLinks.mainLink));
                if (NightbreakAccount.hasToken()) {
                    p.spigot().sendMessage(
                            SpigotMessage.commandHoverMessage(
                                    CommandMessagesConfig.getDownloadAllSetupClickMessage(),
                                    CommandMessagesConfig.getDownloadAllSetupClickHover(),
                                    "/em downloadall"
                            )
                    );
                }
                Logger.sendSimpleMessage(p, "<g:#8B0000:#CC4400:#DAA520>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</g>");
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
