package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SummonTheReturned extends BossPower implements Listener {

    public SummonTheReturned() {
        super(PowersConfig.getPower("summon_the_returned.yml"));
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        if (event.isCancelled()) return;
        SummonTheReturned summonTheReturned = (SummonTheReturned) event.getEliteMobEntity().getPower(this);
        if (summonTheReturned == null) return;
        if (!eventIsValid(event, summonTheReturned)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        summonTheReturned.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doSummonParticles(event.getEliteMobEntity());
    }

    private void doSummonParticles(EliteEntity eliteEntity) {
        eliteEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }
                counter++;
                eliteEntity.getLivingEntity().getWorld().spawnParticle(Particle.PORTAL,
                        eliteEntity.getLivingEntity().getLocation().add(new Vector(0, 1, 0)), 50, 0.01, 0.01, 0.01, 1);
                if (counter < 20 * 3) return;
                cancel();
                doSummon(eliteEntity);
                eliteEntity.getLivingEntity().setAI(true);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void doSummon(EliteEntity eliteEntity) {

        for (int i = 0; i < 10; i++) {
            Location spawnLocation = eliteEntity.getLivingEntity().getLocation();

            CustomBossEntity.createCustomBossEntity("the_returned.yml").spawn(spawnLocation, eliteEntity.getLevel(), false);

            double x = ThreadLocalRandom.current().nextDouble() - 0.5;
            double z = ThreadLocalRandom.current().nextDouble() - 0.5;

            try {
                eliteEntity.getLivingEntity().setVelocity(new Vector(x, 0.5, z));
            } catch (Exception ex) {
                new WarningMessage("Attempted to complete Summon the Returned power but a reinforcement mob wasn't detected! Did the boss move to an area that prevents spawning?");
            }
        }

    }

}
