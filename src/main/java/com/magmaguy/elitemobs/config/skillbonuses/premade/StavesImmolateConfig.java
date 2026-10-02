package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class StavesImmolateConfig extends SkillBonusConfigFields {
    public StavesImmolateConfig() {
        super("staves_immolate.yml", true, "&4Immolate",
              List.of("&7Fireballs deal bonus damage", "&7to badly wounded enemies."),
              SkillType.STAVES, SkillBonusType.CONDITIONAL, 3, 0.45, 0.006, Material.LAVA_BUCKET);
        this.loreTemplates = List.of(
                "&7Bonus: &f+$bonus% damage",
                "&7Below &f$threshold%&7 health"
        );
        this.formattedBonusTemplate = "+$bonus% below $threshold% HP";
    }
}
