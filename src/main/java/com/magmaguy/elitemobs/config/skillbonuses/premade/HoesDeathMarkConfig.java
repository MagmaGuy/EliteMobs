package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class HoesDeathMarkConfig extends SkillBonusConfigFields {
    public HoesDeathMarkConfig() {
        super("hoes_death_mark.yml", true, "&8Death Mark",
              List.of("&7Mark enemies for death.", "&7Marked targets take bonus damage."),
              SkillType.HOES, 2, 0.50, 0.01, 0.25, 0.0, Material.WITHER_ROSE, false);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Mark Damage: &f+$damageBonus%",
                "&7Duration: &f15 seconds",
                "&7Marked enemies glow"
        );
        this.formattedBonusTemplate = "+$damageBonus% vs Marked";
    }
}
