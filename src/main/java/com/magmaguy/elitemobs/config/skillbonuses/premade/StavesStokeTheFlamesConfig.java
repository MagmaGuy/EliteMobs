package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class StavesStokeTheFlamesConfig extends SkillBonusConfigFields {
    public StavesStokeTheFlamesConfig() {
        super("staves_stoke_the_flames.yml", true, "&cStoke the Flames",
              List.of("&7Each fireball that hits builds", "&7heat for the next. Stacks up to 5x."),
              SkillType.STAVES, 2, 5, 0.05, 0.0006, Material.CAMPFIRE);
        this.loreTemplates = List.of(
                "&7Per stack: &f+$bonusPerStack% fireball damage",
                "&7Max stacks: &f$maxStacks",
                "&8Fades after 6s without a fireball hit"
        );
        this.formattedBonusTemplate = "+$bonusPerStack% per stack (max $maxStacks)";
    }
}
