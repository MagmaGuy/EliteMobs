package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsArrowRainConfig extends SkillBonusConfigFields {
    public CrossbowsArrowRainConfig() {
        super("crossbows_arrow_rain.yml", true, "&9Arrow Rain",
              List.of("&7Rain arrows on your target.", "&730s cooldown."),
              SkillType.CROSSBOWS, 4, 1.0, 0.02, 0.0, 30.0, Material.CROSSBOW, true);
        this.loreTemplates = List.of(
                "&7Arrow Damage: &f$arrowDamage%",
                "&7Arrows: &f15 total (3x5 waves)",
                "&7Cooldown: &f$cooldown" + "s"
        );
        this.formattedBonusTemplate = "$arrowDamage% arrow damage";
    }
}
