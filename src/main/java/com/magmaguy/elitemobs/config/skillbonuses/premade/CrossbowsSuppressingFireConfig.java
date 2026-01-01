package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsSuppressingFireConfig extends SkillBonusConfigFields {
    public CrossbowsSuppressingFireConfig() {
        super("crossbows_suppressing_fire.yml", true, "&7Suppressing Fire",
              List.of("&7Chance to weaken and slow", "&7enemies with your bolts."),
              SkillType.CROSSBOWS, 3, 1.0, 0.02, 0.20, 0.0, Material.COBWEB, false);
    }
}
