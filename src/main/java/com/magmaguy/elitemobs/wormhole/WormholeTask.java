package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class WormholeTask {
    private WormholeTask() {
    }

    public static BukkitTask startWormholeTask(WormholeEntry wormholeEntry) {
        HashSet<Player> teleportingPlayers = new HashSet<>();
        return new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!ChunkLocationChecker.locationIsLoaded(wormholeEntry.getLocation())) {
                    cancel();
                    return;
                }

                for (Player player : Bukkit.getServer().getOnlinePlayers())
                    if (checkPoint(wormholeEntry, player.getLocation(), player, teleportingPlayers))
                        teleportingPlayers.add(player);
                    else teleportingPlayers.remove(player);

                if (!WormholesConfig.isNoParticlesMode() && !wormholeEntry.getWormhole().
                        getWormholeConfigFields().
                        getStyle().
                        equals(Wormhole.WormholeStyle.NONE)) {
                    if (counter >= wormholeEntry.getWormhole().getCachedRotations().size()) counter = 0;
                    if (WormholesConfig.isReducedParticlesMode()) {
                        if (counter % 2 == 0) visualEffect(counter, wormholeEntry);
                    } else visualEffect(counter, wormholeEntry);
                    if (counter + 1 >= Integer.MAX_VALUE) counter = 0;
                    counter++;
                }
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 0, 5);

    }

    private static boolean checkPoint(WormholeEntry wormholeEntry, Location playerLocation, Player player, HashSet<Player> teleportingPlayers) {
        if (wormholeEntry.getLocation() == null) return false;
        if (Wormhole.getPlayerCooldowns().contains(player)) return false;
        if (!PlayerQuestCooldowns.getBypassedPlayers().contains(player) &&
                wormholeEntry.getWormhole().getWormholeConfigFields().getPermission() != null &&
                !wormholeEntry.getWormhole().getWormholeConfigFields().getPermission().isEmpty() &&
                !player.hasPermission(wormholeEntry.getWormhole().getWormholeConfigFields().getPermission()))
            return false;
        if (!Objects.equals(wormholeEntry.getLocation().getWorld(), playerLocation.getWorld())) return false;
        if (wormholeEntry.getLocation().distance(playerLocation) > 1.5 * wormholeEntry.getWormhole().getWormholeConfigFields().getSizeMultiplier())
            return false;
        //A player might be standing in the teleporter after getting teleported, avoid teleporting them back
        //if (teleportingPlayers.contains(player)) return true; todo: is this working?
        if (wormholeEntry.getWormhole().getWormholeConfigFields().getCoinCost() > 0) {
            double coinCost = wormholeEntry.getWormhole().getWormholeConfigFields().getCoinCost() + wormholeEntry.getWormhole().getWormholeConfigFields().getCoinCost() * GuildRank.currencyBonusMultiplier(player.getUniqueId());
            if (EconomyHandler.checkCurrency(player.getUniqueId()) < coinCost) {
                player.sendMessage(ChatColorConverter.convert(TranslationConfig.getInsufficientCurrencyForWormholeMessage()).replace("$amount", "" + coinCost));
                return false;
            } else EconomyHandler.subtractCurrency(player.getUniqueId(), coinCost);
        }

        teleport(wormholeEntry, player);
        return true;
    }

    private static void teleport(WormholeEntry wormholeEntry, Player player) {
        Location destination;
        if (wormholeEntry == wormholeEntry.getWormhole().getWormholeEntry1())
            destination = wormholeEntry.getWormhole().getWormholeEntry2().getLocation();
        else destination = wormholeEntry.getWormhole().getWormholeEntry1().getLocation();
        if (destination == null || destination.getWorld() == null) {
            if (wormholeEntry.getPortalMissingMessage() == null)
                player.sendMessage(ChatColorConverter.convert(WormholesConfig.getDefaultPortalMissingMessage()));
            else {
                player.sendMessage(wormholeEntry.getPortalMissingMessage());
                if (player.isOp() || player.hasPermission("elitemobs.*"))
                    player.sendMessage(wormholeEntry.getOpMessage());
            }
            return;
        }
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (wormholeEntry.getWormhole().getWormholeConfigFields().isBlindPlayer())
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 0));
            player.teleport(destination);
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
            player.setVelocity(destination.getDirection().normalize());
            player.setFlying(false);
            Wormhole.getPlayerCooldowns().add(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> Wormhole.getPlayerCooldowns().remove(player), 20 * 10L);
        });

    }

    private static void visualEffect(int counter, WormholeEntry wormholeEntry) {
        for (Vector vector : new ArrayList<Vector>(wormholeEntry.getWormhole().getCachedRotations().get(counter))) {
            Location particleLocation = wormholeEntry.getLocation().clone().add(vector);
            wormholeEntry.getLocation().getWorld().spawnParticle(Particle.REDSTONE, particleLocation.getX(), particleLocation.getY(), particleLocation.getZ(), 1, 0, 0, 0, 1, new Particle.DustOptions(wormholeEntry.getWormhole().getParticleColor(), 1));
        }
    }
}
