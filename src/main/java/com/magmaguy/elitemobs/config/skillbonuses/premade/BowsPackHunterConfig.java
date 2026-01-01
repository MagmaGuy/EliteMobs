package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class BowsPackHunterConfig extends SkillBonusConfigFields {
    public BowsPackHunterConfig() {
        super("bows_pack_hunter.yml", true, "&6Pack Hunter",
              List.of("&7Deal bonus damage when", "&7allies are nearby."),
              SkillType.BOWS, SkillBonusType.CONDITIONAL, 1, 0.10, 0.002, Material.WOLF_SPAWN_EGG);
    }
}
