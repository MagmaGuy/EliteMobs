package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Getter;
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
    @Getter
    public static List<FallingBlock> fallingBlocks = new ArrayList<>();
    @Getter
    private final EliteEntity eliteEntity;
    @Getter
    private final List<BlockState> blockStates;
    @Getter
    private final PowersConfigFields powersConfigFields;
    @Getter
    private Location explosionSourceLocation;
    private boolean isCancelled = false;

    public EliteExplosionEvent(EliteEntity eliteEntity,
                               PowersConfigFields powersConfigFields,
                               Location explosionSourceLocation,
                               List<BlockState> blockStates) {
        this.eliteEntity = eliteEntity;
        this.explosionSourceLocation = explosionSourceLocation;
        this.powersConfigFields = powersConfigFields;
        this.blockStates = blockStates;
    }

    public void setExplosionSourceLocation(Location explosionSourceLocation) {
        this.explosionSourceLocation = explosionSourceLocation;
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

    public void visualExplosionEffect(PowersConfigFields powersConfigFields) {
        VisualExplosionEffectType visualExplosionEffectType;

        if (powersConfigFields == null)
            visualExplosionEffectType = VisualExplosionEffectType.NORMAL;
        else if (powersConfigFields.getFilename().equals("ender_dragon_empowered_lightning.yml"))
            visualExplosionEffectType = VisualExplosionEffectType.ASCEND;
        else if (powersConfigFields.getFilename().equalsIgnoreCase("ender_dragon_tornado.yml")) {
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
