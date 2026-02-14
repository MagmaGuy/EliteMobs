package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class MacesStunningForceConfig extends SkillBonusConfigFields {
    public MacesStunningForceConfig() {
        super("maces_stunning_force.yml", true, "&eStunning Force",
              List.of("&7Increased knockback.", "&7Chance to root enemies."),
              SkillType.MACES, SkillBonusType.PASSIVE, 3, 1.5, 0.02, Material.IRON_BARS);
        this.loreTemplates = List.of(
                "&7Knockback: &f$knockback%",
                "&7Root Chance: &f$rootChance%",
                "&7Root Duration: &f2 seconds"
        );
        this.formattedBonusTemplate = "$knockback% Knockback, $rootChance% Root";
    }
}
