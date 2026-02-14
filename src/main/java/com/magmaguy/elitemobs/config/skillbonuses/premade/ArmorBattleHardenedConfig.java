package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class ArmorBattleHardenedConfig extends SkillBonusConfigFields {
    public ArmorBattleHardenedConfig() {
        super("armor_battle_hardened.yml", true, "&6Battle Hardened",
              List.of("&7Passive damage reduction", "&7from all sources."),
              SkillType.ARMOR, SkillBonusType.PASSIVE, 1, 0.20, 0.004, Material.NETHERITE_CHESTPLATE);
        this.loreTemplates = List.of(
                "&7Passive damage reduction from all sources",
                "&7Damage Reduction: $value%"
        );
        this.formattedBonusTemplate = "$value% Damage Reduction";
    }
}
