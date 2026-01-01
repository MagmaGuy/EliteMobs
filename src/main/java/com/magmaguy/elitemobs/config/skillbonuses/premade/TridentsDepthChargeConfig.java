package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class TridentsDepthChargeConfig extends SkillBonusConfigFields {
    public TridentsDepthChargeConfig() {
        super("tridents_depth_charge.yml", true, "&9Depth Charge",
              List.of("&7Bonus damage and AOE against", "&7enemies in water."),
              SkillType.TRIDENTS, SkillBonusType.CONDITIONAL, 4, 0.50, 0.01, Material.PRISMARINE_SHARD);
    }
}
