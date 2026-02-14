package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SwordsFinishingFlourishConfig extends SkillBonusConfigFields {
    public SwordsFinishingFlourishConfig() {
        super("swords_finishing_flourish.yml", true, "&cFinishing Flourish",
              List.of("&7Deal bonus damage to enemies", "&7below 30% health."),
              SkillType.SWORDS, SkillBonusType.CONDITIONAL, 3, 0.50, 0.01, Material.WITHER_SKELETON_SKULL);
        this.loreTemplates = List.of(
                "&7Execute Damage: &f+$value%",
                "&7Threshold: &fBelow 30% HP",
                "&7Finish off weakened enemies!"
        );
        this.formattedBonusTemplate = "+$value% Execute Damage";
    }
}
