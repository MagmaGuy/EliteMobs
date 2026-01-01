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
    }
}
