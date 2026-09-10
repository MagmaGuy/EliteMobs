package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.items.itemconstructor.ClassLootItemConstructor;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** A balanced baseline independent of a dungeon's age or number of authored sword/bow drops. */
public final class ClassLootCoverage {
    private ClassLootCoverage() {}

    public static boolean enabled(CustomBossEntity boss) {
        return ClassLootSettingsConfig.enabled() && boss.getCustomBossesConfigFields().isClassLoot()
                && !boss.getCustomBossesConfigFields().isReinforcement() && !boss.isReinforcementOrMount();
    }

    public static void dropLoot(CustomBossEntity boss) {
        if (!enabled(boss) || boss.isTriggeredAntiExploit() || boss.getLevel() < 1) return;
        boolean dungeonPool = boss instanceof InstancedBossEntity;
        Difficulty difficulty = difficulty(boss);
        Rank rank = rank(boss);
        double chance = ClassLootSettingsConfig.dropChance(rank);
        List<Player> contributors = boss.getDamagers().keySet().stream().filter(player ->
                !player.hasMetadata("NPC") && PlayerData.isInMemory(player.getUniqueId())
                        && !(boss instanceof InstancedBossEntity instanced && instanced.getLockoutPlayers().contains(player))
                        && boss.getDamagers().get(player) / boss.getMaxHealth() >= .1).toList();
        for (Player player : contributors) {
            if (ThreadLocalRandom.current().nextDouble() >= chance) continue;
            int level = Math.max(1, (int) LootTables.setItemTier(ScaledCombatRewardResolver.getRewardLevel(boss, player)));
            boolean partyPool = !dungeonPool && PartyManager.shouldUsePartyLoot(player, boss);
            List<Player> audience = List.of(player);
            if (dungeonPool) audience = contributors;
            else if (partyPool) {
                var party = PartyManager.getParty(player.getUniqueId());
                var nearby = PartyManager.getNearbyMembers(party, boss.getLocation());
                audience = contributors.stream().filter(nearby::contains).toList();
            }
            Player preferenceOwner = audience.isEmpty() ? player : audience.get(ThreadLocalRandom.current().nextInt(audience.size()));
            var eligible = candidates(boss.getCustomBossesConfigFields(), difficulty, rank, preferenceOwner, boss);
            var selected = ClassLootSelection.selectEntry(eligible, preferenceOwner);
            if (selected == null || !selected.willDrop(preferenceOwner)) continue;
            for (int copy = 0; copy < selected.getAmount(); copy++) {
                ItemStack item = ClassLootItemConstructor.construct(CustomItem.getCustomItem(selected.getFilename()),
                        level, difficulty, rank, boss, dungeonPool || partyPool ? null : player);
                if (item == null) continue;
                if (dungeonPool) {
                    SharedLootTable table = SharedLootTable.getSharedLootTables().get(boss);
                    if (table == null) table = new SharedLootTable(boss);
                    table.addLoot(item);
                } else if (!partyPool || !SharedLootTable.addPartyLoot(boss, player, item))
                    LootTables.deliverGeneratedItem(player, boss.getLocation(), item);
            }
        }
    }

    public static Difficulty difficulty(CustomBossEntity boss) {
        String override = boss.getCustomBossesConfigFields().getClassLootDifficulty();
        if (!"AUTO".equals(override))
            return ClassLootSettingsConfig.difficulty(override, ClassLootSettingsConfig.defaultDifficulty());
        if (boss instanceof InstancedBossEntity instanced && instanced.getDungeonInstance() != null)
            return ClassLootSettingsConfig.forDifficultyId(instanced.getDungeonInstance().getDifficultyID(),
                    instanced.getDungeonInstance().getResolvedDifficultyID());
        return ClassLootSettingsConfig.defaultDifficulty();
    }

    public static Rank rank(CustomBossEntity boss) {
        return rank(boss.getCustomBossesConfigFields());
    }

    public static Rank rank(CustomBossesConfigFields fields) {
        try { return Rank.valueOf(fields.getClassLootRank()); }
        catch (IllegalArgumentException ignored) { /* AUTO uses explicit boss type, then legacy level tokens. */ }
        return switch (fields.getBossType()) {
            case BOSS, EVENT -> Rank.BOSS;
            case MINIBOSS -> Rank.MINIBOSS;
            default -> fields.getName().contains("$minibossLevel") ? Rank.MINIBOSS
                    : fields.getName().contains("$bossLevel") || fields.getName().contains("$eventBossLevel")
                    ? Rank.BOSS : Rank.TRASH;
        };
    }

    public static ItemStack generate(CustomBossEntity boss, int level, Player owner) {
        Difficulty difficulty = difficulty(boss);
        Rank rank = rank(boss);
        ClassLootFamily family = ClassLootSelection.select(candidates(boss.getCustomBossesConfigFields(), difficulty, rank, owner, boss).stream()
                .map(entry -> CustomItem.getCustomItem(entry.getFilename()).getCustomItemsConfigFields().getClassLootFamily()).distinct().toList(), owner);
        if (family == null) return null;
        return generate(boss.getCustomBossesConfigFields(), boss.getLevel(), level, difficulty, rank, family, boss, owner);
    }

    /** Uses the production item constructor without spawning an entity or awarding death rewards. */
    public static ItemStack preview(CustomBossesConfigFields fields, int level, Difficulty difficulty,
                                    Rank rank, ClassLootFamily family, Player owner) {
        if (!availableFamilies(fields, difficulty, rank, owner).contains(family)) return null;
        return generate(fields, level, level, difficulty, rank, family, null, owner);
    }

    private static ItemStack generate(CustomBossesConfigFields fields, int mobLevel, int level, Difficulty difficulty,
                                      Rank rank, ClassLootFamily family, CustomBossEntity boss, Player owner) {
        var choices = candidates(fields, difficulty, rank, owner, boss).stream()
                .filter(entry -> CustomItem.getCustomItem(entry.getFilename()).getCustomItemsConfigFields().getClassLootFamily() == family).toList();
        if (choices.isEmpty()) return null;
        var item = CustomItem.getCustomItem(choices.get(ThreadLocalRandom.current().nextInt(choices.size())).getFilename());
        return ClassLootItemConstructor.construct(item, level, difficulty, rank, boss, owner, fields.getName());
    }

    public static List<EliteCustomLootEntry> candidates(CustomBossesConfigFields fields, Difficulty difficulty,
                                                       Rank rank, Player player, CustomBossEntity boss) {
        if (fields.getCustomLootTable() == null) return List.of();
        var resolver = new com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver(fields.getFilename(), List.of());
        java.util.function.Predicate<List<String>> filter = ids -> resolver.matches(ids, Integer.toString(difficulty.ordinal()), fields.getFilename());
        if (boss instanceof InstancedBossEntity instanced && instanced.getDungeonInstance() != null)
            filter = ids -> instanced.getDungeonInstance().matchesDifficulty(ids, fields.getFilename());
        var difficultyFilter = filter;
        return fields.getCustomLootTable().getEntries().stream()
                .filter(EliteCustomLootEntry.class::isInstance).map(EliteCustomLootEntry.class::cast)
                .filter(entry -> entry.eligibleForClassLoot(player, difficultyFilter))
                .filter(entry -> ClassLootItemConstructor.available(CustomItem.getCustomItem(entry.getFilename()), difficulty, rank))
                .toList();
    }

    public static List<ClassLootFamily> availableFamilies(CustomBossesConfigFields fields, Difficulty difficulty,
                                                         Rank rank, Player player) {
        return candidates(fields, difficulty, rank, player, null).stream()
                .map(entry -> CustomItem.getCustomItem(entry.getFilename()).getCustomItemsConfigFields().getClassLootFamily())
                .distinct().toList();
    }

}
