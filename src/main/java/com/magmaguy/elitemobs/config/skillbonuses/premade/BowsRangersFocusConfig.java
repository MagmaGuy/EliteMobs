package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class BowsRangersFocusConfig extends SkillBonusConfigFields {
    public BowsRangersFocusConfig() {
        super("bows_rangers_focus.yml", true, "&6Ranger's Focus",
              List.of("&7Focus on one target for", "&7stacking damage. Stacks up to 8x."),
              SkillType.BOWS, SkillBonusType.STACKING, 4, 0.04, 0.001, Material.TARGET);
    }
}
