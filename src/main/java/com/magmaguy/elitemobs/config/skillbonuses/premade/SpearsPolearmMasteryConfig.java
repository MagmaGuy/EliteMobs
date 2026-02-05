package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SpearsPolearmMasteryConfig extends SkillBonusConfigFields {
    public SpearsPolearmMasteryConfig() {
        super("spears_polearm_mastery.yml", true, "&b&lPolearm Mastery",
              List.of("&7Significant attack speed", "&7and damage increase."),
              SkillType.SPEARS, SkillBonusType.PASSIVE, 4, 0.20, 0.003, Material.TRIDENT);
    }
}
