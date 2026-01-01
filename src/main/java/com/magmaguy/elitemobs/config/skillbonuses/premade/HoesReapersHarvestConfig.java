package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesReapersHarvestConfig extends SkillBonusConfigFields {
    public HoesReapersHarvestConfig() {
        super("hoes_reapers_harvest.yml", true, "&4Reaper's Harvest",
              List.of("&7Massive damage to enemies", "&7below 25% health."),
              SkillType.HOES, SkillBonusType.CONDITIONAL, 1, 1.0, 0.02, Material.NETHERITE_HOE);
    }
}
