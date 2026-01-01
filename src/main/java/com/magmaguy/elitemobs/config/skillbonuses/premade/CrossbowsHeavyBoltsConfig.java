package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsHeavyBoltsConfig extends SkillBonusConfigFields {
    public CrossbowsHeavyBoltsConfig() {
        super("crossbows_heavy_bolts.yml", true, "&8Heavy Bolts",
              List.of("&7Bolts deal bonus damage", "&7and increased knockback."),
              SkillType.CROSSBOWS, SkillBonusType.PASSIVE, 3, 0.25, 0.005, Material.IRON_INGOT);
    }
}
