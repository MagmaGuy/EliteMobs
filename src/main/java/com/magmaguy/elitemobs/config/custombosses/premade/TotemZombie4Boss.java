package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class TotemZombie4Boss extends CustomBossesConfigFields {
    public TotemZombie4Boss(){
        super("totem_zombie_4", EntityType.ZOMBIE, true, "$eventBossLevel Totem Zombie", "dynamic");
        setMountedEntity("totem_zombie_5.yml");
        setPowers(Arrays.asList("attack_push.yml", "attack_poison.yml", "bonus_loot.yml"));
        setCullReinforcements(false);
        setFollowDistance(100);
        setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        setMainHand(new ItemStack(Material.STONE_SWORD));
        setDamageMultiplier(1.5);
        setHealthMultiplier(1.5);
    }
}
