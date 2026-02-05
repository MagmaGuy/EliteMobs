package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class MacesAvatarOfJudgmentConfig extends SkillBonusConfigFields {
    public MacesAvatarOfJudgmentConfig() {
        super("maces_avatar_of_judgment.yml", true, "&e&lAvatar of Judgment",
              List.of("&7Become an avatar of divine wrath.", "&7Massive damage boost for 10s. 90s CD."),
              SkillType.MACES, 4, 2.0, 0.03, 0.0, 90.0, Material.NETHER_STAR, true);
    }
}
