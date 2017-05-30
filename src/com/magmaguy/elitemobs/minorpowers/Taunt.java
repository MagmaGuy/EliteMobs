/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.minorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class Taunt extends MinorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.TAUNT_MD;

    private static Random random = new Random();



    private final static List<String> TARGET_TAUNT_LIST = ConfigValues.translationConfig.getStringList("Taunts.Target");

    private final static List<String> GENERIC_DAMAGED_LIST = ConfigValues.translationConfig.getStringList("Taunts.Damaged");

    private final static List<String> DAMAGED_BY_BOW_LIST = ConfigValues.translationConfig.getStringList("Taunts.BowDamaged");

    private final static List<String> HIT_LIST = ConfigValues.translationConfig.getStringList("Taunts.Hit");

    private final static List<String> DEATH_LIST = ConfigValues.translationConfig.getStringList("Taunts.Death");

    private int processID;

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void onTarget (EntityTargetEvent event) {

        if (event.getEntity().hasMetadata(powerMetadata)) {

            if (event.getTarget() instanceof Player) {

                Entity targetter = event.getEntity();

                nametagProcessor(targetter, TARGET_TAUNT_LIST);

            }

        }

    }

    @EventHandler
    public void onDamaged (EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity) ||
                ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() <= 0 ||
                !event.getEntity().isValid()) {

            return;

        }

        if (event.getEntity().hasMetadata(powerMetadata)) {

            Entity entity = event.getEntity();

            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {

                nametagProcessor(entity, DAMAGED_BY_BOW_LIST);

            } else {

                nametagProcessor(entity, GENERIC_DAMAGED_LIST);

            }

        }

    }

    @EventHandler
    public void onHit (EntityDamageByEntityEvent event) {


            if (event.getDamager().hasMetadata(powerMetadata) || event.getDamager() instanceof Projectile &&
                    ProjectileMetadataDetector.projectileMetadataDetector((Projectile) event.getDamager(), powerMetadata)) {

                if (event.getDamager().hasMetadata(powerMetadata)) {

                    nametagProcessor(event.getDamager(), HIT_LIST);

                } else if (event.getDamager() instanceof Projectile &&
                        ProjectileMetadataDetector.projectileMetadataDetector((Projectile) event.getDamager(), powerMetadata)) {

                    Entity entity = (Entity) ((Projectile) event.getDamager()).getShooter();

                    nametagProcessor(entity, HIT_LIST);

                }

            }


    }

    @EventHandler
    public void onDeath (EntityDeathEvent event){

        if (event.getEntity().hasMetadata(powerMetadata)){

            Entity entity = event.getEntity();

            nametagProcessor(entity, DEATH_LIST);

        }

    }

    private void nametagProcessor (Entity entity, List<String> list){

        if (entity.hasMetadata(MetadataHandler.TAUNT_NAME)) {

            return;

        }

        entity.setMetadata(MetadataHandler.TAUNT_NAME, new FixedMetadataValue(plugin, true));

        String originalName = entity.getCustomName();

        int randomizedKey = random.nextInt(list.size());
        String tempName = list.get(randomizedKey);

        entity.setCustomName(chatColorConverter(tempName));

        new BukkitRunnable(){

            @Override
            public void run() {

                if (!entity.isValid()){
                    return;
                }

                if (entity.hasMetadata(MetadataHandler.CUSTOM_NAME)) {
                    entity.setCustomName(originalName);
                } else {
                    entity.setCustomName(NameHandler.customAggressiveName(entity));
                }

                entity.removeMetadata(MetadataHandler.TAUNT_NAME, plugin);

            }


        }.runTaskLater(plugin, 4 * 20);

    }

}
