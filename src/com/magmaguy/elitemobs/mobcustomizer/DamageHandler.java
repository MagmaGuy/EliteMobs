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

package com.magmaguy.elitemobs.mobcustomizer;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.Plugin;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class DamageHandler implements Listener{

    private EliteMobs plugin;

    public DamageHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    public static double damageMath(double initialDamage, int EliteMobLevel) {

        double finalDamage = ScalingFormula.PowerFormula(initialDamage, EliteMobLevel);

        return finalDamage;

    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (event.getDamager().hasMetadata(metadataHandler.eliteMobMD)) {

            event.setDamage(damageMath(event.getFinalDamage(), event.getDamager().getMetadata(metadataHandler.eliteMobMD).get(0).asInt()));

        } else if (event.getDamager() instanceof Fireball) {

            Fireball fireball = (Fireball) event.getDamager();

            if (fireball.getShooter() instanceof Blaze && ((Blaze) fireball.getShooter()).hasMetadata(metadataHandler.eliteMobMD)) {

                event.setDamage(damageMath(event.getFinalDamage(), ((Blaze) fireball.getShooter()).getMetadata(metadataHandler.eliteMobMD).get(0).asInt()));

            }

        } else if (event.getDamager() instanceof Arrow) {

            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Skeleton && ((Skeleton) arrow.getShooter()).hasMetadata(metadataHandler.eliteMobMD)) {

                event.setDamage(damageMath(event.getFinalDamage(), ((Skeleton) arrow.getShooter()).getMetadata(metadataHandler.eliteMobMD).get(0).asInt()));

            } else if (arrow.getShooter() instanceof Stray && ((Stray) arrow.getShooter()).hasMetadata(metadataHandler.eliteMobMD)) {

                event.setDamage(damageMath(event.getFinalDamage(), ((Stray) arrow.getShooter()).getMetadata(metadataHandler.eliteMobMD).get(0).asInt()));

            } else if (arrow.getShooter() instanceof WitherSkeleton && ((WitherSkeleton) arrow.getShooter()).hasMetadata(metadataHandler.eliteMobMD)) {

                event.setDamage(damageMath(event.getFinalDamage(), ((WitherSkeleton) arrow.getShooter()).getMetadata(metadataHandler.eliteMobMD).get(0).asInt()));

            }

        }

    }


    @EventHandler
    public void explosionPrimeEvent(ExplosionPrimeEvent event) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (event.getEntity() instanceof Creeper && event.getEntity().hasMetadata(metadataHandler.eliteMobMD)) {

            event.setRadius((float) damageMath(event.getRadius(), (int) Math.ceil(event.getEntity().getMetadata(metadataHandler.eliteMobMD).get(0).asInt() * plugin.getConfig().getDouble("SuperCreeper explosion nerf multiplier"))));

        }

    }

    //TODO: scale witch potion effects, maybe

}
