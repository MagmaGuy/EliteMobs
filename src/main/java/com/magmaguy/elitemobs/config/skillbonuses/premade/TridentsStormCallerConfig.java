package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class TridentsStormCallerConfig extends SkillBonusConfigFields {
    public TridentsStormCallerConfig() {
        super("tridents_storm_caller.yml", true, "&eStorm Caller",
              List.of("&7Chance to strike enemies", "&7with lightning."),
              SkillType.TRIDENTS, 2, 0.75, 0.015, 0.15, 0.0, Material.LIGHTNING_ROD, false);
    }
}
