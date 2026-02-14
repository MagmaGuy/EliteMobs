package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class ArmorGritConfig extends SkillBonusConfigFields {
    public ArmorGritConfig() {
        super("armor_grit.yml", true, "&8Grit",
              List.of("&7Take less damage when", "&7below 50% health."),
              SkillType.ARMOR, SkillBonusType.CONDITIONAL, 3, 0.50, 0.01, Material.LEATHER_CHESTPLATE);
        this.loreTemplates = List.of(
                "&7Gain increased damage reduction when below 50% health",
                "&7Lower health = more damage reduction",
                "&7Max Reduction: $maxReduction%",
                "&7Requires: Health below 50%"
        );
        this.formattedBonusTemplate = "Up to $maxReduction% damage reduction";
    }
}
