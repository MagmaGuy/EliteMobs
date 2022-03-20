package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave1Melee extends CustomBossesConfigFields {
    public WoodLeagueWave1Melee() {
        super("wood_league_wave_1_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "1");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
    }
}
