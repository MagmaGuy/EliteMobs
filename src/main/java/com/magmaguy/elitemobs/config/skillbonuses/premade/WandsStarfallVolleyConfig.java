package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsStarfallVolleyConfig extends SkillBonusConfigFields {
    public WandsStarfallVolleyConfig() {
        super("wands_starfall_volley.yml", true, "&eStarfall Volley",
              List.of("&7A missile hit calls down a", "&7volley of bolts every 20 seconds."),
              SkillType.WANDS, 4, 0.7, 0.004, 20.0, Material.NETHER_STAR, true);
        this.loreTemplates = List.of(
                "&7Bolts: &f$bolts &7at &f$boltPercent%&7 of the hit",
                "&7Up to 4 enemies",
                "&7Cooldown: &f$cooldown seconds"
        );
        this.formattedBonusTemplate = "$bolts bolts at $boltPercent%";
    }
}
