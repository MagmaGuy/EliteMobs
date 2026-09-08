package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.custombosses.ClassLootItem;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
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
        return boss.getCustomBossesConfigFields().isClassLoot()
                && !boss.getCustomBossesConfigFields().isReinforcement() && !boss.isReinforcementOrMount();
    }

    public static ItemStack generate(CustomBossEntity boss, int level, Player owner) {
        List<Family> supported = new ArrayList<>();
        for (Family family : Family.values())
            if (family.material() != null && (family.magicType() == null || family.magicType().isAvailable()))
                supported.add(family);
        Family family = supported.get(ThreadLocalRandom.current().nextInt(supported.size()));
        var fields = boss.getCustomBossesConfigFields();
        String bossName = fields.getName().replace("$bossLevel", "").replace("$minibossLevel", "")
                .replace("$normalLevel", "").replace("$eventBossLevel", "").replace("$reinforcementLevel", "")
                .replace("$level", Integer.toString(boss.getLevel()));
        bossName = ChatColor.stripColor(ChatColorConverter.convert(bossName)).strip();
        ClassLootItem presentation = fields.getClassLootItems().getOrDefault(family.skill, ClassLootItem.DEFAULT);
        String name = presentation.name().replace("$boss", bossName).replace("$weapon", family.label);
        String finalBossName = bossName;
        List<String> lore = presentation.lore().stream().map(line ->
                line.replace("$boss", finalBossName).replace("$weapon", family.label)).toList();
        Material material = family.material();
        SkillType explicit = family.skill == SkillType.STAVES || family.skill == SkillType.WANDS ? family.skill : null;
        var enchantments = EnchantmentGenerator.generateEnchantments(level, material, explicit,
                new ItemStack(material).getItemMeta());
        ProceduralItemType magicType = family.magicType();
        String model = magicType == null ? null : magicType.fmmItemId();
        ItemStack item = ItemConstructor.constructItem(level, name, material, enchantments, new HashMap<>(),
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
