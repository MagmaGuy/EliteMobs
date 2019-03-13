package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NPCChatBubble {

    public static void NPCChatBubble(String message, LivingEntity livingEntity) {

        Location newLocation = livingEntity.getEyeLocation().clone().add(new Vector(0, -50, 0));

        ArmorStand messageBubble = (ArmorStand) newLocation.getWorld().spawnEntity(newLocation, EntityType.ARMOR_STAND);
        messageBubble.setVisible(false);
        messageBubble.setMarker(true);
        messageBubble.setCustomName(ChatColorConverter.convert(message));
        messageBubble.setCustomNameVisible(true);
        messageBubble.setGravity(false);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > 20 * 3) {
                    messageBubble.remove();
                    cancel();
                    return;
                }

                if (counter == 1)
                    messageBubble.teleport(messageBubble.getLocation().add(new Vector(0, 49.5, 0)));

                if (counter > 1)
                    messageBubble.teleport(messageBubble.getLocation().clone().add(new Vector(0, 0.005, 0)));

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    public static void NPCChatBubble(List<String> message, LivingEntity livingEntity) {

        String selectedMessage = message.get(ThreadLocalRandom.current().nextInt(message.size()));

        NPCChatBubble(selectedMessage, livingEntity);

    }

}
