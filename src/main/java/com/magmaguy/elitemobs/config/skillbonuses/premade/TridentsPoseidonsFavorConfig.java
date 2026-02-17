package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class TridentsPoseidonsFavorConfig extends SkillBonusConfigFields {
    public TridentsPoseidonsFavorConfig() {
        super("tridents_poseidons_favor.yml", true, "&1Poseidon's Favor",
              List.of("&7Gain aquatic buffs when", "&7hitting enemies with trident.", "&7(Best in water environments)"),
              SkillType.TRIDENTS, SkillBonusType.PASSIVE, 2, 1.0, 0.02, Material.CONDUIT);
        this.loreTemplates = List.of(
                "&7On hit: &fWater Breathing",
                "&7Dolphin's Grace &f$dolphinLevel",
                "&7Conduit Power",
                "&7Damage Bonus: &f+$damageBonus%"
        );
        this.formattedBonusTemplate = "+$damageBonus% Damage";
    }
}
