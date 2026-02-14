package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class HoesScytheSweepConfig extends SkillBonusConfigFields {
    public HoesScytheSweepConfig() {
        super("hoes_scythe_sweep.yml", true, "&cScythe Sweep",
              List.of("&7Chance to sweep attack", "&7all nearby enemies."),
              SkillType.HOES, 3, 0.50, 0.01, 0.30, 0.0, Material.DIAMOND_HOE, false);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Sweep Damage: &f$sweepPercent% of hit",
                "&7Radius: &f$radius blocks",
                "&7AoE cleave attack"
        );
        this.formattedBonusTemplate = "$sweepPercent% AoE Sweep";
    }
}
