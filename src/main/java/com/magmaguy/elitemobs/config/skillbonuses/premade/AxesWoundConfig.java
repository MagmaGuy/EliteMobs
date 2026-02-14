package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class AxesWoundConfig extends SkillBonusConfigFields {
    public AxesWoundConfig() {
        super("axes_wound.yml", true, "&cDeep Wound",
              List.of("&7Attacks cause heavy bleeding", "&7that deals damage over time."),
              SkillType.AXES, 1, 3.0, 0.08, 0.12, 0.0, Material.IRON_AXE, false);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$procChance%",
                "&7Bleed: &f$bleedPercent% of hit/tick",
                "&7Total DoT: &f$totalPercent% over 4s"
        );
        this.formattedBonusTemplate = "$totalPercent% bleed over 4s";
    }
}
