package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class AxesStaggerConfig extends SkillBonusConfigFields {
    public AxesStaggerConfig() {
        super("axes_stagger.yml", true, "&eStagger",
              List.of("&7Chance to stagger enemies,", "&7slowing and weakening them."),
              SkillType.AXES, 2, 0.0, 0.0, 0.10, 0.0, Material.SHIELD, false);
    }
}
