package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave12Melee extends CustomBossesConfigFields {
    public WoodLeagueWave12Melee(){
        super("wood_league_wave_12_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "12");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml"));
        setHelmet(new ItemStack(Material.STICK));
    }
}
