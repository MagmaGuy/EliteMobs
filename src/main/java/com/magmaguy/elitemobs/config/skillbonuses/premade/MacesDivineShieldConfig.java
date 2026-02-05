package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesDivineShieldConfig extends SkillBonusConfigFields {
    public MacesDivineShieldConfig() {
        super("maces_divine_shield.yml", true, "&eDivine Shield",
              List.of("&7When taking fatal damage,", "&7become invulnerable briefly. 120s CD."),
              SkillType.MACES, 2, 2.0, 0.02, 0.0, 120.0, Material.GOLDEN_CHESTPLATE, true);
    }
}
