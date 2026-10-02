package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsHexBrandConfig extends SkillBonusConfigFields {
    public WandsHexBrandConfig() {
        super("wands_hex_brand.yml", true, "&5Hex Brand",
              List.of("&7Missiles can brand enemies to", "&7take more of your damage."),
              SkillType.WANDS, 1, 0.11, 0.002, 0.15, Material.ENDER_EYE);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Branded: &f+$brandBonus% damage from you",
                "&8Lasts 6 seconds"
        );
        this.formattedBonusTemplate = "+$brandBonus% vs branded";
    }
}
