package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class ArmorAdrenalineSurgeConfig extends SkillBonusConfigFields {
    public ArmorAdrenalineSurgeConfig() {
        super("armor_adrenaline_surge.yml", true, "&cAdrenaline Surge",
              List.of("&7Gain combat buffs when", "&7health drops below 30%. 45s CD."),
              SkillType.ARMOR, 2, 1.0, 0.02, 0.0, 45.0, Material.BLAZE_POWDER, true);
        this.loreTemplates = List.of(
                "&7Gain buffs when health drops below 30%",
                "&7Buffs: Speed, Strength, Resistance",
                "&7Duration: $duration",
                "&7Cooldown: $cooldown"
        );
        this.formattedBonusTemplate = "Buffs on low health (CD: $cooldown)";
    }
}
