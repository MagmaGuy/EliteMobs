package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave13Melee extends CustomBossesConfigFields {
    public WoodLeagueWave13Melee(){
        super("wood_league_wave_13_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "13");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml", "attack_poison.yml"));
        setHelmet(new ItemStack(Material.STICK));
    }
}
