package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class BowsBarrageConfig extends SkillBonusConfigFields {
    public BowsBarrageConfig() {
        super("bows_barrage.yml", true, "&cBarrage",
              List.of("&7Consecutive hits build up", "&7damage. Stacks up to 5x."),
              SkillType.BOWS, SkillBonusType.STACKING, 3, 0.05, 0.001, Material.ARROW);
    }
}
