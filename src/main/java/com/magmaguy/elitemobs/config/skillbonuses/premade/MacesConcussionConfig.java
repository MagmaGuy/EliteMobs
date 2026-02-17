package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesConcussionConfig extends SkillBonusConfigFields {
    public MacesConcussionConfig() {
        super("maces_concussion.yml", true, "&eConcussion",
              List.of("&7Chance to daze enemies,", "&7reducing their damage output."),
              SkillType.MACES, 1, 0.15, 0.002, 0.15, Material.IRON_BLOCK);
        this.loreTemplates = List.of(
                "&7Proc Chance: &f$procChance%",
                "&7Duration: &f3 seconds",
                "&7Applies Weakness"
        );
        this.formattedBonusTemplate = "$procChance% Daze Chance";
    }
}
