package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SpearsPhalanxConfig extends SkillBonusConfigFields {
    public SpearsPhalanxConfig() {
        super("spears_phalanx.yml", true, "&bPhalanx",
              List.of("&7Reduced damage from", "&7frontal attacks."),
              SkillType.SPEARS, SkillBonusType.PASSIVE, 2, 0.15, 0.002, Material.SHIELD);
    }
}
