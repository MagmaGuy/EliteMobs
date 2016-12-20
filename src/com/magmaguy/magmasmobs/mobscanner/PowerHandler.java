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

package com.magmaguy.magmasmobs.mobscanner;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.minorpowers.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class PowerHandler {

    private MagmasMobs plugin;

    public PowerHandler(Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }

    public void powerHandler(Entity entity)
    {

        int availableMinorPowers = 0;
        int availableMajorPowers = 0;

        if (entity.hasMetadata("MagmasSuperMob") && entity.isValid() && ((LivingEntity) entity).getHealth() > 0)
        {

            if (entity.getMetadata("MagmasSuperMob").get(0).asInt() >= 5)
            {

                int superMobLevel = entity.getMetadata("MagmasSuperMob").get(0).asInt();

                availableMinorPowers = (superMobLevel - 5) / 10 + 1;
                availableMajorPowers = superMobLevel / 10;

            }

        }

        if (availableMinorPowers >= 1)
        {

            int currentMinorPowerAmount = 0;

            ArrayList<MinorPowers> minorPowerArray = new ArrayList();
            minorPowerArray.add(new InvulnerabilityFire(plugin));
            minorPowerArray.add(new InvulnerabilityArrow(plugin));
            minorPowerArray.add(new AttackGravity(plugin));
            minorPowerArray.add(new AttackPush(plugin));
            minorPowerArray.add(new AttackPoison(plugin));
            minorPowerArray.add(new AttackWither(plugin));
            minorPowerArray.add(new MovementSpeed(plugin));
            minorPowerArray.add(new InvulnerabilityFallDamage(plugin));

            if (entity.hasMetadata("MinorPowerAmount"))
            {

                currentMinorPowerAmount = entity.getMetadata("MinorPowerAmount").get(0).asInt();

                Iterator<MinorPowers> minorPowerIterator = minorPowerArray.iterator();

                while(minorPowerIterator.hasNext())
                {

                    MinorPowers minorPower = minorPowerIterator.next();

                    if (minorPower.existingPowers(entity))
                    {

                        minorPowerIterator.remove();

                    }

                }

            }

            int missingMinorPowerAmount = availableMinorPowers - currentMinorPowerAmount;

            if (missingMinorPowerAmount > 0 && minorPowerArray.size() > 0)
            {

                for (int i = 0; i < missingMinorPowerAmount; i++)
                {

                    if (minorPowerArray.size() > 0)
                    {

                        Random random = new Random();
                        int randomizer = random.nextInt(minorPowerArray.size());
                        MinorPowers selectedMinorPower = minorPowerArray.get(randomizer);
                        minorPowerArray.remove(minorPowerArray.get(randomizer));

                        if (entity.hasMetadata("MinorPowerAmount"))
                        {

                            int oldMinorPowerAmount = entity.getMetadata("MinorPowerAmount").get(0).asInt();
                            int newMinorPowerAmount = oldMinorPowerAmount + 1;

                            entity.setMetadata("MinorPowerAmount", new FixedMetadataValue(plugin, newMinorPowerAmount));

                        } else {

                            entity.setMetadata("MinorPowerAmount", new FixedMetadataValue(plugin, 1));

                        }

                        selectedMinorPower.applyPowers(entity);

                    }

                }

            }

        }

        if (availableMajorPowers >= 1)
        {

            //Todo: Add major powers

        }

    }
}
