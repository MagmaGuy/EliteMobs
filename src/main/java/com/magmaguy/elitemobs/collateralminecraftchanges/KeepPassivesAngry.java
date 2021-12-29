package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;

public class KeepPassivesAngry {
    private static final HashSet<EliteEntity> angryMobs = new HashSet<>();

    private KeepPassivesAngry() {
    }

    public static void showMeYouWarFace(EliteEntity eliteEntity) {
        //might already contain
        if (angryMobs.contains(eliteEntity)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                //It is possible for entities to change type during combat, in which case they need to be wiped
                if (!eliteEntity.isValid() || !eliteEntity.getLivingEntity().getType().equals(EntityType.WOLF)) {
                    cancel();
                    angryMobs.remove(eliteEntity);
                    return;
                }

                if (((Mob) eliteEntity.getLivingEntity()).getTarget() != null) return;

                for (Player player : Bukkit.getOnlinePlayers())
                    if (Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld()) &&
                            player.getLocation().distanceSquared(eliteEntity.getLocation()) <
                                    Objects.requireNonNull(eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE)).getBaseValue()) {
                        ((Mob) eliteEntity.getLivingEntity()).setTarget(player);
                        return;
                    }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }
}
