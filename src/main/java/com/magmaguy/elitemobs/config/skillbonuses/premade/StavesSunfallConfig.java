package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class StavesSunfallConfig extends SkillBonusConfigFields {
    public StavesSunfallConfig() {
        super("staves_sunfall.yml", true, "&6Sunfall",
              List.of("&7Your next fireball every 20", "&7seconds lands with triple force."),
              SkillType.STAVES, 4, 1.5, 0.01, 20.0, Material.SUNFLOWER, true);
        this.loreTemplates = List.of(
                "&7Bonus: &f+$bonus% fireball damage",
                "&7Cooldown: &f$cooldown seconds",
                "&8Every target in the blast"
        );
        this.formattedBonusTemplate = "+$bonus% fireball every $cooldowns";
    }
}
