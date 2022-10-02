package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;

public class KeepNeutralsAngry {
    private static final HashSet<EliteEntity> angryMobs = new HashSet<>();

    private KeepNeutralsAngry() {
    }

    public static void showMeYouWarFace(EliteEntity eliteEntity) {
        //might already contain
        EntityType entityType = eliteEntity.getLivingEntity().getType();
        if (angryMobs.contains(eliteEntity)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                //It is possible for entities to change type during combat, in which case they need to be wiped
                if (!eliteEntity.isValid() ||
                        !entityType.equals(eliteEntity.getLivingEntity().getType()) ||
                        entityType.equals(EntityType.WOLF) && ((Wolf) eliteEntity.getLivingEntity()).isTamed()) {
                    cancel();
                    angryMobs.remove(eliteEntity);
                    return;
                }

                if (!eliteEntity.getLivingEntity().getType().equals(EntityType.LLAMA) &&
                        ((Mob) eliteEntity.getLivingEntity()).getTarget() != null)
                    return;

                for (Player player : Bukkit.getOnlinePlayers())
                    if (!player.getGameMode().equals(GameMode.SPECTATOR) && !player.getGameMode().equals(GameMode.CREATIVE) &&
                            Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld()) &&
                            player.getLocation().distanceSquared(eliteEntity.getLocation()) <
                                    Math.pow(Objects.requireNonNull(eliteEntity.getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE)).getValue(),2)) {
                        ((Mob) eliteEntity.getLivingEntity()).setTarget(player);
                        return;
                    }

                ((Mob) eliteEntity.getLivingEntity()).setTarget(null);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }
}
