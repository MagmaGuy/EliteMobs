package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class AxesMomentumConfig extends SkillBonusConfigFields {
    public AxesMomentumConfig() {
        super("axes_momentum.yml", true, "&6Momentum",
              List.of("&7Consecutive hits increase", "&7damage. Stacks up to 5 times."),
              SkillType.AXES, SkillBonusType.STACKING, 3, 0.08, 0.002, Material.RABBIT_FOOT);
    }
}
