package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesHammerOfWrathConfig extends SkillBonusConfigFields {
    public MacesHammerOfWrathConfig() {
        super("maces_hammer_of_wrath.yml", true, "&eHammer of Wrath",
              List.of("&7Deal massive damage to", "&7enemies below 30% health. 30s CD."),
              SkillType.MACES, 4, 3.0, 0.05, 0.0, 30.0, Material.NETHERITE_BLOCK, true);
        this.loreTemplates = List.of(
                "&7Damage: &f$damage%",
                "&7Threshold: &f<30% HP",
                "&7Cooldown: &f$cooldown" + "s"
        );
        this.formattedBonusTemplate = "$damage% vs Low HP (CD: $cooldown" + "s)";
    }
}
