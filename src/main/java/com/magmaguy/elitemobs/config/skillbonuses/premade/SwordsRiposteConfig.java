package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class SwordsRiposteConfig extends SkillBonusConfigFields {
    public SwordsRiposteConfig() {
        super("swords_riposte.yml", true, "&6Riposte",
              List.of("&7After blocking an attack, your next", "&7attack deals bonus damage."),
              SkillType.SWORDS, 2, 1.5, 0.01, 10.0, Material.SHIELD, true);
    }
}
