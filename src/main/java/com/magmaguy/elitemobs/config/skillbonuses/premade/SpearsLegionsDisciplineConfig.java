package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SpearsLegionsDisciplineConfig extends SkillBonusConfigFields {
    public SpearsLegionsDisciplineConfig() {
        super("spears_legions_discipline.yml", true, "&bLegion's Discipline",
              List.of("&7Consecutive hits increase", "&7damage. Resets on miss."),
              SkillType.SPEARS, 3, 10, 0.03, 0.005, Material.IRON_BARS);
        this.loreTemplates = List.of(
                "&7Max Stacks: &f$maxStacks",
                "&7Damage/Stack: &f+$perStack%",
                "&7Max Bonus: &f+$maxBonus%",
                "&7Resets on miss"
        );
        this.formattedBonusTemplate = "+$maxBonus% Max (Discipline)";
    }
}
