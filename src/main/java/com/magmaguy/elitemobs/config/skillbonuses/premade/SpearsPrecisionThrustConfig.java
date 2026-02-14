package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsPrecisionThrustConfig extends SkillBonusConfigFields {
    public SpearsPrecisionThrustConfig() {
        super("spears_precision_thrust.yml", true, "&bPrecision Thrust",
              List.of("&7Chance to deal", "&7critical damage."),
              SkillType.SPEARS, 1, 1.5, 0.01, 0.10, Material.DIAMOND);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$chance%",
                "&7Crit Damage: &f$critDamage%"
        );
        this.formattedBonusTemplate = "$chance% for $critDamage% Crit";
    }
}
