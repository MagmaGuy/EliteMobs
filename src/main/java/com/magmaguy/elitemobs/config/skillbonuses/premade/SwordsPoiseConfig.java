package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SwordsPoiseConfig extends SkillBonusConfigFields {
    public SwordsPoiseConfig() {
        super("swords_poise.yml", true, "&aPoise",
              List.of("&7Reduces knockback taken while", "&7wielding a sword."),
              SkillType.SWORDS, SkillBonusType.PASSIVE, 1, 0.20, 0.003, Material.IRON_BOOTS);
    }
}
