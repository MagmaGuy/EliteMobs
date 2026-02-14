package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class ArmorReactiveShieldingConfig extends SkillBonusConfigFields {
    public ArmorReactiveShieldingConfig() {
        super("armor_reactive_shielding.yml", true, "&bReactive Shielding",
              List.of("&7Big hits activate a shield", "&7that reduces damage. 30s CD."),
              SkillType.ARMOR, 4, 1.0, 0.02, 0.0, 30.0, Material.SHIELD, true);
        this.loreTemplates = List.of(
                "&7Activate damage reduction shield when taking a big hit",
                "&7Triggers on hits dealing 20%+ of max health",
                "&7Shield Reduction: $shieldReduction%",
                "&7Shield Duration: $shieldDuration",
                "&7Cooldown: $cooldown"
        );
        this.formattedBonusTemplate = "$shieldReduction% damage reduction shield (CD: $cooldown)";
    }
}
