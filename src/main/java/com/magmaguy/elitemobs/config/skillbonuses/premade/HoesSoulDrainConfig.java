package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class HoesSoulDrainConfig extends SkillBonusConfigFields {
    public HoesSoulDrainConfig() {
        super("hoes_soul_drain.yml", true, "&5Soul Drain",
              List.of("&7Chance to drain health", "&7from enemies on hit."),
              SkillType.HOES, 1, 1.0, 0.02, 0.20, 0.0, Material.SOUL_SAND, false);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Drain: &f$drainPercent% of damage",
                "&7Heals you for damage dealt"
        );
        this.formattedBonusTemplate = "$drainPercent% Life Drain";
    }
}
