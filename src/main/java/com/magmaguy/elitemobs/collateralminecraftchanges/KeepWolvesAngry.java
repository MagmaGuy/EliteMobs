package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class KeepWolvesAngry {
    private static HashMap<EliteEntity, BukkitTask> angryDoggos = new HashMap<>();

    public void showMeYouWarFace(EliteEntity eliteEntity) {
        //might already contain
        if (angryDoggos.containsKey(eliteEntity)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                //It is possible for entities to change type during combat, in which case they need to be wiped
                if (!eliteEntity.isValid() || !eliteEntity.getLivingEntity().getType().equals(EntityType.WOLF)) {
                    cancel();
                    angryDoggos.remove(eliteEntity);
                    return;
                }

                if (((Wolf) eliteEntity.getLivingEntity()).getTarget() != null) return;

                for (Player player : Bukkit.getOnlinePlayers())
                    if (player.getLocation().getWorld().equals(eliteEntity.getLocation().getWorld()))
                        if (player.getLocation().distanceSquared(eliteEntity.getLocation()) <
                                eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getBaseValue()) {
                            ((Wolf) eliteEntity.getLivingEntity()).setTarget(player);
                            return;
                        }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }
}
