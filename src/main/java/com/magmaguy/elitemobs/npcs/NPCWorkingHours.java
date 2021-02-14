package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCWorkingHours {

    public NPCWorkingHours() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPCEntity npcEntity : EntityTracker.getNPCEntities().values()) {
                    if (npcEntity.sleepScheduled) continue;
                    npcEntity.sleepScheduled = true;
                    if (!npcEntity.getDisappearsAtNight()) continue;
                    if (npcEntity.getVillager().getWorld().getTime() < 24000 &&
                            npcEntity.getVillager().getWorld().getTime() > 12000) {
                        npcEntity.getVillager().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 12000, 1));
                        if (!npcEntity.getIsSleeping()) {
                            npcEntity.setTempRole(ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.NPC_SLEEPING_MESSAGE)));
                            npcEntity.setIsSleeping(true);
                        }
                    } else {
                        if (npcEntity.getIsSleeping()) {
                            npcEntity.setRole(npcEntity.getRole());
                            npcEntity.setIsSleeping(false);
                            npcEntity.getVillager().removePotionEffect(PotionEffectType.INVISIBILITY);
                        }
                    }
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);
    }

}
