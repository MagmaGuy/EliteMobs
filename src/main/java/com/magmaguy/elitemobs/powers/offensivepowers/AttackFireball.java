package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackFireball extends MinorPower implements Listener {

    public AttackFireball() {
        super(PowersConfig.getPower("attack_fireball.yml"));
    }

    @EventHandler
    public void targetEvent(EliteMobTargetPlayerEvent event) {
        if (!(event.getEliteMobEntity().getLivingEntity() instanceof Monster)) return;
        AttackFireball attackFireball = (AttackFireball) event.getEliteMobEntity().getPower(this);
        if (attackFireball == null) return;
        if (attackFireball.getIsFiring()) return;

        attackFireball.setIsFiring(true);
        repeatingFireballTask((Monster) event.getEntity(), attackFireball);
    }

    private void repeatingFireballTask(Monster monster, AttackFireball attackFireball) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!monster.isValid() || monster.getTarget() == null) {
                    attackFireball.setIsFiring(false);
                    cancel();
                    return;
                }

                for (Entity nearbyEntity : monster.getNearbyEntities(20, 20, 20))
                    if (nearbyEntity instanceof Player)
                        if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL))
                            shootFireball(monster, (Player) nearbyEntity);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 8);

    }

    public static Fireball shootFireball(Entity entity, Player player) {
        Vector targetterToTargetted = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(0.01);
        Fireball repeatingFireball = (Fireball) EliteProjectile.create(EntityType.FIREBALL, entity, player, targetterToTargetted, true);
        repeatingFireball.setYield(3F);
        repeatingFireball.setIsIncendiary(true);
        repeatingFireball.setShooter((ProjectileSource) entity);
        return repeatingFireball;
    }

}
