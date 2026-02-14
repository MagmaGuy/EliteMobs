package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsImpalerConfig extends SkillBonusConfigFields {
    public SpearsImpalerConfig() {
        super("spears_impaler.yml", true, "&b&lImpaler",
              List.of("&7Pin an enemy in place,", "&7dealing massive damage. 45s CD."),
              SkillType.SPEARS, 4, 4.0, 0.05, 0.0, 45.0, Material.END_ROD, true);
        this.loreTemplates = List.of(
                "&7Damage: &f$damage%",
                "&7Pin Duration: &f3 seconds",
                "&7Cooldown: &f$cooldowns"
        );
        this.formattedBonusTemplate = "$damage% + Pin (CD: $cooldowns)";
    }
}
