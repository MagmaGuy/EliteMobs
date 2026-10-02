package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsArcingBoltConfig extends SkillBonusConfigFields {
    public WandsArcingBoltConfig() {
        super("wands_arcing_bolt.yml", true, "&9Arcing Bolt",
              List.of("&7Missiles can arc to another", "&7nearby enemy."),
              SkillType.WANDS, 2, 0.55, 0.005, 0.15, Material.LIGHTNING_ROD);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Arc damage: &f$arcPercent% of the hit",
                "&8Needs an enemy within 5 blocks"
        );
        this.formattedBonusTemplate = "$arcPercent% arc";
    }
}
