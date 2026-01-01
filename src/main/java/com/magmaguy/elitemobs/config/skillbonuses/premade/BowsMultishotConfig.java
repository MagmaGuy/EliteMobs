package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class BowsMultishotConfig extends SkillBonusConfigFields {
    public BowsMultishotConfig() {
        super("bows_multishot.yml", true, "&dMultishot",
              List.of("&7Chance to fire additional", "&7arrows on each shot."),
              SkillType.BOWS, 2, 0.0, 0.0, 0.20, 0.0, Material.SPECTRAL_ARROW, false);
    }
}
