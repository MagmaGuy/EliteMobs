package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class TridentsRiptideMasteryConfig extends SkillBonusConfigFields {
    public TridentsRiptideMasteryConfig() {
        super("tridents_riptide_mastery.yml", true, "&3Riptide Mastery",
              List.of("&7Bonus damage in water or rain.", "&7Extra bonus when both!"),
              SkillType.TRIDENTS, SkillBonusType.CONDITIONAL, 3, 0.35, 0.007, Material.HEART_OF_THE_SEA);
    }
}
