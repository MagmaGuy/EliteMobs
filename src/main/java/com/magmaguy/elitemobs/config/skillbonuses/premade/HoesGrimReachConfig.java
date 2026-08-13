package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesGrimReachConfig extends SkillBonusConfigFields {
    private static final List<String> LEGACY_DESCRIPTION =
            List.of("&7Extended attack range", "&7with your scythe.");
    private static final List<String> DESCRIPTION =
            List.of("&7Deal bonus damage and extend", "&7your scythe attack range.");
    private static final List<String> LEGACY_LORE = List.of(
            "&7Damage Bonus: &f+$bonusPercent%",
            "&7Extended attack range",
            "&7Always active"
    );
    private static final List<String> LORE = List.of(
            "&7Damage Bonus: &f+$bonusPercent%",
            "&7Reach Bonus: &f+$reachBlocks blocks",
            "&7Always active"
    );
    private static final String LEGACY_FORMATTED_BONUS = "+$bonusPercent% Damage";
    private static final String FORMATTED_BONUS = "+$bonusPercent% Damage, +$reachBlocks Blocks Reach";

    public HoesGrimReachConfig() {
        super("hoes_grim_reach.yml", true, "&7Grim Reach",
              DESCRIPTION,
              SkillType.HOES, SkillBonusType.PASSIVE, 1, 0.5, 0.01, Material.IRON_HOE);
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
