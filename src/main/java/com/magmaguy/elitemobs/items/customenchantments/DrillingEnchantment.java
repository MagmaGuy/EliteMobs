package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class DrillingEnchantment extends CustomEnchantment implements Listener {

    public static String key = "drilling";

    public DrillingEnchantment() {
        super(key, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDig(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().isSneaking()) return;
        if (event.getPlayer().getInventory().getItemInMainHand() == null ||
                !event.getPlayer().getInventory().getItemInMainHand().hasItemMeta() ||
                event.getPlayer().getInventory().getItemInMainHand().getItemMeta() == null) return;
        if (!ItemTagger.hasEnchantment(event.getPlayer().getInventory().getItemInMainHand().getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, key)))
            return;


        drillBlocks(event.getBlock(),
                ItemTagger.getEnchantment(event.getPlayer().getInventory().getItemInMainHand().getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, key)),
                event.getPlayer().getLocation(),
                event.getPlayer().getInventory().getItemInMainHand(),
                event.getPlayer());

    }

    private enum MiningDirection {
        UP,
        DOWN,
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    private Material material = null;
    private ItemStack itemStack = null;
    private MiningDirection miningDirection = null;
    private Player player;

    private void drillBlocks(Block originalBlock, int enchantmentLevel, Location playerLocation, ItemStack playerItem, Player player) {

        this.player = player;
        this.material = originalBlock.getType();
        this.itemStack = playerItem;
        this.miningDirection = determineDirection(originalBlock.getLocation(), playerLocation);

        switch (enchantmentLevel) {
            case 1:
                drillLevel1(originalBlock);
                break;
            case 2:
                drillLevel2(originalBlock);
                break;
            case 3:
                drillLevel3(originalBlock);
                drillLevel2(drillLevel1(originalBlock));
                break;
            case 4:
                drillLevel3(originalBlock);
                drillLevel3(drillLevel1(originalBlock));
                drillLevel2(drillLevel1(drillLevel1(originalBlock)));
                break;
            case 5:
            default:
                drillLevel3(originalBlock);
                drillLevel3(drillLevel1(originalBlock));
                drillLevel3(drillLevel1(drillLevel1(originalBlock)));
        }

    }

    private MiningDirection determineDirection(Location blockLocation, Location playerLocation) {

        Location adjustedPlayerLocation = playerLocation.clone().add(new Vector(0, 1, 0));
        Location adjustedBlockLocation = blockLocation.clone().add(new Vector(0.5, 0.5, 0.5));

        Vector directionVector = adjustedBlockLocation.clone().subtract(adjustedPlayerLocation).toVector().normalize();

        double x = directionVector.getX();
        double y = directionVector.getY();
        double z = directionVector.getZ();

        if (Math.abs(y) > 0.9) {
            if (y > 0)
                return MiningDirection.UP;
            else
                return MiningDirection.DOWN;
        }

        if (Math.abs(x) > Math.abs(z)) {
            if (x > 0)
                return MiningDirection.EAST;
            else
                return MiningDirection.WEST;
        }

        if (z > 0)
            return MiningDirection.NORTH;

        return MiningDirection.SOUTH;

    }

    private Block processBlock(Block originalBlock, Vector addedVector) {
        Block finalBlock = originalBlock.getWorld().getBlockAt(originalBlock.getLocation().clone().add(addedVector));
        if (!this.material.equals(finalBlock.getType())) return finalBlock;
        finalBlock.breakNaturally(this.itemStack);

        ItemMeta itemMeta = itemStack.getItemMeta();
        Damageable damageable = (Damageable) itemMeta;

        if (itemStack.getItemMeta().hasEnchant(Enchantment.DURABILITY))
            if (itemStack.getItemMeta().getEnchantLevel(Enchantment.DURABILITY) / 20D > ThreadLocalRandom.current().nextDouble())
                damageable.setDamage(damageable.getDamage() + 1);
        itemStack.setItemMeta(itemMeta);
        if (itemStack.getType().getMaxDurability() < damageable.getDamage())
            player.getInventory().setItemInMainHand(null);
        return finalBlock;
    }

    private Block drillLevel1(Block originalBlock) {

        switch (miningDirection) {
            case NORTH:
                return processBlock(originalBlock, new Vector(0, 0, 1));
            case SOUTH:
                return processBlock(originalBlock, new Vector(0, 0, -1));
            case EAST:
                return processBlock(originalBlock, new Vector(1, 0, 0));
            case WEST:
                return processBlock(originalBlock, new Vector(-1, 0, 0));
            case UP:
                return processBlock(originalBlock, new Vector(0, 1, 0));
            case DOWN:
                return processBlock(originalBlock, new Vector(0, -1, 0));
            default:
                return null;
        }

    }

    private void drillLevel2(Block originalBlock) {

        switch (miningDirection) {
            case NORTH:
            case SOUTH:
                processBlock(originalBlock, new Vector(0, 1, 0));
                processBlock(originalBlock, new Vector(0, -1, 0));
                processBlock(originalBlock, new Vector(1, 0, 0));
                processBlock(originalBlock, new Vector(-1, 0, 0));
                break;
            case EAST:
            case WEST:
                processBlock(originalBlock, new Vector(0, 1, 0));
                processBlock(originalBlock, new Vector(0, -1, 0));
                processBlock(originalBlock, new Vector(0, 0, 1));
                processBlock(originalBlock, new Vector(0, 0, -1));
                break;
            case UP:
            case DOWN:
                processBlock(originalBlock, new Vector(1, 0, 0));
                processBlock(originalBlock, new Vector(-1, 0, 0));
                processBlock(originalBlock, new Vector(0, 0, 1));
                processBlock(originalBlock, new Vector(0, 0, -1));
                break;
        }

    }

    private void drillLevel3(Block originalBlock) {

        switch (miningDirection) {
            case NORTH:
            case SOUTH:
                for (int x = -1; x < 2; x++)
                    for (int y = -1; y < 2; y++)
                        if (!(x == 0 && y == 0))
                            processBlock(originalBlock, new Vector(x, y, 0));
                break;
            case EAST:
            case WEST:
                for (int y = -1; y < 2; y++)
                    for (int z = -1; z < 2; z++)
                        if (!(y == 0 && z == 0))
                            processBlock(originalBlock, new Vector(0, y, z));
                break;
            case UP:
            case DOWN:
                for (int x = -1; x < 2; x++)
                    for (int z = -1; z < 2; z++)
                        if (!(x == 0 && z == 0))
                            processBlock(originalBlock, new Vector(x, 0, z));
                break;
        }

    }

}
