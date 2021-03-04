package com.magmaguy.elitemobs.items.customenchantments;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class IceBreakerEnchantment extends CustomEnchantment {

    public static String key = "ice_breaker";

    public IceBreakerEnchantment() {
        super(key, false);
    }

    private static boolean isValidBlock(Block block) {
        if (block == null)
            return false;
        Material material = block.getType();
        return material.equals(Material.WATER) ||
                material.equals(Material.ICE) ||
                material.equals(Material.BLUE_ICE) ||
                material.equals(Material.FROSTED_ICE) ||
                material.equals(Material.PACKED_ICE);
    }

    private static void freezeBlocks(Block block, ItemStack itemStack) {
        switch (getCustomEnchantmentLevel(itemStack, key)) {
            case 1:
                freezeBlocks1(block, itemStack);
                break;
            case 2:
            default:
                freezeBlocks2(block, itemStack);
                break;
        }
    }

    private static void freezeBlocks1(Block block, ItemStack itemStack) {
        parseBlock(block, 0, 0, 0, itemStack);
    }

    private static void freezeBlocks2(Block block, ItemStack itemStack) {
        parseBlock(block, 0, 0, 0, itemStack);
        parseBlock(block, 1, 0, 0, itemStack);
        parseBlock(block, -1, 0, 0, itemStack);
        parseBlock(block, 0, 1, 0, itemStack);
        parseBlock(block, 0, -1, 0, itemStack);
        parseBlock(block, 0, 0, 1, itemStack);
        parseBlock(block, 0, 0, -1, itemStack);
    }

    private static void parseBlock(Block block, int x, int y, int z, ItemStack itemStack) {
        Block newBlock = block.getLocation().clone().add(new Vector(x, y, z)).getBlock();
        if (!newBlock.getType().equals(Material.WATER))
            return;

        newBlock.setType(Material.ICE);
        itemStack.setDurability((short) (itemStack.getDurability() + 1));
    }

    public static class IceBreakerEnchantmentEvent implements Listener {

        @EventHandler
        public void onInteract(PlayerInteractEvent event) {
            ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
            if (!hasCustomEnchantment(mainHandItem, IceBreakerEnchantment.key)) return;

            if (event.getPlayer().rayTraceBlocks(6, FluidCollisionMode.ALWAYS) == null) return;

            Block block = event.getPlayer().rayTraceBlocks(6, FluidCollisionMode.ALWAYS).getHitBlock();

            if (!isValidBlock(block)) return;

            freezeBlocks(block, mainHandItem);
        }

        @EventHandler
        public void onMine(BlockBreakEvent event) {
            if (event.isCancelled()) return;
            if (!isValidBlock(event.getBlock())) return;
            ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
            if (!hasCustomEnchantment(mainHandItem, IceBreakerEnchantment.key)) return;
            freezeBlocks(event.getBlock(), mainHandItem);
            event.getBlock().setType(Material.AIR);
        }

    }

}
