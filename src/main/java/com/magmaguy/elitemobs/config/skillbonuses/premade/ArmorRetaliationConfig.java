package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class ArmorRetaliationConfig extends SkillBonusConfigFields {
    public ArmorRetaliationConfig() {
        super("armor_retaliation.yml", true, "&cRetaliation",
              List.of("&7Chance to reflect damage", "&7back to attackers."),
              SkillType.ARMOR, 2, 1.0, 0.02, 0.25, 0.0, Material.CACTUS, false);
        this.loreTemplates = List.of(
                "&7Chance to reflect damage back to attackers",
                "&7Proc Chance: $procChance%",
                "&7Reflect Damage: $reflectDamage%"
        );
        this.formattedBonusTemplate = "$procChance% chance to reflect $reflectDamage% damage";
    }
}
