package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class ArmorFortifyConfig extends SkillBonusConfigFields {
    public ArmorFortifyConfig() {
        super("armor_fortify.yml", true, "&9Fortify",
              List.of("&7Taking damage builds stacks", "&7that reduce future damage."),
              SkillType.ARMOR, SkillBonusType.STACKING, 3, 1.0, 0.02, Material.DIAMOND_CHESTPLATE);
        this.loreTemplates = List.of(
                "&7Build damage reduction stacks when taking damage",
                "&7Max Stacks: $maxStacks",
                "&7Reduction per Stack: $reductionPerStack%",
                "&7Stacks decay after 10 seconds without damage"
        );
        this.formattedBonusTemplate = "$reductionPerStack% reduction per stack (max $maxStacks)";
    }
}
