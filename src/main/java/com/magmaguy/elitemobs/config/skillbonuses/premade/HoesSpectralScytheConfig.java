package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;

import java.util.List;

public class HoesSpectralScytheConfig extends SkillBonusConfigFields {
    public HoesSpectralScytheConfig() {
        super("hoes_spectral_scythe.yml", true, "&bSpectral Scythe",
              List.of("&7Launch a spectral scythe", "&7projectile. 25s CD."),
              SkillType.HOES, 4, 1.5, 0.03, 0.0, 25.0, Material.PHANTOM_MEMBRANE, true);
    }
}
