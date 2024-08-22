package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TotemZombie3Boss extends CustomBossesConfigFields {
    public TotemZombie3Boss() {
        super("totem_zombie_3", EntityType.ZOMBIE, true, "$eventBossLevel Totem Zombie", "dynamic");
        setMountedEntity("totem_zombie_4.yml");
        setPowers(new ArrayList<>(List.of("attack_lightning.yml", "attack_web.yml", "bonus_loot.yml")));
        setCullReinforcements(false);
        setFollowDistance(100);
        setHelmet(new ItemStack(Material.IRON_HELMET));
        setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        setBoots(new ItemStack(Material.IRON_BOOTS));
        setMainHand(new ItemStack(Material.IRON_SWORD));
        setDamageMultiplier(1.5);
        setHealthMultiplier(1.5);
    }
}
