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

package com.magmaguy.activestack.Mobs;/*
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

import com.magmaguy.activestack.ActiveStack;
import com.magmaguy.activestack.MobScanner;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.Random;

/**
 * Created by MagmaGuy on 08/10/2016.
 */
public class SilverfishHandler implements Listener{

    private ActiveStack plugin;

    public SilverfishHandler(Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (event.getEntity() instanceof Silverfish && event.getEntity().hasMetadata("SuperMob"))
        {

            Random random = new Random();

            Silverfish silverFish = (Silverfish) event.getEntity();

            double damage = event.getFinalDamage();
            //health is hardcoded here, maybe change it at some point
            double dropChance = damage / MobScanner.silverfishHealth;
            double dropRandomizer = random.nextDouble();
            //this rounds down
            int dropMinAmount = (int) dropChance;

            //this has no drops aside from xp

            for (int i = 0; i < dropMinAmount; i++)
            {

                ExperienceOrb xpDrop = silverFish.getWorld().spawn(silverFish.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

            if (dropChance > dropRandomizer)
            {

                ExperienceOrb xpDrop = silverFish.getWorld().spawn(silverFish.getLocation(), ExperienceOrb.class);
                xpDrop.setExperience(5);

            }

        }

    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Silverfish && event.getDamager().hasMetadata("SuperMob"))
        {

            event.setDamage(event.getFinalDamage() * event.getDamager().getMetadata("SuperMob").get(0).asInt());

        }

    }

}
