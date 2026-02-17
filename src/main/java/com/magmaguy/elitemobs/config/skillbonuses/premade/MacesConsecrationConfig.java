package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesConsecrationConfig extends SkillBonusConfigFields {
    public MacesConsecrationConfig() {
        super("maces_consecration.yml", true, "&eConsecration",
              List.of("&7Create holy ground that", "&7damages enemies within. 25s CD."),
              SkillType.MACES, 3, 0.5, 0.01, 0.0, 25.0, Material.GLOWSTONE, true);
        this.loreTemplates = List.of(
                "&7Damage/Second: &f$damagePerSecond",
                "&7Radius: &f$radius blocks",
                "&7Duration: &f5 seconds",
                "&7Cooldown: &f$cooldown" + "s"
        );
        this.formattedBonusTemplate = "$damagePerSecond DPS Zone (CD: $cooldown" + "s)";
    }
}
