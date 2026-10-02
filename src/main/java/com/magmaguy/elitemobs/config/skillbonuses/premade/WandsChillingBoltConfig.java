package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class WandsChillingBoltConfig extends SkillBonusConfigFields {
    public WandsChillingBoltConfig() {
        super("wands_chilling_bolt.yml", true, "&bChilling Bolt",
              List.of("&7Missiles can chill enemies,", "&7slowing them."),
              SkillType.WANDS, 1, 0.0, 0.0, 0.03, Material.PACKED_ICE);
        this.loreTemplates = List.of(
                "&7Chance: &f$procChance%",
                "&7Slowness II for 2s"
        );
        this.formattedBonusTemplate = "$procChance% slow";
    }
}
