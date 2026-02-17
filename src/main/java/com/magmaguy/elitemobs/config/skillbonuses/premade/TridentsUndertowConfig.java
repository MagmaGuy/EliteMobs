package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class TridentsUndertowConfig extends SkillBonusConfigFields {
    public TridentsUndertowConfig() {
        super("tridents_undertow.yml", true, "&8Undertow",
              List.of("&7Chance to pull enemies", "&7toward you."),
              SkillType.TRIDENTS, 3, 1.0, 0.02, 0.20, 0.0, Material.FISHING_ROD, false);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Pull Strength: &f$pullStrength",
                "&7Pulls targets toward you"
        );
        this.formattedBonusTemplate = "$pullStrength Pull Strength";
    }
}
