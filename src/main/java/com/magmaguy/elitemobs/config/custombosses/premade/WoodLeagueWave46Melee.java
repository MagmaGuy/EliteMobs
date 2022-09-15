package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave46Melee extends CustomBossesConfigFields {
    public WoodLeagueWave46Melee() {
        super("wood_league_wave_46_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "46");
        setFollowDistance(60);
        setPowers(Arrays.asList("corpse.yml", "attack_fire.yml", "invulnerability_arrow.yml"));
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.DIAMOND_SWORD));
        setMovementSpeedAttribute(0.3D);
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
