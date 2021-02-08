package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.BossPower;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GoldExplosion extends BossPower implements Listener {

    public GoldExplosion() {
        super(PowersConfig.getPower("gold_explosion.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {

        GoldExplosion goldExplosion = (GoldExplosion) event.getEliteMobEntity().getPower(this);
        if (goldExplosion == null) return;
        if (!eventIsValid(event, goldExplosion)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        goldExplosion.doCooldown(20 * 20, event.getEliteMobEntity());
        doGoldExplosion(event.getEliteMobEntity());

    }

    private void doGoldExplosion(EliteMobEntity eliteMobEntity) {

        eliteMobEntity.getLivingEntity().setAI(false);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (MobCombatSettingsConfig.enableWarningVisualEffects)
                    eliteMobEntity.getLivingEntity().getWorld().spawnParticle(Particle.SMOKE_NORMAL, eliteMobEntity.getLivingEntity().getLocation(), counter, 1, 1, 1, 0);

                if (counter < 20 * 1.5) return;
                cancel();
                eliteMobEntity.getLivingEntity().setAI(true);
                List<Item> goldNuggets = generateVisualItems(eliteMobEntity);
                ProjectileDamage.doGoldNuggetDamage(goldNuggets, eliteMobEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private List<Item> generateVisualItems(EliteMobEntity eliteMobEntity) {
        List<Item> visualItemsList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Vector velocityVector = new Vector(
                    ThreadLocalRandom.current().nextDouble() - 0.5,
                    ThreadLocalRandom.current().nextDouble() / 1.5,
                    ThreadLocalRandom.current().nextDouble() - 0.5);

            Item visualProjectile = eliteMobEntity.getLivingEntity().getWorld().dropItem(
                    eliteMobEntity.getLivingEntity().getLocation().clone()
                            .add(new Vector(velocityVector.getX(),
                                    0.5,
                                    velocityVector.getZ())),
                    ItemStackGenerator.generateItemStack(
                            Material.GOLD_NUGGET,
                            "visual projectile",
                            Arrays.asList(ThreadLocalRandom.current().nextDouble() + "")));
            ProjectileDamage.configureVisualProjectile(visualProjectile);
            visualProjectile.setVelocity(velocityVector);
            visualItemsList.add(visualProjectile);
        }
        return visualItemsList;
    }
}
