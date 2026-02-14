package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

public class MacesDivineShieldConfig extends SkillBonusConfigFields {

    @Getter
    private String procMessage = "&6&lDivine Shield &esaved you from a fatal blow!";

    public MacesDivineShieldConfig() {
        super("maces_divine_shield.yml", true, "&eDivine Shield",
              List.of("&7When taking fatal damage,", "&7become invulnerable briefly. 120s CD."),
              SkillType.MACES, 2, 2.0, 0.02, 0.0, 120.0, Material.GOLDEN_CHESTPLATE, true);
        this.loreTemplates = List.of(
                "&7Prevents death once",
                "&7Invulnerable for 2 seconds",
                "&7Cooldown: &f$cooldown" + "s"
        );
        this.formattedBonusTemplate = "Prevent Death (CD: $cooldown" + "s)";
    }

    @Override
    public void processAdditionalFields() {
        this.procMessage = translatable(filename, "procMessage",
                processString("procMessage", procMessage, procMessage, true));
    }
}
