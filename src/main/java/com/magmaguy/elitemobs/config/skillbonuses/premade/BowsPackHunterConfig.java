package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class BowsPackHunterConfig extends SkillBonusConfigFields {
    private static final List<String> LEGACY_DESCRIPTION =
            List.of("&7Deal bonus damage when", "&7allies are nearby.");
    private static final List<String> DESCRIPTION =
            List.of("&7Deal bonus damage while", "&7another player is within 10 blocks.");
    private static final List<String> LEGACY_LORE = List.of(
            "&7Bonus per Ally: &f$bonusPerAlly%",
            "&7Max Allies: &f3",
            "&7Range: &f$range blocks"
    );
    private static final List<String> LORE = List.of(
            "&7Bonus: &f+$bonusPercent%",
            "&7Requires: &f1 nearby player",
            "&7Range: &f$range blocks"
    );
    private static final String LEGACY_FORMATTED_BONUS = "+$bonusPerAlly% damage per ally";
    private static final String FORMATTED_BONUS = "+$bonusPercent% damage near another player";

    public BowsPackHunterConfig() {
        super("bows_pack_hunter.yml", true, "&6Pack Hunter",
              DESCRIPTION,
              SkillType.BOWS, SkillBonusType.CONDITIONAL, 1, 0.10, 0.002, Material.WOLF_SPAWN_EGG);
        this.loreTemplates = LORE;
        this.formattedBonusTemplate = FORMATTED_BONUS;
    }

    @Override
    protected void migrateLegacyDisplayDefaults() {
        migrateStringListIfExact("description", LEGACY_DESCRIPTION, DESCRIPTION);
        migrateStringListIfExact("loreTemplates", LEGACY_LORE, LORE);
        migrateStringIfExact("formattedBonusTemplate", LEGACY_FORMATTED_BONUS, FORMATTED_BONUS);
    }
}
