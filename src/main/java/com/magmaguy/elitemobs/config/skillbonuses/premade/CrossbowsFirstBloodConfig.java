package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsFirstBloodConfig extends SkillBonusConfigFields {
    public CrossbowsFirstBloodConfig() {
        super("crossbows_first_blood.yml", true, "&4First Blood",
              List.of("&7Deal massive bonus damage", "&7with the first hit on an enemy."),
              SkillType.CROSSBOWS, SkillBonusType.CONDITIONAL, 4, 0.75, 0.015, Material.REDSTONE);
    }
}
