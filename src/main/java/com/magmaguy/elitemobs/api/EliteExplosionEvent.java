package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonEmpoweredLightning;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonTornado;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EliteExplosionEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    public static List<FallingBlock> fallingBlocks = new ArrayList<>();
    private final EliteEntity eliteEntity;
    private final List<BlockState> blockStates;
    private final ElitePower elitePower;
    private Location explosionSourceLocation;
    private boolean isCancelled = false;

    public EliteExplosionEvent(EliteEntity eliteEntity,
                               ElitePower elitePower,
                               Location explosionSourceLocation,
                               List<BlockState> blockStates) {
        this.eliteEntity = eliteEntity;
        this.explosionSourceLocation = explosionSourceLocation;
        this.elitePower = elitePower;
        this.blockStates = blockStates;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EliteEntity getEliteMobEntity() {
        return eliteEntity;
    }

    public Location getExplosionSourceLocation() {
        return explosionSourceLocation;
    }

    public void setExplosionSourceLocation(Location explosionSourceLocation) {
        this.explosionSourceLocation = explosionSourceLocation;
    }

    public ElitePower getElitePower() {
        return elitePower;
    }

    public List<BlockState> getBlockStates() {
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

    public void visualExplosionEffect(ElitePower elitePower) {
        VisualExplosionEffectType visualExplosionEffectType;

        if (elitePower == null)
            visualExplosionEffectType = VisualExplosionEffectType.NORMAL;
        else if (elitePower.getFileName().equals(new EnderDragonEmpoweredLightning().getFileName()))
            visualExplosionEffectType = VisualExplosionEffectType.ASCEND;
        else if (elitePower instanceof EnderDragonTornado) {
            visualExplosionEffectType = VisualExplosionEffectType.HIGH_POWER;
        } else
            visualExplosionEffectType = VisualExplosionEffectType.NORMAL;

        for (BlockState blockState : blockStates) {
            FallingBlock fallingBlock = blockState.getBlock().getWorld().spawnFallingBlock(blockState.getLocation().add(new Vector(0, 0.2, 0)), blockState.getBlockData());
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(true);

            switch (visualExplosionEffectType) {
                case ASCEND:
                    fallingBlock.setVelocity(fallingBlock.getLocation().clone().subtract(explosionSourceLocation)
                            .toVector().normalize().setY(1).normalize().multiply(ThreadLocalRandom.current().nextDouble()));
                    fallingBlock.setGravity(false);
                    fallingBlock.setGlowing(true);
                    break;
                case HIGH_POWER:
                    fallingBlock.setVelocity(fallingBlock.getLocation().clone().subtract(explosionSourceLocation).toVector().normalize().setY(2).multiply(0.9));
                    break;
                case NORMAL:
                default:
                    fallingBlock.setVelocity(fallingBlock.getLocation().clone().subtract(explosionSourceLocation).toVector().normalize().setY(1).multiply(0.5));
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fallingBlock.isValid())
                        fallingBlock.remove();
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 4);

            CrashFix.persistentTracker(fallingBlock);
            fallingBlocks.add(fallingBlock);
        }
    }

    public enum VisualExplosionEffectType {
        NORMAL,
        HIGH_POWER,
        ASCEND
    }

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
