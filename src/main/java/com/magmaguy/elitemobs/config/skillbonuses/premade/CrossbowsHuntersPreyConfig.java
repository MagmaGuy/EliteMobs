package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsHuntersPreyConfig extends SkillBonusConfigFields {
    public CrossbowsHuntersPreyConfig() {
        super("crossbows_hunters_prey.yml", true, "&4Hunter's Prey",
              List.of("&7Deal bonus damage to enemies", "&7below 50% health."),
              SkillType.CROSSBOWS, SkillBonusType.CONDITIONAL, 2, 0.40, 0.008, Material.BONE);
        this.loreTemplates = List.of(
                "&7Bonus Damage: &f$value%",
                "&7Threshold: &fBelow 50% HP"
        );
        this.formattedBonusTemplate = "+$value% to low HP targets";
    }
}
