package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class ArmorSecondWindConfig extends SkillBonusConfigFields {
    public ArmorSecondWindConfig() {
        super("armor_second_wind.yml", true, "&aSecond Wind",
              List.of("&7Heal when health drops", "&7below 25%. 60s cooldown."),
              SkillType.ARMOR, 3, 1.0, 0.02, 0.0, 60.0, Material.GOLDEN_APPLE, true);
        this.loreTemplates = List.of(
                "&7Automatically heal when health drops below 25%",
                "&7Heal Amount: $healAmount%",
                "&7Cooldown: $cooldown"
        );
        this.formattedBonusTemplate = "$healAmount% heal on low health (CD: $cooldown)";
    }
}
