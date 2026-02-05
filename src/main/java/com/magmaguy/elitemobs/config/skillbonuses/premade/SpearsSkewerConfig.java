package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsSkewerConfig extends SkillBonusConfigFields {
    public SpearsSkewerConfig() {
        super("spears_skewer.yml", true, "&bSkewer",
              List.of("&7Pierce through enemies", "&7in a line. 18s CD."),
              SkillType.SPEARS, 2, 0.8, 0.015, 0.0, 18.0, Material.ARROW, true);
    }
}
