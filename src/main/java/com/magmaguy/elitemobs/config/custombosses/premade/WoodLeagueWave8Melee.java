package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave8Melee extends CustomBossesConfigFields {
    public WoodLeagueWave8Melee(){
        super("wood_league_wave_8_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "8");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml"));
        setHelmet(new ItemStack(Material.STICK));
    }
}
