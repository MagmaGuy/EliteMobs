package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class BowsOverdrawConfig extends SkillBonusConfigFields {
    public BowsOverdrawConfig() {
        super("bows_overdraw.yml", true, "&cOverdraw",
              List.of("&7Hold your bow longer for", "&7increased damage."),
              SkillType.BOWS, SkillBonusType.CONDITIONAL, 2, 0.25, 0.005, Material.BOW);
        this.loreTemplates = List.of(
                "&7Base Bonus: &f$baseBonus%",
                "&7Max Overdraw: &f3x bonus",
                "&7Requires full draw"
        );
        this.formattedBonusTemplate = "+$baseBonus% damage (overdraw)";
    }
}
