package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class WandsDuelistsFocusConfig extends SkillBonusConfigFields {
    public WandsDuelistsFocusConfig() {
        super("wands_duelists_focus.yml", true, "&5Duelist's Focus",
              List.of("&7Missiles hit harder when you", "&7face one enemy alone."),
              SkillType.WANDS, SkillBonusType.CONDITIONAL, 2, 0.25, 0.003, Material.SPYGLASS);
        this.loreTemplates = List.of(
                "&7Bonus: &f+$bonus% damage",
                "&7No other enemy within &f$range&7 blocks"
        );
        this.formattedBonusTemplate = "+$bonus% one-on-one";
    }
}
