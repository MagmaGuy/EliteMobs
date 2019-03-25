package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.NPCConfig;
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals("Guild rank selector"))
            for (Entity entity : event.getPlayer().getNearbyEntities(ConfigValues.npcConfig.getDouble(NPCConfig.GUILD_GREETER_ACTIVATION_RADIUS),
                    ConfigValues.npcConfig.getDouble(NPCConfig.GUILD_GREETER_ACTIVATION_RADIUS),
                    ConfigValues.npcConfig.getDouble(NPCConfig.GUILD_GREETER_ACTIVATION_RADIUS))) {
                if (EntityTracker.isNPCEntity(entity))
                    EntityTracker.getNPCEntity(entity).sayFarewell((Player) event.getPlayer());

            }

        if (event.getInventory().getName().equals(ConfigValues.economyConfig.getString(EconomySettingsConfig.SHOP_NAME)))
            for (Entity entity : event.getPlayer().getNearbyEntities(ConfigValues.npcConfig.getDouble(NPCConfig.BLACKSMITH_ACTIVATION_RADIUS),
                    ConfigValues.npcConfig.getDouble(NPCConfig.BLACKSMITH_ACTIVATION_RADIUS),
                    ConfigValues.npcConfig.getDouble(NPCConfig.BLACKSMITH_ACTIVATION_RADIUS))) {
                if (EntityTracker.isNPCEntity(entity))
                    EntityTracker.getNPCEntity(entity).sayFarewell((Player) event.getPlayer());

            }

        if (event.getInventory().getName().equals(ConfigValues.economyConfig.getString(EconomySettingsConfig.CUSTOM_SHOP_NAME)))
            for (Entity entity : event.getPlayer().getNearbyEntities(ConfigValues.npcConfig.getDouble(NPCConfig.SPECIAL_BLACKSMITH_ACTIVATION_RADIUS),
                    ConfigValues.npcConfig.getDouble(NPCConfig.SPECIAL_BLACKSMITH_ACTIVATION_RADIUS),
                    ConfigValues.npcConfig.getDouble(NPCConfig.SPECIAL_BLACKSMITH_ACTIVATION_RADIUS))) {
                if (EntityTracker.isNPCEntity(entity))
                    EntityTracker.getNPCEntity(entity).sayFarewell((Player) event.getPlayer());

            }

    }

}
