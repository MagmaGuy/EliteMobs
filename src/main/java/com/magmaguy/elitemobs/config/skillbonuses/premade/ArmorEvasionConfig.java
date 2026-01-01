package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class ArmorEvasionConfig extends SkillBonusConfigFields {
    public ArmorEvasionConfig() {
        super("armor_evasion.yml", true, "&fEvasion",
              List.of("&7Chance to completely", "&7dodge incoming attacks."),
              SkillType.ARMOR, 1, 1.0, 0.02, 0.10, 0.0, Material.FEATHER, false);
    }
}
