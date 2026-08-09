package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsLegionsDisciplineConfig extends SkillBonusConfigFields {
    public SpearsLegionsDisciplineConfig() {
        super("spears_legions_discipline.yml", true, "&bLegion's Discipline",
              List.of("&7Maintain a steady combat pace", "&7to build stacking damage."),
              SkillType.SPEARS, 3, 10, 0.025, 0.00015, Material.IRON_BARS);
        this.loreTemplates = List.of(
                "&7Max Stacks: &f$maxStacks",
                "&7Damage/Stack: &f+$perStack%",
                "&7Max Bonus: &f+$maxBonus%",
                "&7Stacks fade after 2.5s without a hit"
        );
        this.formattedBonusTemplate = "+$maxBonus% Max (Discipline)";
    }
}
