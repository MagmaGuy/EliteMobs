package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsVortexThrustConfig extends SkillBonusConfigFields {
    public SpearsVortexThrustConfig() {
        super("spears_vortex_thrust.yml", true, "&bVortex Thrust",
              List.of("&7Pull nearby enemies toward", "&7your target. 18s CD."),
              SkillType.SPEARS, 2, 0.8, 0.005, 0.0, 18.0, Material.ENDER_PEARL, true);
        this.loreTemplates = List.of(
                "&7Pulls enemies within &f$radius blocks &7toward target",
                "&7Applies Slowness II for 2s",
                "&7Cooldown: &f$cooldowns"
        );
        this.formattedBonusTemplate = "Pull + Slow (CD: $cooldowns)";
    }
}
