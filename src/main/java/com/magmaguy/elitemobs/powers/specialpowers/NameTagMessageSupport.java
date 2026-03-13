package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NameTagMessageSupport {
    private NameTagMessageSupport() {
    }

    public static void nameTagProcessor(EliteEntity eliteEntity, Entity entity, List<String> list) {
        if (entity == null || !eliteEntity.isValid() || list == null || list.isEmpty()) {
            return;
        }
        String tempName = list.get(ThreadLocalRandom.current().nextInt(list.size()));
        entity.setCustomName(ChatColorConverter.convert(tempName));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid()) {
                    return;
                }
                entity.setCustomName(eliteEntity.getName());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 4 * 20L);
    }
}
