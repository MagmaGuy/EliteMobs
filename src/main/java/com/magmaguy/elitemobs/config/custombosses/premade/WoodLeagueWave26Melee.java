package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave26Melee extends CustomBossesConfigFields {
    public WoodLeagueWave26Melee(){
        super("wood_league_wave_26_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "26");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml", "attack_fire.yml"));
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.LEATHER_BOOTS));
        setMainHand(new ItemStack(Material.IRON_SWORD));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
