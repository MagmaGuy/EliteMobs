package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SpearsFirstStrikeConfig extends SkillBonusConfigFields {
    public SpearsFirstStrikeConfig() {
        super("spears_first_strike.yml", true, "&bFirst Strike",
              List.of("&7Deal bonus damage to", "&7full-health enemies."),
              SkillType.SPEARS, SkillBonusType.PASSIVE, 1, 0.30, 0.005, Material.IRON_SWORD);
        this.loreTemplates = List.of(
                "&7Bonus vs Full HP: &f+$value%"
        );
        this.formattedBonusTemplate = "+$value% vs Full HP";
    }
}
