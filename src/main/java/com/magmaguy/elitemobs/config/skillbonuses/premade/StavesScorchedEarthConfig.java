package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class StavesScorchedEarthConfig extends SkillBonusConfigFields {
    public StavesScorchedEarthConfig() {
        super("staves_scorched_earth.yml", true, "&cScorched Earth",
              List.of("&7Fireballs can leave the", "&7ground burning."),
              SkillType.STAVES, 3, 0.15, 0.002, 0.1, Material.MAGMA_BLOCK);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Burn: &f$pulsePercent% of the hit per second for 4s",
                "&8Hits every enemy in the flames"
        );
        this.formattedBonusTemplate = "$pulsePercent%/s burning ground";
    }
}
