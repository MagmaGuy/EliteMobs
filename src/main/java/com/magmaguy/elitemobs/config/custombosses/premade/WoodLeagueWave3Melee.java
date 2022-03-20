package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave3Melee extends CustomBossesConfigFields {
    public WoodLeagueWave3Melee() {
        super("wood_league_wave_3_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "3");
        setPowers(Arrays.asList("attack_fire.yml"));
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
    }
}
