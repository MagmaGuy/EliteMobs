package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class StavesBattlemagesGuardConfig extends SkillBonusConfigFields {
    public StavesBattlemagesGuardConfig() {
        super("staves_battlemages_guard.yml", true, "&6Battlemage's Guard",
              List.of("&7Take less damage from nearby", "&7enemies while holding a staff."),
              SkillType.STAVES, SkillBonusType.PASSIVE, 1, 0.1, 0.001, Material.SHIELD);
        this.loreTemplates = List.of(
                "&7Damage taken: &f-$reduction%",
                "&7From enemies within 4 blocks"
        );
        this.formattedBonusTemplate = "-$reduction% damage from nearby enemies";
    }
}
