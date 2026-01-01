package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class ArmorFortifyConfig extends SkillBonusConfigFields {
    public ArmorFortifyConfig() {
        super("armor_fortify.yml", true, "&9Fortify",
              List.of("&7Taking damage builds stacks", "&7that reduce future damage."),
              SkillType.ARMOR, SkillBonusType.STACKING, 3, 1.0, 0.02, Material.DIAMOND_CHESTPLATE);
    }
}
