package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TotemZombie2Boss extends CustomBossesConfigFields {
    public TotemZombie2Boss() {
        super("totem_zombie_2", EntityType.ZOMBIE, true, "$eventBossLevel Totem Zombie", "dynamic");
        setMountedEntity("totem_zombie_3.yml");
        setPowers(new ArrayList<>(List.of("arrow_rain.yml", "attack_arrow.yml", "bonus_loot.yml")));
        setCullReinforcements(false);
        setFollowDistance(100);
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.DIAMOND_SWORD));
        setDamageMultiplier(1.5);
        setHealthMultiplier(1.5);
    }
}
