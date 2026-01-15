package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

public class DungeonProtector implements Listener {
    private static final HashSet<UUID> bypassingPlayers = new HashSet<>();

    public static void shutdown() {
        bypassingPlayers.clear();
    }

    public static boolean toggleBypass(UUID playerUUID) {
        if (bypassingPlayers.contains(playerUUID)) {
            bypassingPlayers.remove(playerUUID);
            return false;
        }
        bypassingPlayers.add(playerUUID);
        return true;
    }

    private boolean shouldBypass(Player player) {
        return bypassingPlayers.contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockDamage(BlockDamageEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockBreak(BlockBreakEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockBurnDamage(BlockBurnEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockPlace(BlockCanBuildEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (event.getPlayer() != null && shouldBypass(event.getPlayer())) return;
        event.setBuildable(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockExplosionEvent(BlockExplodeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventEntityExplosionEvent(EntityExplodeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getLocation().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getLocation().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.blockList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventTntPrimeEvent(TNTPrimeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockFadeEvent(BlockFadeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (event.getBlock().getType().equals(Material.FROSTED_ICE)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBonemeal(BlockFertilizeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (event.getPlayer() != null && shouldBypass(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLiquidFlow(BlockFromToEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID()).getContentPackagesConfigFields().isAllowLiquidFlow())
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockFire(BlockIgniteEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockPlace(BlockPlaceEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLiquidPlace(PlayerBucketEmptyEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLeafDecay(LeavesDecayEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventVanillaMobSpawning(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) return;
        if (!EliteMobsWorld.isEliteMobsWorld(event.getLocation().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventFriendlyFireInDungeon(EntityDamageByEntityEvent event) {
        if (DungeonsConfig.isFriendlyFireInDungeons()) return;
        if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getLocation().getWorld().getUID())) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventDoorOpeningSpawning(PlayerInteractEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getPlayer().getWorld().getUID())) return;
        if (event.getClickedBlock() == null) return;
        if (shouldBypass(event.getPlayer())) return;
        Material material = event.getClickedBlock().getType();
        if (material.toString().toLowerCase(Locale.ROOT).endsWith("_door") ||
                material.toString().toLowerCase(Locale.ROOT).endsWith("_trapdoor"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventDoorOpeningSpawning(EntityChangeBlockEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventCobwebPotions(LingeringPotionSplashEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getWorld().getUID())) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        event.getEntity().getEffects().forEach(potionEffect -> {
            if (potionEffect.getType().equals(PotionEffectType.WEAVING)) event.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventCobwebPotions(PotionSplashEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getWorld().getUID())) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        event.getEntity().getEffects().forEach(potionEffect -> {
            if (potionEffect.getType().equals(PotionEffectType.WEAVING)) event.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventSignEdits(SignChangeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getPlayer().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventContainerAccess(PlayerInteractEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getPlayer().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();

        // Check if this is a container type
        if (type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                type == Material.BARREL || type == Material.SHULKER_BOX ||
                type.name().endsWith("_SHULKER_BOX")) {

            // Allow treasure chest interaction
            if (TreasureChest.getTreasureChest(block.getLocation()) != null) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventDragonEggInteraction(PlayerInteractEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getPlayer().getWorld().getUID())) return;
        if (shouldBypass(event.getPlayer())) return;

        Block block = event.getClickedBlock();
        if (block != null && block.getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

}
