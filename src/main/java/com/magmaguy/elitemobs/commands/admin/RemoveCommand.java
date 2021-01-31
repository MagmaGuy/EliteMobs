package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;
import java.util.UUID;

public class RemoveCommand {
    public static void remove(Player player) {
        if (removingPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aYou are no longer removing elites!"));
            removingPlayers.remove(player.getUniqueId());
        } else {
            removingPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cYou are now removing elites when you punch them! Run &a/em remove &cagain to stop removing elites!"));
        }
    }

    public static HashSet<UUID> removingPlayers = new HashSet<>();

    public static class RemoveCommandEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void otherDamageEvents(EntityDamageByEntityEvent event) {
            if (!removingPlayers.contains(event.getDamager().getUniqueId())) return;
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity != null)
                if (eliteMobEntity.regionalBossEntity != null)
                    event.getDamager().sendMessage(ChatColorConverter.convert(
                            "&8[EliteMobs] &cRemoved a spawn location for boss " +
                                    eliteMobEntity.regionalBossEntity.getCustomBossConfigFields().getFileName()));
            EntityTracker.unregister(event.getEntity().getUniqueId(), RemovalReason.REMOVE_COMMAND);
            event.setCancelled(true);
        }
    }
}
