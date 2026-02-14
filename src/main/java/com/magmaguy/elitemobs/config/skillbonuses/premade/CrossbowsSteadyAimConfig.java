package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsSteadyAimConfig extends SkillBonusConfigFields {
    public CrossbowsSteadyAimConfig() {
        super("crossbows_steady_aim.yml", true, "&aSteady Aim",
              List.of("&7Stand still for bonus damage.", "&7Damage increases over time."),
              SkillType.CROSSBOWS, SkillBonusType.CONDITIONAL, 1, 0.25, 0.005, Material.SPYGLASS);
        this.loreTemplates = List.of(
                "&7Base Bonus: &f$value%",
                "&7Max Bonus: &f3x base",
                "&7Stand still for 1.5s to activate"
        );
        this.formattedBonusTemplate = "+$value% when stationary";
    }
}
