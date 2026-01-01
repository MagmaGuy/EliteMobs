package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesReapWhatYouSowConfig extends SkillBonusConfigFields {
    public HoesReapWhatYouSowConfig() {
        super("hoes_reap_what_you_sow.yml", true, "&6Reap What You Sow",
              List.of("&7Deal more damage when", "&7your health is low."),
              SkillType.HOES, SkillBonusType.CONDITIONAL, 3, 0.50, 0.01, Material.GOLDEN_HOE);
    }
}
