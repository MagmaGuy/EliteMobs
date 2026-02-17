package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class BowsRicochetConfig extends SkillBonusConfigFields {
    public BowsRicochetConfig() {
        super("bows_ricochet.yml", true, "&aRicochet",
              List.of("&7Arrows can bounce to", "&7hit nearby enemies."),
              SkillType.BOWS, 3, 0.50, 0.01, 0.15, 0.0, Material.SLIME_BALL, false);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$procChance%",
                "&7Ricochet Damage: &f$ricochetDamage%",
                "&7Range: &f$range blocks"
        );
        this.formattedBonusTemplate = "$ricochetDamage% ricochet damage";
    }
}
