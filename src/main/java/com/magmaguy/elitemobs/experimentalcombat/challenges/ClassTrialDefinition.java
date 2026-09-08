package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.npcs.ClassTrainerConfig;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.mobconstructor.BossType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

/** Selected authored encounter; arena admission and progression remain owned by the existing challenge. */
public record ClassTrialDefinition(ClassFormDefinition form, String root,
                                   String opening, String halfway, String victory, String defeat) {
    public static ClassTrialDefinition forForm(String formId) {
        var lineage = BuiltInClassContent.catalog().lineageOf(formId);
        var encounter = TrialEncounterAssets.require(formId);
        TrialPoses.validate();
        // Existing admission resolves this definition before charging the attempt fee.
        if (encounter.magicWeapon() != null) TrialEquipment.magic(encounter.magicWeapon());
        return new ClassTrialDefinition(lineage.activeForm(), lineage.root().id(),
                encounter.opening(), encounter.halfway(), encounter.victory(), encounter.defeat());
    }

    public CustomBossesConfigFields boss() {
        var encounter = TrialEncounterAssets.require(form.id());
        var boss = new CustomBossesConfigFields("class_trial_" + form.id() + ".yml", EntityType.HUSK,
                true, "$bossLevel &6" + form.displayName() + " Instructor", Integer.toString(form.requiredFoundationSkillLevel()));
        boss.setBossType(BossType.BOSS);
        boss.setHealthMultiplier(encounter.healthMultiplier());
        boss.setDamageMultiplier(1D);
        boss.setNormalizedCombat(true);
        boss.setAi(false);
        boss.setMovementSpeedAttribute(root.equals("ranger") ? .29 : .24);
        boss.setFollowDistance(70);
        boss.setDisguise(ClassTrainerConfig.disguise(encounter.skin()));
        boss.setDropsEliteMobsLoot(false);
        boss.setDropsVanillaLoot(false);
        boss.setDropsRandomLoot(false);
        boss.setDropsSkillXP(false);
        boss.setClassLoot(false);
        boss.setPowers(List.of());
        boss.setUniqueLootList(List.of());
        encounter.equipment().forEach((slot, material) -> {
            ItemStack item = new ItemStack(material);
            var meta = item.getItemMeta();
            meta.setUnbreakable(true);
            if (meta instanceof LeatherArmorMeta leather) leather.setColor(encounter.armorColor());
            item.setItemMeta(meta);
            switch (slot) {
                case HAND -> boss.setMainHand(item);
                case OFF_HAND -> boss.setOffHand(item);
                case HEAD -> boss.setHelmet(item);
                case CHEST -> boss.setChestplate(item);
                case LEGS -> boss.setLeggings(item);
                case FEET -> boss.setBoots(item);
                default -> throw new IllegalArgumentException("Unsupported trial equipment slot " + slot);
            }
        });
        if (encounter.magicWeapon() != null) boss.setMainHand(TrialEquipment.magic(encounter.magicWeapon()));
        return boss;
    }
}
