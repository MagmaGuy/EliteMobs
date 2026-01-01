package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class BowsWindRunnerConfig extends SkillBonusConfigFields {
    public BowsWindRunnerConfig() {
        super("bows_wind_runner.yml", true, "&fWind Runner",
              List.of("&7Gain speed boost when", "&7hitting enemies with arrows."),
              SkillType.BOWS, SkillBonusType.PASSIVE, 3, 0.5, 0.01, Material.FEATHER);
    }
}
