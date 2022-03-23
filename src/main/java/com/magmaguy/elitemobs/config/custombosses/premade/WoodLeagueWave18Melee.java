package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave18Melee extends CustomBossesConfigFields {
    public WoodLeagueWave18Melee(){
        super("wood_league_wave_18_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Fencing Club Enthusiast",
                "18");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml", "attack_fire.yml"));
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.DIAMOND_SWORD));
    }
}
