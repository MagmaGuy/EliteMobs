package com.magmaguy.elitemobs.mobcustomizer.displays;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DisplayMob implements Listener {

    public static List<LivingEntity> livingEntityList = new ArrayList<>();
    private static String encodedKey = ChatColorConverter.chatColorConverter("&D&d&d&d&D&d&d&f");

    public static void nameEncoder(LivingEntity livingEntity) {

        new BukkitRunnable() {
            @Override
            public void run() {
                livingEntity.setCustomName(ChatColorConverter.chatColorConverter(encodedKey + livingEntity.getCustomName()));
                MetadataHandler.registerMetadata(livingEntity, MetadataHandler.CUSTOM_NAME, true);
                MetadataHandler.registerMetadata(livingEntity, MetadataHandler.LIVINGENTITY_DISPLAY, true);
                MetadataHandler.registerMetadata(livingEntity, MetadataHandler.PERSISTENT_ENTITY, true);
                livingEntityList.add(livingEntity);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);

    }

    public static boolean hasNameKey(LivingEntity livingEntity) {

        if (livingEntity.getCustomName() == null) return false;
        return (livingEntity.getCustomName().contains(encodedKey));

    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        for (Entity entity : event.getChunk().getEntities())
            if (entity instanceof LivingEntity)
                if (hasNameKey((LivingEntity) entity))
                    if (!livingEntityList.contains(entity))
                        livingEntityList.add((LivingEntity) entity);

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(MetadataHandler.LIVINGENTITY_DISPLAY))
            if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))
                event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.LIVINGENTITY_DISPLAY))
            if (event.getDamager().isOp()) {
                event.getEntity().remove();
                event.setCancelled(true);
            } else
                event.setCancelled(true);

    }

    @EventHandler
    public void onFire(EntityCombustEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.LIVINGENTITY_DISPLAY))
            event.setCancelled(true);

    }

}
