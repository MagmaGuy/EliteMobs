package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesShatterConfig extends SkillBonusConfigFields {
    public MacesShatterConfig() {
        super("maces_shatter.yml", true, "&eShatter",
              List.of("&7Slam the ground, damaging", "&7and slowing nearby enemies. 15s CD."),
              SkillType.MACES, 2, 1.0, 0.02, 0.0, 15.0, Material.CRACKED_STONE_BRICKS, true);
    }
}
