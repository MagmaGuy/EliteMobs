package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class StavesRepelConfig extends SkillBonusConfigFields {
    public StavesRepelConfig() {
        super("staves_repel.yml", true, "&eRepel",
              List.of("&7Staff strikes can knock", "&7enemies back."),
              SkillType.STAVES, 2, 0.0, 0.0, 0.25, Material.PISTON);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Knockback and Slowness I for 2s",
                "&8Staff strikes only"
        );
        this.formattedBonusTemplate = "$procChance% knockback";
    }
}
