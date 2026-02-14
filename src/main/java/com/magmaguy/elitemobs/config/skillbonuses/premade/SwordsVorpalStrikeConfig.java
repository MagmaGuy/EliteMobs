package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SwordsVorpalStrikeConfig extends SkillBonusConfigFields {
    public SwordsVorpalStrikeConfig() {
        super("swords_vorpal_strike.yml", true, "&4Vorpal Strike",
              List.of("&7Critical hits can deal", "&7devastating bonus damage."),
              SkillType.SWORDS, 4, 3.0, 0.02, 30.0, Material.NETHERITE_SWORD, true);
        this.loreTemplates = List.of(
                "&7Damage Multiplier: &f$multiplierx",
                "&7Cooldown: &f$cooldown" + "s",
                "&7Only triggers on critical hits"
        );
        this.formattedBonusTemplate = "$multiplierx Critical Damage";
    }
}
