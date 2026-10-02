package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class StavesPhoenixMantleConfig extends SkillBonusConfigFields {
    public StavesPhoenixMantleConfig() {
        super("staves_phoenix_mantle.yml", true, "&6Phoenix Mantle",
              List.of("&7Survive a fatal blow and", "&7burst into flame."),
              SkillType.STAVES, 4, 0.0, 0.0, 120.0, Material.FEATHER, true);
        this.loreTemplates = List.of(
                "&7Prevents fatal damage, leaving 1 heart",
                "&7Throws back and ignites nearby enemies",
                "&7Cooldown: &f$cooldown seconds"
        );
        this.formattedBonusTemplate = "Death prevention every $cooldowns";
    }
}
