package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.BossPower;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GoldShotgun extends BossPower implements Listener {

    public GoldShotgun() {
        super(PowersConfig.getPower("gold_shotgun.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        GoldShotgun goldShotgun = (GoldShotgun) event.getEliteMobEntity().getPower(this);
        if (goldShotgun == null) return;
        if (!eventIsValid(event, goldShotgun)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        goldShotgun.doCooldown(20 * 20, event.getEliteMobEntity());
        goldShotgun.doGoldShotgun(event.getEliteMobEntity(), event.getPlayer());

    }

    private void doGoldShotgun(EliteMobEntity eliteMobEntity, Player player) {

        eliteMobEntity.getLivingEntity().setAI(false);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                doSmokeEffect(eliteMobEntity, player);
                counter++;

                if (counter < 20 * 3) return;

                cancel();
                List<Item> nuggetList = generateVisualItems(eliteMobEntity, player);
                ProjectileDamage.doGoldNuggetDamage(nuggetList, eliteMobEntity);
                eliteMobEntity.getLivingEntity().setAI(true);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void doSmokeEffect(EliteMobEntity eliteMobEntity, Player player) {
        for (int i = 0; i < 200; i++) {
            Vector shotVector = getShotVector(eliteMobEntity, player);
            eliteMobEntity.getLivingEntity().getWorld().spawnParticle(
                    Particle.SMOKE_NORMAL,
                    eliteMobEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 0.5, 0)),
                    0,
                    shotVector.getX(),
                    shotVector.getY(),
                    shotVector.getZ(),
                    0.75);
        }
    }

    private List<Item> generateVisualItems(EliteMobEntity eliteMobEntity, Player player) {
        List<Item> nuggetList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Item visualProjectile = eliteMobEntity.getLivingEntity().getWorld().dropItem(
                    eliteMobEntity.getLivingEntity().getLocation(),
                    ItemStackGenerator.generateItemStack(
                            Material.GOLD_NUGGET,
                            "visual projectile",
                            Arrays.asList(ThreadLocalRandom.current().nextDouble() + "")));
            ProjectileDamage.configureVisualProjectile(visualProjectile);
            visualProjectile.setVelocity(getShotVector(eliteMobEntity, player).multiply(0.9));
            visualProjectile.setGravity(false);
            nuggetList.add(visualProjectile);
        }
        return nuggetList;
    }

    private Vector getShotVector(EliteMobEntity eliteMobEntity, Player player) {
        return player.getLocation().clone()
                .add(new Location(player.getWorld(), 0.0, 1.0, 0.0))
                .add(new Location(player.getWorld(),
                        ThreadLocalRandom.current().nextDouble() * 2 - 1,
                        ThreadLocalRandom.current().nextDouble() * 2 - 1,
                        ThreadLocalRandom.current().nextDouble() * 2 - 1))
                .subtract(eliteMobEntity.getLivingEntity().getLocation()).toVector().normalize();
    }

}
