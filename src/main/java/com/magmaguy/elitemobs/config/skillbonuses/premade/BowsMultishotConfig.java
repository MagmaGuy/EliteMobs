package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class BowsMultishotConfig extends SkillBonusConfigFields {
    public BowsMultishotConfig() {
        super("bows_multishot.yml", true, "&dMultishot",
              List.of("&7Chance to fire additional", "&7arrows on each shot."),
              SkillType.BOWS, 2, 0.0, 0.0, 0.20, 0.0, Material.SPECTRAL_ARROW, false);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$procChance%",
                "&7Extra Arrows: &f$extraArrows",
                "&7Arrow Damage: &f50% of original"
        );
        this.formattedBonusTemplate = "$procChance% chance for extra arrows";
    }
}
