package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.custombosses.ClassLootItem;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.ProceduralItemType;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
        var available = availableFamilies(difficulty, rank);
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
            ClassLootFamily family = ClassLootSelection.select(available, preferenceOwner);
            if (family == null) continue;
            ItemStack item = generate(boss.getCustomBossesConfigFields(), boss.getLevel(), level, difficulty, rank,
                    family, boss, dungeonPool || partyPool ? null : player);
            if (item == null) continue;
            if (dungeonPool) {
                SharedLootTable table = SharedLootTable.getSharedLootTables().get(boss);
                if (table == null) table = new SharedLootTable(boss);
                table.addLoot(item);
            } else if (!partyPool || !SharedLootTable.addPartyLoot(boss, player, item))
                LootTables.deliverGeneratedItem(player, boss.getLocation(), item);
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
        ClassLootFamily family = ClassLootSelection.select(availableFamilies(difficulty, rank), owner);
        if (family == null) return null;
        return generate(boss.getCustomBossesConfigFields(), boss.getLevel(), level, difficulty, rank, family, boss, owner);
    }

    public static List<ClassLootFamily> availableFamilies(Difficulty difficulty, Rank rank) {
        List<ClassLootFamily> supported = new ArrayList<>();
        for (ClassLootFamily family : ClassLootFamily.values())
            if (family.material() != null && (family.magicType() == null || family.magicType().isAvailable())
                    && ClassLootSettingsConfig.profile(difficulty, rank, family) != null)
                supported.add(family);
        return List.copyOf(supported);
    }

    /** Uses the production item constructor without spawning an entity or awarding death rewards. */
    public static ItemStack preview(CustomBossesConfigFields fields, int level, Difficulty difficulty,
                                    Rank rank, ClassLootFamily family, Player owner) {
        if (!availableFamilies(difficulty, rank).contains(family)) return null;
        return generate(fields, level, level, difficulty, rank, family, null, owner);
    }

    private static ItemStack generate(CustomBossesConfigFields fields, int mobLevel, int level, Difficulty difficulty,
                                      Rank rank, ClassLootFamily family, CustomBossEntity boss, Player owner) {
        String bossName = fields.getName().replace("$bossLevel", "").replace("$minibossLevel", "")
                .replace("$normalLevel", "").replace("$eventBossLevel", "").replace("$reinforcementLevel", "")
                .replace("$level", Integer.toString(mobLevel));
        bossName = ChatColor.stripColor(ChatColorConverter.convert(bossName)).strip();
        ClassLootItem presentation = fields.getClassLootItems().getOrDefault(family, ClassLootItem.DEFAULT);
        String name = presentation.name().replace("$boss", bossName).replace("$weapon", family.label()).replace("$item", family.label())
                .replace("$difficulty", difficulty.name());
        String finalBossName = bossName;
        List<String> lore = presentation.lore().stream().map(line ->
                line.replace("$boss", finalBossName).replace("$weapon", family.label()).replace("$item", family.label())
                        .replace("$difficulty", difficulty.name())).toList();
        Material material = family.material();
        SkillType explicit = family.skill() == SkillType.STAVES || family.skill() == SkillType.WANDS ? family.skill() : null;
        var profile = ClassLootSettingsConfig.profile(difficulty, rank, family);
        var roll = profile.roll(
                ClassLootSettingsConfig.budgetFraction(), ClassLootSettingsConfig.minimumPrimaryLevel());
        ProceduralItemType magicType = family.magicType();
        String model = magicType == null ? null : magicType.fmmItemId();
        ItemStack item = ItemConstructor.constructItem(level, name, material, roll.nativeEnchantments(), roll.customEnchantments(),
                profile.potionEffects(), lore, boss, owner, false, null, null, true,
                "class_coverage_" + family.name().toLowerCase(java.util.Locale.ROOT), null, explicit, model);
        if (magicType != null && !magicType.applyMagicData(item)) return null;
        if (family.isWeapon() && WeaponIdentityResolver.progressionSkill(item) != family.skill())
            throw new IllegalStateException("Class loot lost its " + family.skill() + " identity");
        return item;
    }

}
