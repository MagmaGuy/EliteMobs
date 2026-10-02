package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class StavesSmolderConfig extends SkillBonusConfigFields {
    public StavesSmolderConfig() {
        super("staves_smolder.yml", true, "&6Smolder",
              List.of("&7Fireball hits can leave the", "&7target smoldering."),
              SkillType.STAVES, 1, 0.6, 0.004, 0.15, Material.BLAZE_POWDER);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Burn: &f$burnPercent% of the hit over 4s",
                "&8Fireball hits only"
        );
        this.formattedBonusTemplate = "$burnPercent% burn over 4s";
    }
}
