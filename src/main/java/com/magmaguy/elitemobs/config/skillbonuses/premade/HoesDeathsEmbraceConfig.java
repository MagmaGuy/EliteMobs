package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class HoesDeathsEmbraceConfig extends SkillBonusConfigFields {
    public HoesDeathsEmbraceConfig() {
        super("hoes_deaths_embrace.yml", true, "&8Death's Embrace",
              List.of("&7Cheat death and revive", "&7with 10% health."),
              SkillType.HOES, 4, 0.05, 0.001, 0.0, 120.0, Material.TOTEM_OF_UNDYING, true);
        this.loreTemplates = List.of(
                "&7Prevents fatal damage",
                "&7Heals to: &f$healPercent% HP",
                "&7Passive Bonus: &f+$passiveBonus% damage",
                "&7Cooldown: &f$cooldown seconds"
        );
        this.formattedBonusTemplate = "+$passiveBonus% Damage & Death Prevention";
    }
}
