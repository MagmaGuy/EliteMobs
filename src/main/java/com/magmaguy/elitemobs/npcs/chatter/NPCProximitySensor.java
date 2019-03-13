package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class NPCProximitySensor {

    private HashSet<Player> nearbyPlayers = new HashSet<>();

    public NPCProximitySensor() {

        new BukkitRunnable() {

            @Override
            public void run() {

                HashSet<Player> seenPlayerList = (HashSet<Player>) nearbyPlayers.clone();

                for (NPCEntity npcEntity : EntityTracker.getNPCEntities())
                    if (npcEntity.getVillager().isValid())
                        for (Entity entity : npcEntity.getVillager().getNearbyEntities(npcEntity.getActivationRadius(),
                                npcEntity.getActivationRadius(), npcEntity.getActivationRadius()))
                            if (entity.getType().equals(EntityType.PLAYER)) {
                                if (nearbyPlayers.contains(entity)) {
                                    NPCChatBubble.NPCChatBubble(npcEntity.getDialog(), npcEntity.getVillager());
                                    nearbyPlayers.add((Player) entity);
                                } else {
                                    NPCChatBubble.NPCChatBubble(npcEntity.getGreetings(), npcEntity.getVillager());
                                    seenPlayerList.remove(entity);
                                }
                            }

                for (Player player : seenPlayerList)
                    nearbyPlayers.remove(player);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 5);


    }

}
