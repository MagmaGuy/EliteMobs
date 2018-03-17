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

package com.magmaguy.elitemobs.mobpowers;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ProjectileLocationGenerator {

    public static Location generateLocation(LivingEntity targetter, LivingEntity targetted) {

        Location targetterLocation = targetter.getLocation();
        Vector toTargetVector = targetted.getEyeLocation().subtract(targetterLocation).toVector().normalize();
        Location offsetLocation = new Location(targetterLocation.getWorld(),
                targetterLocation.getX() + toTargetVector.getX(),
                targetterLocation.getY() + 1,
                targetterLocation.getZ() + toTargetVector.getZ());

        return offsetLocation;

    }

}
