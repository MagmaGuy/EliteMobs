package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesSoulSiphonConfig extends SkillBonusConfigFields {
    public HoesSoulSiphonConfig() {
        super("hoes_soul_siphon.yml", true, "&dSoul Siphon",
              List.of("&7Kills grant soul stacks.", "&7Stacks increase damage."),
              SkillType.HOES, SkillBonusType.STACKING, 2, 0.03, 0.0006, Material.SOUL_LANTERN);
        this.loreTemplates = List.of(
                "&7Bonus per Stack: &f+$bonusPerStack%",
                "&7Max Stacks: &f$maxStacks",
                "&7Max Bonus: &f+$maxBonus%",
                "&7Stacks decay after 30s"
        );
        this.formattedBonusTemplate = "+$bonusPerStack% per Soul Stack";
    }
}
