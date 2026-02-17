package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesRighteousFuryConfig extends SkillBonusConfigFields {
    public MacesRighteousFuryConfig() {
        super("maces_righteous_fury.yml", true, "&eRighteous Fury",
              List.of("&7Build fury stacks on hit,", "&7increasing your damage."),
              SkillType.MACES, 1, 5, 0.05, 0.01, Material.BLAZE_POWDER);
        this.loreTemplates = List.of(
                "&7Max Stacks: &f$maxStacks",
                "&7Damage/Stack: &f+$damagePerStack%",
                "&7Decay: &f5 seconds"
        );
        this.formattedBonusTemplate = "+$maxDamage% Max Damage";
    }
}
