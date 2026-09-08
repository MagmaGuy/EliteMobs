package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.custombosses.ClassLootItem;
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
import java.util.HashMap;
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
        double chance = ClassLootSettingsConfig.dropChance(rank(boss));
        for (Player player : boss.getDamagers().keySet()) {
            if (player.hasMetadata("NPC") || !PlayerData.isInMemory(player.getUniqueId())) continue;
            if (boss instanceof InstancedBossEntity instanced && instanced.getLockoutPlayers().contains(player)) continue;
            if (boss.getDamagers().get(player) / boss.getMaxHealth() < .1) continue;
            if (ThreadLocalRandom.current().nextDouble() >= chance) continue;
            int level = Math.max(1, (int) LootTables.setItemTier(ScaledCombatRewardResolver.getRewardLevel(boss, player)));
            boolean partyPool = !dungeonPool && PartyManager.shouldUsePartyLoot(player, boss);
            ItemStack item = generate(boss, level, dungeonPool || partyPool ? null : player);
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
        var fields = boss.getCustomBossesConfigFields();
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
        List<Family> supported = new ArrayList<>();
        Difficulty difficulty = difficulty(boss);
        Rank rank = rank(boss);
        for (Family family : Family.values())
            if (family.material() != null && (family.magicType() == null || family.magicType().isAvailable())
                    && ClassLootSettingsConfig.profile(difficulty, rank, family.skill) != null)
                supported.add(family);
        if (supported.isEmpty()) return null;
        Family family = supported.get(ThreadLocalRandom.current().nextInt(supported.size()));
        var fields = boss.getCustomBossesConfigFields();
        String bossName = fields.getName().replace("$bossLevel", "").replace("$minibossLevel", "")
                .replace("$normalLevel", "").replace("$eventBossLevel", "").replace("$reinforcementLevel", "")
                .replace("$level", Integer.toString(boss.getLevel()));
        bossName = ChatColor.stripColor(ChatColorConverter.convert(bossName)).strip();
        ClassLootItem presentation = fields.getClassLootItems().getOrDefault(family.skill, ClassLootItem.DEFAULT);
        String name = presentation.name().replace("$boss", bossName).replace("$weapon", family.label)
                .replace("$difficulty", difficulty.name());
        String finalBossName = bossName;
        List<String> lore = presentation.lore().stream().map(line ->
                line.replace("$boss", finalBossName).replace("$weapon", family.label)
                        .replace("$difficulty", difficulty.name())).toList();
        Material material = family.material();
        SkillType explicit = family.skill == SkillType.STAVES || family.skill == SkillType.WANDS ? family.skill : null;
        var roll = ClassLootSettingsConfig.profile(difficulty, rank, family.skill).roll(
                ClassLootSettingsConfig.budgetFraction(), ClassLootSettingsConfig.minimumPrimaryLevel());
        ProceduralItemType magicType = family.magicType();
        String model = magicType == null ? null : magicType.fmmItemId();
        ItemStack item = ItemConstructor.constructItem(level, name, material, roll.nativeEnchantments(), roll.customEnchantments(),
                List.of(), lore, boss, owner, false, null, null, true,
                "class_coverage_" + family.skill.name().toLowerCase(java.util.Locale.ROOT), null, explicit, model);
        if (magicType != null && !magicType.applyMagicData(item)) return null;
        if (WeaponIdentityResolver.progressionSkill(item) != family.skill)
            throw new IllegalStateException("Class loot lost its " + family.skill + " identity");
        return item;
    }

    private enum Family {
        SWORD(SkillType.SWORDS, "Sword", "DIAMOND_SWORD"),
        AXE(SkillType.AXES, "Axe", "DIAMOND_AXE"),
        BOW(SkillType.BOWS, "Bow", "BOW"),
        CROSSBOW(SkillType.CROSSBOWS, "Crossbow", "CROSSBOW"),
        TRIDENT(SkillType.TRIDENTS, "Trident", "TRIDENT"),
        SCYTHE(SkillType.HOES, "Scythe", "DIAMOND_HOE"),
        MACE(SkillType.MACES, "Mace", "MACE"),
        SPEAR(SkillType.SPEARS, "Spear", "DIAMOND_SPEAR"),
        STAFF(SkillType.STAVES, "Staff", "WOODEN_SPEAR"),
        WAND(SkillType.WANDS, "Wand", "BLAZE_ROD");

        private final SkillType skill;
        private final String label;
        private final String materialName;
        Family(SkillType skill, String label, String materialName) {
            this.skill = skill;
            this.label = label;
            this.materialName = materialName;
        }
        Material material() {
            return magicType() == null ? Material.getMaterial(materialName) : magicType().material();
        }
        ProceduralItemType magicType() {
            return switch (skill) {
                case STAVES -> ProceduralItemType.STAFF;
                case WANDS -> ProceduralItemType.WAND;
                default -> null;
            };
        }
    }
}
