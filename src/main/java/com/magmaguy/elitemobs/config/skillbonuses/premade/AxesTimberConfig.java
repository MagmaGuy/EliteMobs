package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class AxesTimberConfig extends SkillBonusConfigFields {
    public AxesTimberConfig() {
        super("axes_timber.yml", true, "&2Timber!",
              List.of("&7Bonus damage and hits", "&7disable enemy shields."),
              SkillType.AXES, SkillBonusType.PASSIVE, 4, 0.20, 0.004, Material.OAK_LOG);
    }
}
