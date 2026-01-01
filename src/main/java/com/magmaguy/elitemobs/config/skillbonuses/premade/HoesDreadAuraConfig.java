package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Material;

import java.util.List;

public class HoesDreadAuraConfig extends SkillBonusConfigFields {
    public HoesDreadAuraConfig() {
        super("hoes_dread_aura.yml", true, "&5Dread Aura",
              List.of("&7Toggle: Emit an aura that", "&7weakens and slows nearby enemies."),
              SkillType.HOES, SkillBonusType.TOGGLE, 3, 1.0, 0.02, Material.SCULK_SHRIEKER);
    }
}
