package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SwordsFlurryConfig extends SkillBonusConfigFields {
    public SwordsFlurryConfig() {
        super("swords_flurry.yml", true, "&eFlurry",
              List.of("&7Consecutive hits increase your", "&7attack speed. Stacks decay over time."),
              SkillType.SWORDS, 2, 5, 0.05, 0.001, Material.BLAZE_POWDER);
        this.loreTemplates = List.of(
                "&7Max Stacks: &f$maxStacks",
                "&7Attack Speed per Stack: &f+$perStack%",
                "&7Max Bonus: &f+$maxBonus%",
                "&7Stacks decay after 3 seconds"
        );
        this.formattedBonusTemplate = "+$maxBonus% Attack Speed (max)";
    }
}
