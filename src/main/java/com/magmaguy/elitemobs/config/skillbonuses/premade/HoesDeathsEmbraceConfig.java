package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class HoesDeathsEmbraceConfig extends SkillBonusConfigFields {
    public HoesDeathsEmbraceConfig() {
        super("hoes_deaths_embrace.yml", true, "&8Death's Embrace",
              List.of("&7Cheat death once and", "&7revive with 20% health. 60s CD."),
              SkillType.HOES, 4, 1.0, 0.02, 0.0, 60.0, Material.TOTEM_OF_UNDYING, true);
        this.loreTemplates = List.of(
                "&7Prevents fatal damage",
                "&7Heals to: &f$healPercent% HP",
                "&7Passive Bonus: &f+$passiveBonus% damage",
                "&7Cooldown: &f$cooldown seconds"
        );
        this.formattedBonusTemplate = "+$passiveBonus% Damage & Death Prevention";
    }
}
