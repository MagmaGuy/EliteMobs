package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class AxesExecutionerConfig extends SkillBonusConfigFields {
    public AxesExecutionerConfig() {
        super("axes_executioner.yml", true, "&4Executioner",
              List.of("&7Deal massive bonus damage to", "&7enemies below 40% health."),
              SkillType.AXES, SkillBonusType.CONDITIONAL, 2, 0.75, 0.015, Material.DIAMOND_AXE);
    }
}
