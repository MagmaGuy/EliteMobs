package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsExplosiveTipConfig extends SkillBonusConfigFields {
    public CrossbowsExplosiveTipConfig() {
        super("crossbows_explosive_tip.yml", true, "&cExplosive Tip",
              List.of("&7Chance for bolts to explode", "&7on impact, damaging nearby enemies."),
              SkillType.CROSSBOWS, 2, 0.50, 0.01, 0.15, 0.0, Material.TNT, false);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$procChance%",
                "&7AoE Damage: &f$aoeDamage%",
                "&7Radius: &f$radius blocks"
        );
        this.formattedBonusTemplate = "$aoeDamage% AoE damage";
    }
}
