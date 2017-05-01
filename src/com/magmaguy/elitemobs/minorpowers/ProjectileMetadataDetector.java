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

import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class ProjectileMetadataDetector {

    public static boolean projectileMetadataDetector(Projectile projectile, String metadata) {

        ProjectileSource trueDamager = projectile.getShooter();

        if (trueDamager instanceof Blaze || trueDamager instanceof Skeleton || trueDamager instanceof Witch ||
                trueDamager instanceof Stray) {

            Entity trueDamagerEntity = (Entity) trueDamager;

            if (trueDamagerEntity.hasMetadata(metadata)) {

                return true;

            }

        }

        return false;

    }

}
