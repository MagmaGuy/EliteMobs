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
import com.magmaguy.elitemobs.minorpowers.ProjectileMetadataDetector;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Projectile;
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

        if (event.getDamager().hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

            if (event.getDamager().hasMetadata(MetadataHandler.DOUBLE_DAMAGE_MD)) {

                event.setDamage(damageMath(event.getFinalDamage(), event.getDamager().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt()) * 2);
                return;

            }

            event.setDamage(damageMath(event.getFinalDamage(), event.getDamager().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt()));

        }

        if (event.getDamager() instanceof Projectile) {

            if (ProjectileMetadataDetector.projectileMetadataDetector((Projectile)event.getDamager(), MetadataHandler.ELITE_MOB_MD)) {

                if (ProjectileMetadataDetector.projectileMetadataDetector((Projectile)event.getDamager(), MetadataHandler.DOUBLE_DAMAGE_MD)) {

                    event.setDamage(damageMath(event.getFinalDamage(), event.getDamager().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt()) * 2);
                    return;

                }

                event.setDamage(damageMath(event.getFinalDamage(), event.getDamager().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt()));

            }

        }

    }


    @EventHandler
    public void explosionPrimeEvent(ExplosionPrimeEvent event) {

        if (event.getEntity() instanceof Creeper && event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

            event.setRadius((float) damageMath(event.getRadius(), (int) Math.ceil(event.getEntity().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() * plugin.getConfig().getDouble("SuperCreeper explosion nerf multiplier"))));

        }

    }

    //TODO: scale witch potion effects, maybe

}
