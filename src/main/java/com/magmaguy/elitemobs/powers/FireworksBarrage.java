package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FireworksBarrage extends BossPower {
    public FireworksBarrage() {
        super(PowersConfig.getPower("fireworks_barrage.yml"));
    }

    public void doFireworksBarrage(EliteEntity eliteEntity) {
        eliteEntity.getLivingEntity().setAI(false);
        if (eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)).getBlock().getType().equals(Material.AIR))
            if (!eliteEntity.getLivingEntity().getType().equals(EntityType.GHAST))
                eliteEntity.getLivingEntity().teleport(eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 10, 0)));
        new BukkitRunnable() {
            final Location initialLocation = eliteEntity.getLivingEntity().getLocation().clone();
            int counter = 0;

            @Override
            public void run() {

                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 2; i++) {
                    Firework firework = (Firework) eliteEntity.getLivingEntity().getWorld().spawnEntity(eliteEntity.getLivingEntity().getLocation(), EntityType.FIREWORK);
                    FireworkMeta fireworkMeta = firework.getFireworkMeta();
                    fireworkMeta.setPower(10);
                    fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED, Color.WHITE, Color.BLUE).flicker(true).build());
                    firework.setFireworkMeta(fireworkMeta);
                    firework.setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(-0.5, 0.5),
                            ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble(-0.5, 0.5)));
                }

                for (Entity nearbyEntity : eliteEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                    if (nearbyEntity instanceof Player)
                        if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL)) {
                            Firework firework = (Firework) eliteEntity.getLivingEntity().getWorld().spawnEntity(eliteEntity.getLivingEntity().getLocation(), EntityType.FIREWORK);
                            FireworkMeta fireworkMeta = firework.getFireworkMeta();
                            fireworkMeta.setPower(10);
                            fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED, Color.WHITE, Color.BLUE).flicker(true).build());
                            firework.setFireworkMeta(fireworkMeta);
                            firework.setVelocity(
                                    ((Player) nearbyEntity).getEyeLocation().clone()
                                            .subtract(firework.getLocation())
                                            .toVector().normalize().multiply(0.5));
                            firework.setShotAtAngle(true);
                            new FireworkTask(firework, nearbyEntity.getLocation().clone(), eliteEntity);
                        }

                counter++;
                if (counter > 10) {
                    cancel();
                    eliteEntity.getLivingEntity().setAI(true);
                    eliteEntity.getLivingEntity().teleport(initialLocation);
                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }

    public static class FireworksBarrageEvents implements Listener {
        @EventHandler
        public void onDamage(EliteMobDamagedByPlayerEvent event) {
            FireworksBarrage fireworksBarrage = (FireworksBarrage) event.getEliteMobEntity().getPower(new FireworksBarrage());
            if (fireworksBarrage == null) return;
            if (!eventIsValid(event, fireworksBarrage)) return;
            if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

            fireworksBarrage.doCooldown(event.getEliteMobEntity());
            fireworksBarrage.doFireworksBarrage(event.getEliteMobEntity());
        }
    }

    private class FireworkTask {
        public FireworkTask(Firework firework, Location targetLocation, EliteEntity eliteEntity) {
            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    counter++;
                    if (firework == null || counter > 20 * 5) {
                        cancel();
                        return;
                    }
                    if (firework.getLocation().distanceSquared(targetLocation) < Math.pow(0.01, 2)) {
                        firework.detonate();
                        List<Block> blockList = new ArrayList<>();
                        for (int x = -1; x < 2; x++)
                            for (int y = -1; y < 2; y++)
                                for (int z = -1; z < 2; z++)
                                    blockList.add(firework.getLocation().clone().add(new Vector(x, y, z)).getBlock());
                        Explosion.generateFakeExplosion(blockList, eliteEntity.getLivingEntity(), (PowersConfigFields) getPowersConfigFields(), firework.getLocation());
                    }
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        }
    }

}
