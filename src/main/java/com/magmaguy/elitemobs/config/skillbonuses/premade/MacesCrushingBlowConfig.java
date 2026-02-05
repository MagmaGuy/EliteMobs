package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesCrushingBlowConfig extends SkillBonusConfigFields {
    public MacesCrushingBlowConfig() {
        super("maces_crushing_blow.yml", true, "&eCrushing Blow",
              List.of("&7Chance to ignore enemy", "&7armor on hit."),
              SkillType.MACES, 1, 0.25, 0.003, 0.12, Material.ANVIL);
    }
}
