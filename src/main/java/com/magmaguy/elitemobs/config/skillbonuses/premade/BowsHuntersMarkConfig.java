package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class BowsHuntersMarkConfig extends SkillBonusConfigFields {
    public BowsHuntersMarkConfig() {
        super("bows_hunters_mark.yml", true, "&eHunter's Mark",
              List.of("&7Mark enemies with your arrows.", "&7Marked targets take bonus damage."),
              SkillType.BOWS, 2, 0.20, 0.004, 0.25, 0.0, Material.GLOW_INK_SAC, false);
    }
}
