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
    }
}
