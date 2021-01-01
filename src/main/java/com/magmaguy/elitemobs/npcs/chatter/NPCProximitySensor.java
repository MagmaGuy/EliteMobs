package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class NPCProximitySensor implements Listener {

    private final HashSet<Player> nearbyPlayers = new HashSet<>();

    public NPCProximitySensor() {

        new BukkitRunnable() {

            @Override
            public void run() {

                HashSet<Player> seenPlayerList = (HashSet<Player>) nearbyPlayers.clone();

                for (NPCEntity npcEntity : EntityTracker.getNPCEntities().values())
                    if (npcEntity.getVillager().isValid())
                        for (Entity entity : npcEntity.getVillager().getNearbyEntities(npcEntity.getActivationRadius(),
                                npcEntity.getActivationRadius(), npcEntity.getActivationRadius()))
                            if (entity.getType().equals(EntityType.PLAYER)) {
                                if (nearbyPlayers.contains(entity)) {
                                    if (!npcEntity.getInteractionType().equals(NPCInteractions.NPCInteractionType.CHAT))
                                        npcEntity.sayDialog((Player) entity);
                                    seenPlayerList.remove(entity);
                                    break;
                                } else {
                                    npcEntity.sayGreeting((Player) entity);
                                    nearbyPlayers.add((Player) entity);
                                    break;
                                }
                            }

                for (Player player : seenPlayerList)
                    nearbyPlayers.remove(player);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 5);


    }

    //todo: optimize
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().length() > 0)
            for (Entity entity : event.getPlayer().getNearbyEntities(5, 5, 5))
                if (EntityTracker.isNPCEntity(entity))
                    EntityTracker.getNPCEntity(entity).sayFarewell((Player) event.getPlayer());
    }

}
