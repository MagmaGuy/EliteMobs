package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class CrossbowsQuickReloadConfig extends SkillBonusConfigFields {
    private static final List<String> LEGACY_DESCRIPTION =
            List.of("&7Gain haste after hitting", "&7for faster reloading.");
    private static final List<String> DESCRIPTION =
            List.of("&7Successful hits briefly", "&7increase movement speed.");
    private static final List<String> LEGACY_LORE = List.of(
            "&7Haste Level: &f$hasteLevel",
            "&7Duration: &f3 seconds",
            "&7Triggers on hit"
    );
    private static final List<String> LORE = List.of(
            "&7Movement speed: &f+$speedPercent%",
            "&7Duration: &f3 seconds",
            "&7Triggers on hit"
    );
    private static final String LEGACY_FORMATTED_BONUS = "Haste $hasteLevel on hit";
    private static final String FORMATTED_BONUS = "+$speedPercent% movement speed on hit";

    public CrossbowsQuickReloadConfig() {
        super("crossbows_quick_reload.yml", true, "&eQuick Reload",
              DESCRIPTION,
              SkillType.CROSSBOWS, SkillBonusType.PASSIVE, 1, 0.5, 0.01, Material.SUGAR);
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
