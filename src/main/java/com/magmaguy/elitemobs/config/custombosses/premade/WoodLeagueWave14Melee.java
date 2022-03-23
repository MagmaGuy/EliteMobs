package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave14Melee extends CustomBossesConfigFields {
    public WoodLeagueWave14Melee(){
        super("wood_league_wave_14_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "14");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml"));
        setHelmet(new ItemStack(Material.STICK));
    }
}
