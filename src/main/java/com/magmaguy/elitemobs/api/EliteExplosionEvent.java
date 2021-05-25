package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class EliteExplosionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final EliteMobEntity eliteMobEntity;
    private final Location explosionSourceLocation;
    private boolean isCancelled = false;
    private final ArrayList<BlockState> blockStates;
    private final ElitePower elitePower;

    public EliteExplosionEvent(EliteMobEntity eliteMobEntity,
                               ElitePower elitePower,
                               Location explosionSourceLocation,
                               ArrayList<BlockState> blockStates) {
        this.eliteMobEntity = eliteMobEntity;
        this.explosionSourceLocation = explosionSourceLocation;
        this.elitePower = elitePower;
        this.blockStates = blockStates;
    }

    public EliteMobEntity getEliteMobEntity() {
        return eliteMobEntity;
    }

    public Location getExplosionSourceLocation() {
        return explosionSourceLocation;
    }

    public ElitePower getElitePower() {
        return elitePower;
    }

    public ArrayList<BlockState> getBlockStates() {
        return blockStates;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void visualExplosionEffect() {
        for (BlockState blockState : blockStates) {
            FallingBlock fallingBlock = blockState.getBlock().getWorld().spawnFallingBlock(blockState.getLocation().add(new Vector(0,0.2,0)), blockState.getBlockData());
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(true);
            fallingBlock.setVelocity(fallingBlock.getLocation().clone().subtract(explosionSourceLocation).toVector().normalize().setY(1).multiply(0.5));
            CrashFix.persistentTracker(fallingBlock);
            fallingBlocks.add(fallingBlock);
        }
    }

    public static ArrayList<FallingBlock> fallingBlocks = new ArrayList<>();

    public static class EliteExplosionEvents implements Listener {


        @EventHandler(ignoreCancelled = true)
        public void onBlockDrop(EntityChangeBlockEvent event) {

            if (fallingBlocks.isEmpty()) return;
            if (!event.getEntity().getType().equals(EntityType.FALLING_BLOCK)) return;

            boolean wasFallingBlock = false;
            for (FallingBlock fallingBlock : fallingBlocks)
                if (fallingBlock.equals(event.getEntity())) {
                    event.setCancelled(true);
                    wasFallingBlock = true;
                    break;
                }

            if (!wasFallingBlock) return;
            event.getEntity().remove();
            fallingBlocks.remove((FallingBlock) event.getEntity());
        }
    }

}
