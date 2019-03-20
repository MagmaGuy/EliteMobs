package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class NPCWorkingHours {

    private static HashSet<NPCEntity> sleepEnabledNPCs = new HashSet<>();

    public static HashSet<NPCEntity> getSleepEnabledNPCs() {
        return sleepEnabledNPCs;
    }

    public static void registerSlepEnabledNPC(NPCEntity npcEntity) {
        sleepEnabledNPCs.add(npcEntity);
    }

    public NPCWorkingHours() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPCEntity npcEntity : sleepEnabledNPCs)
                    if (npcEntity.getVillager().getWorld().getTime() < 24000 &&
                            npcEntity.getVillager().getWorld().getTime() > 12000) {
                        npcEntity.getVillager().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 21, 1));
                        npcEntity.setRole("<Sleeping>");
                    }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);
    }

}
