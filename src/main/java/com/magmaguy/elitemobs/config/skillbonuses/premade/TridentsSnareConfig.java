package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class TridentsSnareConfig extends SkillBonusConfigFields {
    public TridentsSnareConfig() {
        super("tridents_snare.yml", true, "&bSnare",
              List.of("&7Chance to root enemies", "&7in place temporarily."),
              SkillType.TRIDENTS, 1, 1.0, 0.02, 0.25, 0.0, Material.COBWEB, false);
    }
}
