package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class TridentsTidalSurgeConfig extends SkillBonusConfigFields {
    public TridentsTidalSurgeConfig() {
        super("tridents_tidal_surge.yml", true, "&9Tidal Surge",
              List.of("&7Create a wave that knocks", "&7back nearby enemies. 20s CD."),
              SkillType.TRIDENTS, 2, 1.0, 0.02, 0.0, 20.0, Material.WATER_BUCKET, true);
    }
}
