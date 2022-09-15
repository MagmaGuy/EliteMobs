package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WoodLeagueWave16Melee extends CustomBossesConfigFields {
    public WoodLeagueWave16Melee() {
        super("wood_league_wave_16_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Mr. Oinkers Fan",
                "16");
        setFollowDistance(60);
        setPowers(List.of("corpse.yml"));
        setHelmet(new ItemStack(Material.STICK));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
