package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TotemZombie5Boss extends CustomBossesConfigFields {
    public TotemZombie5Boss() {
        super("totem_zombie_5", EntityType.ZOMBIE, true, "$eventBossLevel Totem Zombie", "dynamic");
        setMountedEntity("totem_zombie_6.yml");
        setPowers(new ArrayList<>(List.of("attack_gravity.yml", "attack_blinding.yml", "bonus_loot.yml")));
        setCullReinforcements(false);
        setFollowDistance(100);
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.LEATHER_BOOTS));
        setMainHand(new ItemStack(Material.WOODEN_SWORD));
        setDamageMultiplier(1.5);
        setHealthMultiplier(1.5);
    }
}
