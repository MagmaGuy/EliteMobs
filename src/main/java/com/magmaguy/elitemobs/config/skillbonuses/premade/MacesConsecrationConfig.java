package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesConsecrationConfig extends SkillBonusConfigFields {
    public MacesConsecrationConfig() {
        super("maces_consecration.yml", true, "&eConsecration",
              List.of("&7Create holy ground that", "&7damages enemies within. 25s CD."),
              SkillType.MACES, 3, 0.5, 0.01, 0.0, 25.0, Material.GLOWSTONE, true);
    }
}
