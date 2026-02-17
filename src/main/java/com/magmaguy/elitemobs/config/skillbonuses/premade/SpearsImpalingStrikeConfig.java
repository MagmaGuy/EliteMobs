package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsImpalingStrikeConfig extends SkillBonusConfigFields {
    public SpearsImpalingStrikeConfig() {
        super("spears_impaling_strike.yml", true, "&bImpaling Strike",
              List.of("&7Chance to cause bleed,", "&7dealing damage over time."),
              SkillType.SPEARS, 2, 0.5, 0.01, 0.15, Material.REDSTONE);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$chance%",
                "&7Bleed: &f$bleedDamage% hit/sec",
                "&7Duration: &f5 seconds"
        );
        this.formattedBonusTemplate = "$totalBleed% Bleed (5s)";
    }
}
