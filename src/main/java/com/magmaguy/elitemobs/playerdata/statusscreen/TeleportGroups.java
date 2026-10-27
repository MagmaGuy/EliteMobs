package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields.DungeonSizeCategory;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.dungeons.CombatContent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.MetaPackage;

import java.util.*;
import java.util.function.Function;

/**
 * Orders the dungeons on the teleport pages into groups. A content bundle with at least two listed
 * dungeons (Story Mode Dungeons, for example) keeps its own group, in the order the bundle lists
 * them. Every other dungeon joins the group for its size category, lowest level first, then by name.
 */
public final class TeleportGroups {
    /** Above this many dungeons the dialog shows one button per group instead of a single list. */
    public static final int DIALOG_GROUPING_THRESHOLD = 8;

    private static final List<DungeonSizeCategory> SIZE_ORDER = List.of(
            DungeonSizeCategory.ADVENTURE,
            DungeonSizeCategory.LAIR,
            DungeonSizeCategory.MINIDUNGEON,
            DungeonSizeCategory.SANCTUM,
            DungeonSizeCategory.DUNGEON,
            DungeonSizeCategory.RAID,
            DungeonSizeCategory.REALM,
            DungeonSizeCategory.ARENA,
            DungeonSizeCategory.OTHER);

    private TeleportGroups() {
    }

    public record Group<T>(String key, String name, List<T> dungeons) {
    }

    public record Entry<T>(String filename, String name, DungeonSizeCategory size, int lowestLevel, T dungeon) {
    }

    public record Bundle(String filename, String name, List<String> containedFilenames) {
    }

    /** The installed dungeons that ask to be listed, grouped and ordered for display. */
    public static List<Group<EMPackage>> current() {
        List<Entry<EMPackage>> dungeons = new ArrayList<>();
        List<Bundle> bundles = new ArrayList<>();
        for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
            ContentPackagesConfigFields fields = emPackage.getContentPackagesConfigFields();
            if (emPackage instanceof MetaPackage) {
                if (fields.getContainedPackages() != null)
                    bundles.add(new Bundle(fields.getFilename(), fields.getName(), fields.getContainedPackages()));
                continue;
            }
            if (!emPackage.isInstalled() ||
                    !(emPackage instanceof CombatContent combatContent) ||
                    fields.isEnchantmentChallenge() ||
                    !fields.isListedInTeleports()) continue;
            dungeons.add(new Entry<>(fields.getFilename(), fields.getName(), fields.getDungeonSizeCategory(),
                    combatContent.getLowestLevel(), emPackage));
        }
        return group(dungeons, bundles, PlayerStatusMenuConfig::getTeleportGroupName);
    }

    public static Group<EMPackage> find(String key) {
        if (key == null) return null;
        for (Group<EMPackage> group : current())
            if (group.key().equals(key)) return group;
        return null;
    }

    public static int dungeonCount(List<? extends Group<?>> groups) {
        int count = 0;
        for (Group<?> group : groups) count += group.dungeons().size();
        return count;
    }

    static <T> List<Group<T>> group(List<Entry<T>> dungeons, List<Bundle> bundles,
                                    Function<DungeonSizeCategory, String> sizeName) {
        Map<String, Entry<T>> unclaimed = new LinkedHashMap<>();
        for (Entry<T> entry : dungeons) unclaimed.put(entry.filename(), entry);

        List<Bundle> bundleOrder = new ArrayList<>(bundles);
        bundleOrder.sort(Comparator.comparing(Bundle::filename));
        List<List<Entry<T>>> bundleMembers = new ArrayList<>();
        List<Bundle> formedBundles = new ArrayList<>();
        for (Bundle bundle : bundleOrder) {
            List<Entry<T>> members = new ArrayList<>();
            for (String filename : bundle.containedFilenames()) {
                Entry<T> entry = unclaimed.get(filename);
                if (entry != null && !members.contains(entry)) members.add(entry);
            }
            // A bundle with one listed dungeon would only add a click, so that dungeon stays with its size.
            if (members.size() < 2) continue;
            members.forEach(entry -> unclaimed.remove(entry.filename()));
            bundleMembers.add(members);
            formedBundles.add(bundle);
        }

        List<Integer> bundleIndexes = new ArrayList<>();
        for (int i = 0; i < formedBundles.size(); i++) bundleIndexes.add(i);
        bundleIndexes.sort(Comparator
                .comparingInt((Integer i) -> bundleMembers.get(i).stream().mapToInt(Entry::lowestLevel).min().orElse(0))
                .thenComparing(i -> sortName(formedBundles.get(i).name()))
                .thenComparing(i -> formedBundles.get(i).filename()));

        List<Group<T>> groups = new ArrayList<>();
        for (int i : bundleIndexes) {
            Bundle bundle = formedBundles.get(i);
            groups.add(new Group<>(bundle.filename(), bundle.name(), dungeonsOf(bundleMembers.get(i))));
        }

        Map<DungeonSizeCategory, List<Entry<T>>> bySize = new EnumMap<>(DungeonSizeCategory.class);
        for (Entry<T> entry : unclaimed.values())
            bySize.computeIfAbsent(entry.size() == null ? DungeonSizeCategory.OTHER : entry.size(), size -> new ArrayList<>())
                    .add(entry);
        List<DungeonSizeCategory> sizeOrder = new ArrayList<>(SIZE_ORDER);
        for (DungeonSizeCategory size : DungeonSizeCategory.values())
            if (!sizeOrder.contains(size)) sizeOrder.add(size);
        for (DungeonSizeCategory size : sizeOrder) {
            List<Entry<T>> members = bySize.get(size);
            if (members == null) continue;
            members.sort(Comparator.comparingInt((Entry<T> entry) -> entry.lowestLevel())
                    .thenComparing(entry -> sortName(entry.name()))
                    .thenComparing(Entry::filename));
            groups.add(new Group<>(size.name(), sizeName.apply(size), dungeonsOf(members)));
        }
        return groups;
    }

    private static <T> List<T> dungeonsOf(List<Entry<T>> entries) {
        List<T> dungeons = new ArrayList<>(entries.size());
        for (Entry<T> entry : entries) dungeons.add(entry.dungeon());
        return List.copyOf(dungeons);
    }

    /** Dungeon names carry colour codes and gradient tags, which must not decide the order. */
    static String sortName(String name) {
        if (name == null) return "";
        return name.replaceAll("(?i)[&§]#[0-9a-f]{6}", "")
                .replaceAll("(?i)[&§][0-9a-fk-orx]", "")
                .replaceAll("<[^>]*>", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
