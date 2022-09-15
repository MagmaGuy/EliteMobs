package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave33Melee extends CustomBossesConfigFields {
    public WoodLeagueWave33Melee() {
        super("wood_league_wave_33_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "33");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml", "attack_fire.yml", "invulnerability_arrow.yml", "attack_poison.yml"));
        setHelmet(new ItemStack(Material.IRON_HELMET));
        setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        setBoots(new ItemStack(Material.IRON_BOOTS));
        setMainHand(new ItemStack(Material.GOLDEN_SWORD));
    }
}
