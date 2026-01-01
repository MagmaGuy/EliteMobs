package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class AxesRecklessFuryConfig extends SkillBonusConfigFields {
    public AxesRecklessFuryConfig() {
        super("axes_reckless_fury.yml", true, "&cReckless Fury",
              List.of("&7Toggle: Deal more damage but", "&7also take more damage."),
              SkillType.AXES, SkillBonusType.TOGGLE, 3, 0.35, 0.007, Material.BLAZE_POWDER);
    }
}
