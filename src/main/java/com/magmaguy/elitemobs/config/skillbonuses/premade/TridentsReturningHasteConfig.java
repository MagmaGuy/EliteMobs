package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class TridentsReturningHasteConfig extends SkillBonusConfigFields {
    public TridentsReturningHasteConfig() {
        super("tridents_returning_haste.yml", true, "&aReturning Haste",
              List.of("&7Consecutive throws increase", "&7damage. Stacks up to 5x."),
              SkillType.TRIDENTS, SkillBonusType.STACKING, 3, 0.06, 0.0012, Material.ENDER_PEARL);
    }
}
