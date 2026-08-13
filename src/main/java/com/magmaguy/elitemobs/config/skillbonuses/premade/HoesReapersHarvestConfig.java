package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesReapersHarvestConfig extends SkillBonusConfigFields {
    private static final String LEGACY_FORMATTED_BONUS = "+$bonusPercent% Execute Damage";
    private static final String FORMATTED_BONUS = "+$bonusPercent% Damage vs Targets Below 25% HP";

    public HoesReapersHarvestConfig() {
        super("hoes_reapers_harvest.yml", true, "&4Reaper's Harvest",
              List.of("&7Massive damage to enemies", "&7below 25% health."),
              SkillType.HOES, SkillBonusType.CONDITIONAL, 1, 1.0, 0.02, Material.NETHERITE_HOE);
        this.loreTemplates = List.of(
                "&7Bonus Damage: &f+$bonusPercent%",
                "&7Condition: Target below 25% HP",
                "&7Execute weakened foes"
        );
        this.formattedBonusTemplate = FORMATTED_BONUS;
    }

    @Override
    protected void migrateLegacyDisplayDefaults() {
        migrateStringIfExact("formattedBonusTemplate", LEGACY_FORMATTED_BONUS, FORMATTED_BONUS);
    }
}
