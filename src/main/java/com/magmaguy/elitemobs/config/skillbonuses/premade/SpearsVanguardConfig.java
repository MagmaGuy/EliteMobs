package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsVanguardConfig extends SkillBonusConfigFields {
    public SpearsVanguardConfig() {
        super("spears_vanguard.yml", true, "&bVanguard",
              List.of("&7Charge forward, damaging", "&7enemies in your path. 20s CD."),
              SkillType.SPEARS, 3, 1.0, 0.02, 0.0, 20.0, Material.FEATHER, true);
    }
}
