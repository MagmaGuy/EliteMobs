package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesHarvesterConfig extends SkillBonusConfigFields {
    public HoesHarvesterConfig() {
        super("hoes_harvester.yml", true, "&aHarvester",
              List.of("&7Increased loot drops", "&7from slain enemies."),
              SkillType.HOES, SkillBonusType.PASSIVE, 2, 0.25, 0.005, Material.WHEAT);
        this.loreTemplates = List.of(
                "&7Damage Bonus: &f+$damagePercent%",
                "&7Loot Quality: &f+$lootPercent%",
                "&7Passive harvest benefits"
        );
        this.formattedBonusTemplate = "+$damagePercent% Damage & Loot";
    }
}
