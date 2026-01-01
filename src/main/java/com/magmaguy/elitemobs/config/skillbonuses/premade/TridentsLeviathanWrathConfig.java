package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class TridentsLeviathanWrathConfig extends SkillBonusConfigFields {
    public TridentsLeviathanWrathConfig() {
        super("tridents_leviathan_wrath.yml", true, "&5Leviathan's Wrath",
              List.of("&7Unleash devastating AOE attack", "&7with lightning. 60s CD."),
              SkillType.TRIDENTS, 4, 1.5, 0.03, 0.0, 60.0, Material.NAUTILUS_SHELL, true);
    }
}
