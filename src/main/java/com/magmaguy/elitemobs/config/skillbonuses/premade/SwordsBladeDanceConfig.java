package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SwordsBladeDanceConfig extends SkillBonusConfigFields {
    public SwordsBladeDanceConfig() {
        super("swords_blade_dance.yml", true, "&dBlade Dance",
              List.of("&7Toggle: Gain dodge chance but", "&7deal less damage."),
              SkillType.SWORDS, SkillBonusType.TOGGLE, 4, 0.15, 0.003, Material.ELYTRA);
    }
}
