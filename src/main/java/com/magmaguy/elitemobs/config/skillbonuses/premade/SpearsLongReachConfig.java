package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SpearsLongReachConfig extends SkillBonusConfigFields {
    public SpearsLongReachConfig() {
        super("spears_long_reach.yml", true, "&bLong Reach",
              List.of("&7Attacks have slightly", "&7increased range."),
              SkillType.SPEARS, SkillBonusType.PASSIVE, 1, 0.5, 0.01, Material.STICK);
        this.loreTemplates = List.of(
                "&7Extra Reach: &f+$value blocks"
        );
        this.formattedBonusTemplate = "+$value Block Reach";
    }
}
