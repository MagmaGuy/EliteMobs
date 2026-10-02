package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsUnravelConfig extends SkillBonusConfigFields {
    public WandsUnravelConfig() {
        super("wands_unravel.yml", true, "&dUnravel",
              List.of("&7Missiles can unravel an enemy's", "&7defenses for everyone."),
              SkillType.WANDS, 3, 0.1, 0.001, 0.1, Material.FERMENTED_SPIDER_EYE);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Unravelled: &f+$bonus% damage from all players",
                "&8Lasts 5 seconds"
        );
        this.formattedBonusTemplate = "+$bonus% party damage";
    }
}
