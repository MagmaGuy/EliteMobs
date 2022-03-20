package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave4Melee extends CustomBossesConfigFields {
    public WoodLeagueWave4Melee(){
        super("wood_league_wave_4_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "4");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
    }
}
