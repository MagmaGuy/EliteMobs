package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsQuickReloadConfig extends SkillBonusConfigFields {
    public CrossbowsQuickReloadConfig() {
        super("crossbows_quick_reload.yml", true, "&eQuick Reload",
              List.of("&7Successful hits briefly", "&7increase movement speed."),
              SkillType.CROSSBOWS, SkillBonusType.PASSIVE, 1, 0.5, 0.01, Material.SUGAR);
        this.loreTemplates = List.of(
                "&7Movement speed: &f+$speedPercent%",
                "&7Duration: &f3 seconds",
                "&7Triggers on hit"
        );
        this.formattedBonusTemplate = "+$speedPercent% movement speed on hit";
    }
}
