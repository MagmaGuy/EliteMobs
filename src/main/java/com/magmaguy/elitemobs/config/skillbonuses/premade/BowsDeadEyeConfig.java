package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class BowsDeadEyeConfig extends SkillBonusConfigFields {
    public BowsDeadEyeConfig() {
        super("bows_dead_eye.yml", true, "&4Dead Eye",
              List.of("&7Critical hits deal massive", "&7bonus damage. 45s cooldown."),
              SkillType.BOWS, 4, 1.5, 0.03, 0.0, 45.0, Material.ENDER_EYE, true);
    }
}
