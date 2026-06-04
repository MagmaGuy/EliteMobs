package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RemoveCommand {
    private static final Set<UUID> removingPlayers = new HashSet<>();

    public static void shutdown() {
        removingPlayers.clear();
    }

    private RemoveCommand() {
    }

    public static void remove(Player player) {
        if (removingPlayers.contains(player.getUniqueId())) {
            player.sendMessage(CommandMessagesConfig.getRemoveElitesOffMessage());
            removingPlayers.remove(player.getUniqueId());
        } else {
            removingPlayers.add(player.getUniqueId());
            player.sendMessage(CommandMessagesConfig.getRemoveElitesOnMessage());
        }
    }

    public static boolean isRemoving(Player player) {
        return removingPlayers.contains(player.getUniqueId());
    }

    public static boolean removeTrackedEntity(Player player, Entity entity) {
        return removeEntity(player, entity, false);
    }

    private static boolean removeEntity(Player player, Entity entity, boolean removeUntrackedEntity) {
        if (entity == null) return false;

        NPCEntity npcEntity = EntityTracker.getNPCEntity(entity);
        if (npcEntity != null) {
            npcEntity.remove(RemovalReason.REMOVE_COMMAND);
            return true;
        }

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
        if (eliteEntity == null) {
            if (!removeUntrackedEntity) return false;
            player.sendMessage(CommandMessagesConfig.getRemovedNotEliteMobsMessage());
            player.sendMessage(CommandMessagesConfig.getRemovedHijackedMessage());
            entity.remove();
            return true;
        }
        if (eliteEntity instanceof RegionalBossEntity)
            player.sendMessage(CommandMessagesConfig.getRemovedSpawnLocationMessage()
                    .replace("$boss", ((RegionalBossEntity) eliteEntity).getCustomBossesConfigFields().getFilename()));
        eliteEntity.remove(RemovalReason.REMOVE_COMMAND);
        return true;
    }

    public static class RemoveCommandEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void quitEvent(PlayerQuitEvent event) {
            removingPlayers.remove(event.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
        public void removeEliteEntity(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player player)) return;
            if (!isRemoving(player)) return;
            if (!removeEntity(player, event.getEntity(), true)) return;
            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
        public void removeTreasureChest(PlayerInteractEvent event) {
            if (!isRemoving(event.getPlayer())) return;
            if (event.getClickedBlock() == null) return;
            TreasureChest treasureChest = TreasureChest.getTreasureChest(event.getClickedBlock().getLocation());
            if (treasureChest == null) return;
            treasureChest.removeTreasureChest();
            event.getPlayer().sendMessage(CommandMessagesConfig.getRemovedTreasureChestMessage());
            event.setCancelled(true);
            event.getClickedBlock().setType(Material.AIR);
        }
    }

}

