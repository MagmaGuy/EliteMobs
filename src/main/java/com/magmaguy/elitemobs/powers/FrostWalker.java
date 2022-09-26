package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FrostWalker extends MinorPower {

    public FrostWalker() {
        super(PowersConfig.getPower("frost_walker.yml"));
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack frostWalkerBoots = new ItemStack(Material.LEATHER_BOOTS);
                frostWalkerBoots.addEnchantment(Enchantment.FROST_WALKER, 2);
                frostWalkerBoots.addEnchantment(Enchantment.DEPTH_STRIDER, 3);
                livingEntity.getEquipment().setBoots(frostWalkerBoots);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

}
