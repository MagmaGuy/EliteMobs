package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SwordsLacerateConfig extends SkillBonusConfigFields {
    public SwordsLacerateConfig() {
        super("swords_lacerate.yml", true, "&cLacerate",
              List.of("&7Attacks have a chance to cause", "&7bleeding, dealing damage over time."),
              SkillType.SWORDS, SkillBonusType.PROC, 1, 2.0, 0.1, Material.REDSTONE);
    }
}
