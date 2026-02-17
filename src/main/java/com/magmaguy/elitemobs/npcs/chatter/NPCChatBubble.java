package com.magmaguy.elitemobs.npcs.chatter;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NPCChatBubble {

    public NPCChatBubble(String message, NPCEntity npcEntity, Player player) {

        if (!npcEntity.getNPCsConfigFields().isCanTalk()) return;

        if (message == null) return;
        if (npcEntity.getVillager().hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        if (npcEntity.getIsTalking()) return;
        npcEntity.startTalkingCooldown();

        int lineCounter = 0;

        for (String substring : message.split("\\\\n")) {

            Location newLocation = npcEntity.getVillager().getEyeLocation().clone()
                    .add(player.getLocation().clone().subtract(npcEntity.getVillager().getLocation()).toVector().normalize().multiply(0.5))
                    .add(new Vector(0, -0.4 - (0.2 * lineCounter), 0));

            FakeText fakeText = VisualDisplay.generateFakeText(newLocation, substring, player);
            if (fakeText == null) return;

            new BukkitRunnable() {
                int counter = 0;
                Location currentLocation = newLocation.clone();

                @Override
                public void run() {
                    if (counter > 20 * 5 || npcEntity.getVillager() == null || !npcEntity.getVillager().isValid()) {
                        fakeText.remove();
                        cancel();
                        return;
                    }
                    currentLocation.add(0, 0.005, 0);
                    fakeText.teleport(currentLocation);
                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

            lineCounter++;

        }

    }

    public NPCChatBubble(List<String> message, NPCEntity npcEntity, Player player) {

        String selectedMessage = message.get(ThreadLocalRandom.current().nextInt(message.size()));

        new NPCChatBubble(selectedMessage, npcEntity, player);

    }


}
