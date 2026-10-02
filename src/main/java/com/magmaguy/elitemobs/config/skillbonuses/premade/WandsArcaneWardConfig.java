package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class WandsArcaneWardConfig extends SkillBonusConfigFields {
    public WandsArcaneWardConfig() {
        super("wands_arcane_ward.yml", true, "&dArcane Ward",
              List.of("&7Take less damage from projectiles", "&7while holding a wand."),
              SkillType.WANDS, SkillBonusType.PASSIVE, 1, 0.1, 0.001, Material.AMETHYST_SHARD);
        this.loreTemplates = List.of(
                "&7Projectile damage taken: &f-$reduction%"
        );
        this.formattedBonusTemplate = "-$reduction% projectile damage";
    }
}
