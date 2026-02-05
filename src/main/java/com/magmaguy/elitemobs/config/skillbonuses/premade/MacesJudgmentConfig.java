package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesJudgmentConfig extends SkillBonusConfigFields {
    public MacesJudgmentConfig() {
        super("maces_judgment.yml", true, "&eJudgment",
              List.of("&7Mark enemies for judgment.", "&7Marked targets take bonus damage."),
              SkillType.MACES, 2, 0.40, 0.005, 0.20, 0.0, Material.SUNFLOWER, false);
    }
}
