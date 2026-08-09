package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class TridentsImpaleConfig extends SkillBonusConfigFields {
    private static final List<String> LEGACY_LORE = List.of(
            "&7Chance: &f$procChance%",
            "&7Damage Multiplier: &f$multiplierx",
            "&7Ignores armor"
    );
    private static final List<String> LORE = List.of(
            "&7Chance: &f$procChance%",
            "&7Damage Multiplier: &f$multiplierx"
    );

    public TridentsImpaleConfig() {
        super("tridents_impale.yml", true, "&4Impale",
              List.of("&7Chance to impale enemies", "&7for massive bonus damage."),
              SkillType.TRIDENTS, 1, 0.50, 0.01, 0.20, 0.0, Material.TRIDENT, false);
        this.loreTemplates = LORE;
        this.formattedBonusTemplate = "$multiplierx Damage";
    }

    @Override
    protected void migrateLegacyDisplayDefaults() {
        migrateStringListIfExact("loreTemplates", LEGACY_LORE, LORE);
    }
}
