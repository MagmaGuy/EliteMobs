package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class BowsFrostbiteConfig extends SkillBonusConfigFields {
    public BowsFrostbiteConfig() {
        super("bows_frostbite.yml", true, "&bFrostbite",
              List.of("&7Arrows can freeze enemies,", "&7slowing their movement."),
              SkillType.BOWS, 1, 0.15, 0.003, 0.15, 0.0, Material.PACKED_ICE, false);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$procChance%",
                "&7Applies: &bSlowness &7and &8Mining Fatigue"
        );
        this.formattedBonusTemplate = "$procChance% proc chance";
    }
}
