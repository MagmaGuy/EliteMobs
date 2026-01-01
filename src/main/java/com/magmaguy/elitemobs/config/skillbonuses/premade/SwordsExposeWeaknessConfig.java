package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SwordsExposeWeaknessConfig extends SkillBonusConfigFields {
    public SwordsExposeWeaknessConfig() {
        super("swords_expose_weakness.yml", true, "&dExpose Weakness",
              List.of("&7Attacks have a chance to reduce", "&7the target's defense."),
              SkillType.SWORDS, 2, 0.10, 0.002, 0.20, Material.BONE);
    }
}
