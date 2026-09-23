package com.magmaguy.elitemobs.instanced;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.magmacore.util.TemporaryBlockManager;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class InstanceDeathLocation {
    private final MatchInstance matchInstance;
    @Getter
    private Block bannerBlock;
    @Getter
    private Player deadPlayer;
    private Location deathLocation = null;
    private FakeText nameTag;
    private FakeText livesLeft;
    private FakeText instructions;
    private TemporaryBlockManager.OwnedBlock bannerLease;
    private BukkitTask watchdog;
    private boolean cleared;


    protected InstanceDeathLocation(Player player, MatchInstance matchInstance) {
        this.matchInstance = matchInstance;
        if (matchInstance.playerLives.get(player) < 1)
            return;
        this.deadPlayer = player;
        if (!relocate(player.getLocation())) return;
        bannerWatchdog();
    }

    private void createDisplays() {
        instructions = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 2.2, 0)), DungeonsConfig.getInstancePunchToRez(), 30);
        nameTag = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 2, 0)), deadPlayer.getDisplayName(), 30);
        livesLeft = VisualDisplay.generateFakeText(deathLocation.clone().add(new Vector(0, 1.8, 0)), DungeonsConfig.getInstanceLivesLeft().replace("$amount", String.valueOf(matchInstance.playerLives.get(deadPlayer))), 30);
    }

    private Block findBannerLocation(Location location) {
        // A death below the world (void) passes the isAir check immediately and would
        // anchor the banner where nobody can punch it — anchor at the instance start instead
        if (location.getWorld() != null && location.getY() < location.getWorld().getMinHeight() &&
                matchInstance.startLocation != null)
            location = matchInstance.startLocation.clone();
        if (location.getWorld() == null) return null;
        int x = location.getBlockX(), z = location.getBlockZ();
        if (!location.getWorld().isChunkLoaded(x >> 4, z >> 4)) return null;
        for (int y = Math.max(location.getBlockY(), location.getWorld().getMinHeight());
             y < location.getWorld().getMaxHeight(); y++) {
            Block candidate = location.getWorld().getBlockAt(x, y, z);
            if (candidate.getType().isAir() && !matchInstance.deathBanners.containsKey(candidate)
                    && TemporaryBlockManager.canOwn(candidate)) return candidate;
        }
        return null;
    }

    private boolean relocate(Location location) {
        releasePlacement();
        Block candidate = findBannerLocation(location.clone());
        if (candidate == null) {
            Logger.warn("Could not place a revive banner for " + deadPlayer.getName() + ": no free loaded block below world height.");
            clear(false);
            return false;
        }
        try {
            bannerLease = TemporaryBlockManager.replaceOwned(candidate, Material.RED_BANNER.createBlockData(), MetadataHandler.PLUGIN);
            if (bannerLease == null) {
                clear(false);
                return false;
            }
            bannerBlock = candidate;
            deathLocation = candidate.getLocation();
            matchInstance.deathBanners.put(candidate, this);
            createDisplays();
            return true;
        } catch (RuntimeException failure) {
            clear(false);
            throw failure;
        }
    }

    public void clear(boolean resurrect) {
        if (cleared) return;
        cleared = true;
        if (watchdog != null) { watchdog.cancel(); watchdog = null; }
        releasePlacement();
        if (resurrect && bannerBlock != null)
            matchInstance.revivePlayer(deadPlayer, this);
    }

    private void releasePlacement() {
        if (bannerBlock != null) matchInstance.deathBanners.remove(bannerBlock, this);
        if (bannerLease != null) { bannerLease.close(); bannerLease = null; }
        if (nameTag != null) nameTag.remove();
        if (instructions != null) instructions.remove();
        if (livesLeft != null) livesLeft.remove();
        nameTag = null;
        instructions = null;
        livesLeft = null;
    }

    public Location getRespawnLocation() {
        return bannerBlock.getLocation().clone().add(0.5, 0, 0.5);
    }

    //This is necessary because physics updates might remove the banner while it should still be on there
    public void bannerWatchdog() {
        if (cleared || watchdog != null) return;
        watchdog = new BukkitRunnable() {
            @Override
            public void run() {
                if (matchInstance.deathBanners.get(bannerBlock) != InstanceDeathLocation.this) {
                    clear(false);
                    return;
                }
                if (bannerBlock.getType().equals(Material.RED_BANNER)) return;
                relocate(deathLocation);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 5, 5);
    }
}
