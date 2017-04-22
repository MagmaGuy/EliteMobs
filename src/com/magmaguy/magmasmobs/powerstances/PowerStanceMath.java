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

package com.magmaguy.magmasmobs.powerstances;

import com.magmaguy.magmasmobs.MagmasMobs;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;


/**
 * Created by MagmaGuy on 04/11/2016.
 */
public class PowerStanceMath {

    private MagmasMobs plugin;

    public PowerStanceMath(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }

    public List<Location> cylindricalPowerStance(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double speedVertical, float counter) {

        //this code assumes the existence of two translating points
        List<Location> locations = cylindricalPowerStanceMath(entity, radiusHorizontal, radiusVertical, speedHorizontal, speedVertical, counter);

        return locations;

    }

    private List<Location> cylindricalPowerStanceMath(Entity entity, double radiusHorizontal, double radiusVertical, double speedHorizontal, double speedVertical, float counter) {

        List<Location> coordinateLocations = new ArrayList<>();

        speedHorizontal = speedHorizontal * 12 + counter * 12;

        if (speedVertical != 0) {

            speedVertical = speedVertical * 12 + counter * 12;

        }

        double x1 = (0.5 * cos(speedHorizontal) - sin(speedHorizontal) * radiusHorizontal);
        double y1 = cos(speedVertical) * radiusVertical;
        double z1 = (0.5 * sin(speedHorizontal) + cos(speedHorizontal) * radiusHorizontal);

        Location entityLocation = entity.getLocation();

        double playerX = entityLocation.getX();
        double playerY = entityLocation.getY();
        double playerZ = entityLocation.getZ();

        double newX = x1 + playerX;
        double newY = y1 + playerY + 1;
        double newZ = z1 + playerZ;

        Location particleLocation1 = new Location(entity.getWorld(), newX, newY, newZ);
        coordinateLocations.add(particleLocation1);


        double x2 = (-0.5 * cos(speedHorizontal) - sin(speedHorizontal) * radiusHorizontal);
        double y2 = cos(speedVertical) * radiusVertical * -1;
        double z2 = (-0.5 * sin(speedHorizontal) + cos(speedHorizontal) * radiusHorizontal);

        newX = x2 + playerX;
        newY = y2 + playerY + 1;
        newZ = z2 + playerZ;

        Location particleLocation2 = new Location(entity.getWorld(), newX, newY, newZ);
        coordinateLocations.add(particleLocation2);

        return coordinateLocations;

    }

}
