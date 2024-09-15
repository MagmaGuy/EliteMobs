package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave21Melee extends CustomBossesConfigFields {
    public WoodLeagueWave21Melee() {
        super("wood_league_wave_21_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "21");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml", "attack_fire.yml")));
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.LEATHER_BOOTS));
        setMainHand(new ItemStack(Material.IRON_SWORD));
    }
}
