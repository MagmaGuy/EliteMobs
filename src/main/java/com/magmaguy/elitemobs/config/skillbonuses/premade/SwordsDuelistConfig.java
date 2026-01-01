package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SwordsDuelistConfig extends SkillBonusConfigFields {
    public SwordsDuelistConfig() {
        super("swords_duelist.yml", true, "&6Duelist",
              List.of("&7Deal bonus damage when fighting", "&7a single enemy (no others nearby)."),
              SkillType.SWORDS, SkillBonusType.CONDITIONAL, 3, 0.25, 0.005, Material.GOLDEN_SWORD);
    }
}
