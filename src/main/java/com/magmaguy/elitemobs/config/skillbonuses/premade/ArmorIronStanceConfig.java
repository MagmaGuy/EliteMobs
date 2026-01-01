package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class ArmorIronStanceConfig extends SkillBonusConfigFields {
    public ArmorIronStanceConfig() {
        super("armor_iron_stance.yml", true, "&7Iron Stance",
              List.of("&7Take less damage while", "&7standing still."),
              SkillType.ARMOR, SkillBonusType.CONDITIONAL, 1, 0.30, 0.006, Material.IRON_CHESTPLATE);
    }
}
