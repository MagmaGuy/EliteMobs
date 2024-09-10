package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.dungeons.*;
import com.magmaguy.magmacore.menus.SetupMenu;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
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

        new SetupMenu(player, ChatColorConverter.convert(List.of(
                "&2To setup the optional/recommended content for EliteMobs:",
                "&61) &fDownload content from &9magmaguy.itch.io &for &9patreon.com/magmaguy",
                "&62) &fPut the content in the &2imports &ffolder of EliteMobs",
                "&63) &fDo &2/em reload",
                "&2That's it!",
                "Click to get more info!")),
                emPackages,
                List.of(createFilter(emPackages, Material.GRASS_BLOCK, "Filter By Open Dungeons", EliteSetupMenu::filterOpenDungeon),
                        createFilter(emPackages, Material.CRYING_OBSIDIAN, "Filter By Instanced Dungeons", EliteSetupMenu::filterInstancedDungeon),
                        createFilter(emPackages, Material.SKULL_BANNER_PATTERN, "Filter By Events", EliteSetupMenu::filterEvents),
                        createFilter(emPackages, Material.DIAMOND_SWORD, "Filter By Custom Items", EliteSetupMenu::filterItems),
                        createFilter(emPackages, Material.ARMOR_STAND, "Filter By Custom Models", EliteSetupMenu::filterModels)));
    }

    private static SetupMenu.SetupMenuFilter createFilter(List<EMPackage> orderedPackages, Material material, String name, Predicate<EMPackage> predicate) {
        List<EMPackage> filteredPackages = orderedPackages.stream()
                .filter(predicate)
                .toList();

        return new SetupMenu.SetupMenuFilter(
                ItemStackGenerator.generateItemStack(material, name),
                filteredPackages);
    }

    private static boolean filterOpenDungeon(EMPackage pkg) {
        return pkg instanceof WorldDungeonPackage ||
                (pkg instanceof MetaPackage metaPackage && metaPackage.getPackages().stream().anyMatch(WorldDungeonPackage.class::isInstance));
    }

    private static boolean filterInstancedDungeon(EMPackage pkg) {
        return pkg instanceof WorldInstancedDungeonPackage ||
                (pkg instanceof MetaPackage metaPackage && metaPackage.getPackages().stream().allMatch(WorldInstancedDungeonPackage.class::isInstance));
    }

    private static boolean filterEvents(EMPackage pkg) {
        return pkg instanceof EventsPackage;
    }

    private static boolean filterModels(EMPackage pkg) {
        return pkg instanceof ModelsPackage;
    }

    private static boolean filterItems(EMPackage pkg) {
        return pkg instanceof ItemsPackage;
    }
}

