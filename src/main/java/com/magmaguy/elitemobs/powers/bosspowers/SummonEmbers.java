package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SummonEmbers extends BossPower implements Listener {
    public SummonEmbers() {
        super(PowersConfig.getPower("summon_embers.yml"));
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        SummonEmbers summonEmbers = (SummonEmbers) event.getEliteMobEntity().getPower(this);
        if (summonEmbers == null) return;
        if (!eventIsValid(event, summonEmbers)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        summonEmbers.doCooldown(20 * 20, event.getEliteMobEntity());
        doSummonParticles(event.getEliteMobEntity());

    }

    private void doSummonParticles(EliteMobEntity eliteMobEntity) {
        eliteMobEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                eliteMobEntity.getLivingEntity().getWorld().spawnParticle(Particle.FLAME,
                        eliteMobEntity.getLivingEntity().getLocation().add(new Vector(0, 1, 0)), 50, 0.0001, 0.0001, 0.0001);
                if (counter < 20 * 3) return;
                cancel();
                doSummon(eliteMobEntity);
                eliteMobEntity.getLivingEntity().setAI(true);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void doSummon(EliteMobEntity eliteMobEntity) {

        for (int i = 0; i < 10; i++) {
            Location spawnLocation = eliteMobEntity.getLivingEntity().getLocation();

            CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss("ember.yml", spawnLocation, eliteMobEntity.getLevel());

            double x = ThreadLocalRandom.current().nextDouble() - 0.5;
            double z = ThreadLocalRandom.current().nextDouble() - 0.5;

            customBossEntity.getLivingEntity().setVelocity(new Vector(x, 0.5, z));
        }

    }

}
