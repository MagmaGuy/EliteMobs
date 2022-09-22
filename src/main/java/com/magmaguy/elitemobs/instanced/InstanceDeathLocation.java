package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.VisualArmorStand;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class InstanceDeathLocation {
    private final MatchInstance matchInstance;
    @Getter
    private Block bannerBlock;
    @Getter
    private Player deadPlayer;
    private Location deathLocation = null;
    private ArmorStand nameTag;
    private ArmorStand livesLeft;
    private ArmorStand instructions;


    protected InstanceDeathLocation(Player player, MatchInstance matchInstance) {
        this.matchInstance = matchInstance;
        if (matchInstance.playerLives.get(player) < 1)
            return;
        this.deadPlayer = player;
        this.bannerBlock = player.getLocation().getBlock();
        findBannerLocation(player.getLocation());
        instructions = VisualArmorStand.VisualArmorStand(deathLocation.clone().add(new Vector(0, 2.2, 0)), ChatColorConverter.convert("&2Punch to rez!"));
        nameTag = VisualArmorStand.VisualArmorStand(deathLocation.clone().add(new Vector(0, 2, 0)), player.getDisplayName());
        livesLeft = VisualArmorStand.VisualArmorStand(deathLocation.clone().add(new Vector(0, 1.8, 0)), matchInstance.playerLives.get(deadPlayer) + " lives left!");
        if (deathLocation != null)
            matchInstance.deathBanners.put(bannerBlock, this);

        bannerWatchdog();
    }

    private void findBannerLocation(Location location) {
        if (location.getBlock().getType().isAir())
            setBannerBlock(location);
        else if (location.getY() < 320)
            findBannerLocation(location.add(new Vector(0, 1, 0)));
    }

    private void setBannerBlock(Location location) {
        deathLocation = location;
        bannerBlock = location.getBlock();
        location.getBlock().setType(Material.RED_BANNER, false);
    }

    public void clear(boolean resurrect) {
        bannerBlock.setType(Material.AIR);
        EntityTracker.unregister(nameTag, RemovalReason.EFFECT_TIMEOUT);
        EntityTracker.unregister(instructions, RemovalReason.EFFECT_TIMEOUT);
        EntityTracker.unregister(livesLeft, RemovalReason.EFFECT_TIMEOUT);
        matchInstance.deathBanners.remove(bannerBlock);
        if (resurrect)
            matchInstance.revivePlayer(deadPlayer, this);
    }

    //This is necessary because physics updates might remove the banner while it should still be on there
    public void bannerWatchdog() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!matchInstance.deathBanners.containsKey(bannerBlock)) {
                    cancel();
                    return;
                }
                if (bannerBlock.getType().equals(Material.RED_BANNER)) return;
                EntityTracker.unregister(nameTag, RemovalReason.EFFECT_TIMEOUT);
                EntityTracker.unregister(instructions, RemovalReason.EFFECT_TIMEOUT);
                EntityTracker.unregister(livesLeft, RemovalReason.EFFECT_TIMEOUT);
                findBannerLocation(deathLocation);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 5, 5);
    }
}
