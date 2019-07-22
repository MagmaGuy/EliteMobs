package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HunterEnchantment extends CustomEnchantment {

    public static String key = "hunter";

    public HunterEnchantment() {
        super(key);
    }

    private static int range = Bukkit.getServer().getViewDistance() * 16;

    public static int getHuntingGearBonus(Entity entity) {

        int huntingGearChanceAdder = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.getWorld().equals(entity.getWorld()) &&
                    (!player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) ||
                            player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) && !player.getMetadata(MetadataHandler.VANISH_NO_PACKET).get(0).asBoolean())) {

                if (player.getLocation().distanceSquared(entity.getLocation()) < range * range) {

                    ItemStack helmet = player.getInventory().getHelmet();
                    ItemStack chestplate = player.getInventory().getChestplate();
                    ItemStack leggings = player.getInventory().getLeggings();
                    ItemStack boots = player.getInventory().getBoots();
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    ItemStack offHandItem = player.getInventory().getItemInOffHand();

                    huntingGearChanceAdder += CustomEnchantment.getCustomEnchantmentLevel(helmet, key);
                    huntingGearChanceAdder += CustomEnchantment.getCustomEnchantmentLevel(leggings, key);
                    huntingGearChanceAdder += CustomEnchantment.getCustomEnchantmentLevel(chestplate, key);
                    huntingGearChanceAdder += CustomEnchantment.getCustomEnchantmentLevel(boots, key);
                    huntingGearChanceAdder += CustomEnchantment.getCustomEnchantmentLevel(heldItem, key);
                    huntingGearChanceAdder += CustomEnchantment.getCustomEnchantmentLevel(offHandItem, key);

                }

            }

        }

        huntingGearChanceAdder = (int) (huntingGearChanceAdder * EnchantmentsConfig.getEnchantment("hunter.yml").getFileConfiguration().getDouble("hunterSpawnBonus"));

        return huntingGearChanceAdder;

    }

}
