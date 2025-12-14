package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class KeepNeutralsAngry {
    private static final Map<UUID, BukkitTask> angryMobTasks = new HashMap<>();

    private KeepNeutralsAngry() {
    }

    public static void shutdown() {
        for (BukkitTask task : angryMobTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        angryMobTasks.clear();
    }

    public static void showMeYouWarFace(EliteEntity eliteEntity) {
        //might already contain
        EntityType entityType = eliteEntity.getLivingEntity().getType();
        UUID entityUUID = eliteEntity.getLivingEntity().getUniqueId();
        if (angryMobTasks.containsKey(entityUUID)) return;
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomBossesConfigFields().isNeutral())
                    return;
                //It is possible for entities to change type during combat, in which case they need to be wiped
                if (!eliteEntity.isValid() ||
                        !entityType.equals(eliteEntity.getLivingEntity().getType()) ||
                        entityType.equals(EntityType.WOLF) && ((Wolf) eliteEntity.getLivingEntity()).isTamed()) {
                    cancel();
                    angryMobTasks.remove(entityUUID);
                    return;
                }

                if (!eliteEntity.getLivingEntity().getType().equals(EntityType.LLAMA) && !eliteEntity.getLivingEntity().getType().equals(EntityType.RABBIT) &&
                        ((Mob) eliteEntity.getLivingEntity()).getTarget() != null)
                    return;

                for (Player player : Bukkit.getOnlinePlayers())
                    if (!player.getGameMode().equals(GameMode.SPECTATOR) && !player.getGameMode().equals(GameMode.CREATIVE) &&
                            Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld()) &&
                            player.getLocation().distanceSquared(eliteEntity.getLocation()) <
                                    Math.pow(AttributeManager.getAttributeBaseValue(eliteEntity.getLivingEntity(), "generic_follow_range"), 2)) {
                        ((Mob) eliteEntity.getLivingEntity()).setTarget(player);
                        return;
                    }

                ((Mob) eliteEntity.getLivingEntity()).setTarget(null);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
        angryMobTasks.put(entityUUID, task);
    }
}
