package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsSpellMirrorConfig extends SkillBonusConfigFields {
    public WandsSpellMirrorConfig() {
        super("wands_spell_mirror.yml", true, "&dSpell Mirror",
              List.of("&7Negate the next hit every 15", "&7seconds and answer it."),
              SkillType.WANDS, 4, 2.0, 0.0, 15.0, Material.GLASS, true);
        this.loreTemplates = List.of(
                "&7Negates one hit",
                "&7Counter: &f$counterPercent% of the hit",
                "&7Cooldown: &f$cooldown seconds"
        );
        this.formattedBonusTemplate = "Negate a hit every $cooldowns";
    }
}
