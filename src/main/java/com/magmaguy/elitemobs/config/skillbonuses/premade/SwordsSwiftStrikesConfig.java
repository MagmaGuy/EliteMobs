package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class SwordsSwiftStrikesConfig extends SkillBonusConfigFields {
    public SwordsSwiftStrikesConfig() {
        super("swords_swift_strikes.yml", true, "&bSwift Strikes",
              List.of("&7Move faster while wielding a sword."),
              SkillType.SWORDS, SkillBonusType.PASSIVE, 1, 0.05, 0.001, Material.FEATHER);
        this.loreTemplates = List.of(
                "&7Movement Speed: &f+$value%",
                "&7Active while holding a sword"
        );
        this.formattedBonusTemplate = "+$value% Movement Speed";
    }
}
