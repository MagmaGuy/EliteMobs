package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class ArmorLastStandConfig extends SkillBonusConfigFields {
    public ArmorLastStandConfig() {
        super("armor_last_stand.yml", true, "&4Last Stand",
              List.of("&7Survive fatal damage with", "&71 heart. 120s cooldown."),
              SkillType.ARMOR, 4, 1.0, 0.02, 0.0, 120.0, Material.TOTEM_OF_UNDYING, true);
        this.loreTemplates = List.of(
                "&7Survive a fatal blow once per cooldown",
                "&7Prevents death and restores you to 1 HP",
                "&7Cooldown: $cooldown"
        );
        this.formattedBonusTemplate = "Prevent death (CD: $cooldown)";
    }
}
