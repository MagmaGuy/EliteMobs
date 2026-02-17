package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesGrimReachConfig extends SkillBonusConfigFields {
    public HoesGrimReachConfig() {
        super("hoes_grim_reach.yml", true, "&7Grim Reach",
              List.of("&7Extended attack range", "&7with your scythe."),
              SkillType.HOES, SkillBonusType.PASSIVE, 1, 0.5, 0.01, Material.IRON_HOE);
        this.loreTemplates = List.of(
                "&7Damage Bonus: &f+$bonusPercent%",
                "&7Extended attack range",
                "&7Always active"
        );
        this.formattedBonusTemplate = "+$bonusPercent% Damage";
    }
}
