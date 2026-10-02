package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class StavesWildfireConfig extends SkillBonusConfigFields {
    public StavesWildfireConfig() {
        super("staves_wildfire.yml", true, "&6Wildfire",
              List.of("&7Fireballs hit harder when", "&7enemies crowd together."),
              SkillType.STAVES, SkillBonusType.CONDITIONAL, 2, 0.1, 0.001, Material.FLINT_AND_STEEL);
        this.loreTemplates = List.of(
                "&7Per nearby enemy: &f+$bonusPerEnemy% damage",
                "&7Maximum: &f+$maxBonus%",
                "&8Counts enemies within 4 blocks of the target"
        );
        this.formattedBonusTemplate = "up to +$maxBonus% in crowds";
    }
}
