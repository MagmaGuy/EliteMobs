package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsSpellweaveConfig extends SkillBonusConfigFields {
    public WandsSpellweaveConfig() {
        super("wands_spellweave.yml", true, "&dSpellweave",
              List.of("&7Consecutive missiles on one", "&7target build damage. Stacks up to 8x."),
              SkillType.WANDS, 2, 8, 0.03, 0.0004, Material.STRING);
        this.loreTemplates = List.of(
                "&7Per stack: &f+$bonusPerStack% damage",
                "&7Max stacks: &f$maxStacks",
                "&8Resets on a new target or after 3s"
        );
        this.formattedBonusTemplate = "+$bonusPerStack% per stack (max $maxStacks)";
    }
}
