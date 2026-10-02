package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsEchoBoltConfig extends SkillBonusConfigFields {
    public WandsEchoBoltConfig() {
        super("wands_echo_bolt.yml", true, "&bEcho Bolt",
              List.of("&7Missiles can echo and", "&7strike again."),
              SkillType.WANDS, 3, 1.0, 0.0, 0.1, Material.ECHO_SHARD);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Repeats the hit 0.5s later"
        );
        this.formattedBonusTemplate = "$procChance% echo";
    }
}
